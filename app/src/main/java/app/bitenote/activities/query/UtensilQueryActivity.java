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
    private final Executor databaseExecutor = Executors.newSingleThreadExecutor();

    /**
     * Activity's handler for the main thread.
     */
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Application view model. Grants access to the app's database.
     */
    private BiteNoteViewModel viewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar materialToolbar;

    /**
     * Adapter for utensils that have been included in the query.
     */
    private IncludedUtensilAdapter includedUtensilAdapter;

    /**
     * Recycler view for included utensils.
     */
    private RecyclerView includedUtensilsRecyclerView;

    /**
     * Adapter for utensils that have been banned in the query.
     */
    private BannedUtensilAdapter bannedUtensilAdapter;

    /**
     * Recycler view for banned utensils.
     */
    private RecyclerView bannedUtensilsRecyclerView;

    /**
     * Adapter for utensils that have not been added to the query.
     */
    private NonQueriedUtensilAdapter nonQueriedUtensilAdapter;

    /**
     * Recycler view for non-queried utensils.
     */
    private RecyclerView nonQueriedUtensilRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton saveChangesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.utensil_query_activity);

        /// init viewmodel
        viewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Initializes all the views in the activity.
     */
    private void setupViews() {
        materialToolbar = findViewById(R.id.UtensilQueryMaterialToolbar);
        saveChangesButton = findViewById(R.id.UtensilQuerySaveChangesButton);
        includedUtensilsRecyclerView =
                findViewById(R.id.UtensilQueryIncludedUtensilsRecyclerView);
        bannedUtensilsRecyclerView =
                findViewById(R.id.UtensilQueryBannedUtensilsRecyclerView);
        nonQueriedUtensilRecyclerView =
                findViewById(R.id.UtensilQueryNonQueriedUtensilsRecyclerView);

        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        assert viewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery query = viewModel.queryLiveData.getValue();

        databaseExecutor.execute(() -> {
            final List<Pair<Integer, Utensil>>
                    includedUtensils =
                    viewModel.sqliteHelper.getQueryIncludedUtensilsWithProperties(query);
            final List<Pair<Integer, Utensil>>
                    bannedUtensils =
                    viewModel.sqliteHelper.getQueryBannedUtensilsWithProperties(query);
            final List<Pair<Integer, Utensil>>
                    nonQueriedUtensils =
                    viewModel.sqliteHelper.getAllUtensilsExcept(query);

            mainThreadHandler.post(() -> {
                includedUtensilAdapter = new IncludedUtensilAdapter(
                        includedUtensils,
                        getOnIncludedUtensilButtonsClickListener()
                );
                bannedUtensilAdapter = new BannedUtensilAdapter(
                        bannedUtensils,
                        getOnBannedUtensilButtonsClickListener()
                );
                nonQueriedUtensilAdapter = new NonQueriedUtensilAdapter(
                        nonQueriedUtensils,
                        getOnNonQueriedUtensilButtonsClickListener()
                );
                includedUtensilsRecyclerView.setAdapter(includedUtensilAdapter);
                bannedUtensilsRecyclerView.setAdapter(bannedUtensilAdapter);
                nonQueriedUtensilRecyclerView.setAdapter(nonQueriedUtensilAdapter);

                includedUtensilsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                bannedUtensilsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                nonQueriedUtensilRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
            });
        });

        saveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Function called when {@link #saveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        assert viewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery modifiedCopy = new RecipeQuery(viewModel.queryLiveData.getValue()) {{
            clearAllUtensils();

            includedUtensilAdapter.getUtensils().forEach(pair ->
                    includeUtensil(pair.first, true)
            );
            bannedUtensilAdapter.getUtensils().forEach(pair ->
                    banUtensil(pair.first, true)
            );
        }};

        viewModel.postQuery(modifiedCopy);

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
                includedUtensilAdapter.removeUtensil(utensilId, utensil);
                bannedUtensilAdapter.addUtensil(utensilId, utensil);
            }

            @Override
            public void onRemoveButtonClick(int utensilId, @NonNull Utensil utensil) {
                includedUtensilAdapter.removeUtensil(utensilId, utensil);
                nonQueriedUtensilAdapter.addUtensil(utensilId, utensil);
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
                bannedUtensilAdapter.removeUtensil(utensilId, utensil);
                includedUtensilAdapter.addUtensil(utensilId, utensil);
            }

            @Override
            public void onRemoveButtonClick(int utensilId, @NonNull Utensil utensil) {
                bannedUtensilAdapter.removeUtensil(utensilId, utensil);
                nonQueriedUtensilAdapter.addUtensil(utensilId, utensil);
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
                nonQueriedUtensilAdapter.removeUtensil(utensilId, utensil);
                includedUtensilAdapter.addUtensil(utensilId, utensil);
            }

            @Override
            public void onBanButtonClick(int utensilId, @NonNull Utensil utensil) {
                nonQueriedUtensilAdapter.removeUtensil(utensilId, utensil);
                bannedUtensilAdapter.addUtensil(utensilId, utensil);
            }
        };
    }
}
