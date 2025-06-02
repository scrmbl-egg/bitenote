package app.bitenote.activities.query;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.adapters.query.utensil.IncludedUtensilAdapter;
import app.bitenote.adapters.query.utensil.NonQueriedUtensilAdapter;
import app.bitenote.adapters.query.utensil.BannedUtensilAdapter;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Utensil;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user specifies which utensils the recipes of the
 * query must have or must not have.
 * @author Daniel N.
 */
public final class UtensilQueryActivity extends AppCompatActivity {
    /**
     * Activity executor that creates a background thread for database operations.
     */
    private final Executor mDatabaseExecutor = Executors.newSingleThreadExecutor();

    /**
     * Activity's handler for the main thread.
     */
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Application view model. Grants access to the app's database.
     */
    private BiteNoteViewModel mViewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar mMaterialToolbar;

    /**
     * Adapter for utensils that have been included in the query.
     */
    private IncludedUtensilAdapter mIncludedUtensilAdapter;

    /**
     * Recycler view for included utensils.
     */
    private RecyclerView mIncludedUtensilsRecyclerView;

    /**
     * Adapter for utensils that have been banned in the query.
     */
    private BannedUtensilAdapter mBannedUtensilAdapter;

    /**
     * Recycler view for banned utensils.
     */
    private RecyclerView mBannedUtensilsRecyclerView;

    /**
     * Adapter for utensils that have not been added to the query.
     */
    private NonQueriedUtensilAdapter mNonQueriedUtensilAdapter;

    /**
     * Recycler view for non-queried utensils.
     */
    private RecyclerView mNonQueriedUtensilsRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton mSaveChangesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.utensil_query_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Initializes all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.UtensilQueryMaterialToolbar);
        mSaveChangesButton = findViewById(R.id.UtensilQuerySaveChangesButton);
        mIncludedUtensilsRecyclerView =
                findViewById(R.id.UtensilQueryIncludedUtensilsRecyclerView);
        mBannedUtensilsRecyclerView =
                findViewById(R.id.UtensilQueryBannedUtensilsRecyclerView);
        mNonQueriedUtensilsRecyclerView =
                findViewById(R.id.UtensilQueryNonQueriedUtensilsRecyclerView);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        assert mViewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery query = mViewModel.queryLiveData.getValue();

        mDatabaseExecutor.execute(() -> {
            final List<Pair<Integer, Utensil>>
                    includedUtensils =
                    mViewModel.sqliteHelper.getQueryIncludedUtensilsWithProperties(query);
            final List<Pair<Integer, Utensil>>
                    bannedUtensils =
                    mViewModel.sqliteHelper.getQueryBannedUtensilsWithProperties(query);
            final List<Pair<Integer, Utensil>>
                    nonQueriedUtensils =
                    mViewModel.sqliteHelper.getAllUtensilsExcept(query);

            mMainThreadHandler.post(() -> {
                mIncludedUtensilAdapter = new IncludedUtensilAdapter(
                        includedUtensils,
                        getOnIncludedUtensilButtonsClickListener()
                );
                mBannedUtensilAdapter = new BannedUtensilAdapter(
                        bannedUtensils,
                        getOnBannedUtensilButtonsClickListener()
                );
                mNonQueriedUtensilAdapter = new NonQueriedUtensilAdapter(
                        nonQueriedUtensils,
                        getOnNonQueriedUtensilButtonsClickListener()
                );
                mIncludedUtensilsRecyclerView.setAdapter(mIncludedUtensilAdapter);
                mBannedUtensilsRecyclerView.setAdapter(mBannedUtensilAdapter);
                mNonQueriedUtensilsRecyclerView.setAdapter(mNonQueriedUtensilAdapter);

                mIncludedUtensilsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                mBannedUtensilsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mNonQueriedUtensilsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
            });
        });

        mSaveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Function called when {@link #mSaveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        assert mViewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery modifiedCopy = new RecipeQuery(mViewModel.queryLiveData.getValue()) {{
            clearAllUtensils();

            mIncludedUtensilAdapter.getUtensils().forEach(pair ->
                    includeUtensil(pair.first, true)
            );
            mBannedUtensilAdapter.getUtensils().forEach(pair ->
                    banUtensil(pair.first, true)
            );
        }};

        mViewModel.postQuery(modifiedCopy);

        Toast.makeText(this, R.string.utensils_saved_toast, Toast.LENGTH_SHORT).show();

        finish();
    }

    /**
     * @return The {@link IncludedUtensilAdapter.OnButtonsClickListener} implementation that
     * will run when an included utensil's card buttons are clicked.
     */
    private IncludedUtensilAdapter.OnButtonsClickListener
    getOnIncludedUtensilButtonsClickListener() {
        return new IncludedUtensilAdapter.OnButtonsClickListener() {
            @Override
            public void onBanButtonClick(int utensilId, @NonNull Utensil utensil) {
                mIncludedUtensilAdapter.removeUtensil(utensilId, utensil);
                mBannedUtensilAdapter.addUtensil(utensilId, utensil);
            }

            @Override
            public void onRemoveButtonClick(int utensilId, @NonNull Utensil utensil) {
                mIncludedUtensilAdapter.removeUtensil(utensilId, utensil);
                mNonQueriedUtensilAdapter.addUtensil(utensilId, utensil);
            }
        };
    }

    /**
     * @return The {@link BannedUtensilAdapter.OnButtonsClickListener} implementation that
     * will run when a banned utensil's card buttons are clicked.
     */
    private BannedUtensilAdapter.OnButtonsClickListener
    getOnBannedUtensilButtonsClickListener() {
        return new BannedUtensilAdapter.OnButtonsClickListener() {
            @Override
            public void onIncludeButtonClick(int utensilId, @NonNull Utensil utensil) {
                mBannedUtensilAdapter.removeUtensil(utensilId, utensil);
                mIncludedUtensilAdapter.addUtensil(utensilId, utensil);
            }

            @Override
            public void onRemoveButtonClick(int utensilId, @NonNull Utensil utensil) {
                mBannedUtensilAdapter.removeUtensil(utensilId, utensil);
                mNonQueriedUtensilAdapter.addUtensil(utensilId, utensil);
            }
        };
    }

    /**
     * @return The {@link NonQueriedUtensilAdapter.OnButtonsClickListener} implementation that
     * will run when a non-queried utensil's card buttons are clicked.
     */
    private NonQueriedUtensilAdapter.OnButtonsClickListener
    getOnNonQueriedUtensilButtonsClickListener() {
        return new NonQueriedUtensilAdapter.OnButtonsClickListener() {
            @Override
            public void onIncludeButtonClick(int utensilId, @NonNull Utensil utensil) {
                mNonQueriedUtensilAdapter.removeUtensil(utensilId, utensil);
                mIncludedUtensilAdapter.addUtensil(utensilId, utensil);
            }

            @Override
            public void onBanButtonClick(int utensilId, @NonNull Utensil utensil) {
                mNonQueriedUtensilAdapter.removeUtensil(utensilId, utensil);
                mBannedUtensilAdapter.addUtensil(utensilId, utensil);
            }
        };
    }
}
