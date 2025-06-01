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
import app.bitenote.adapters.query.ingredient.IncludedIngredientAdapter;
import app.bitenote.adapters.query.ingredient.NonQueriedIngredientAdapter;
import app.bitenote.adapters.query.ingredient.BannedIngredientAdapter;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Ingredient;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user specifies which ingredients the recipes of the
 * query must have or must not have.
 * @author Daniel N.
 */
public final class IngredientQueryActivity extends AppCompatActivity {
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
     * Adapter for ingredients that are included in the query.
     */
    private IncludedIngredientAdapter mIncludedIngredientAdapter;

    /**
     * Recycler view for included ingredients.
     */
    private RecyclerView mIncludedIngredientsRecyclerView;

    /**
     * Adapter for ingredients that have been banned from the query.
     */
    private BannedIngredientAdapter mBannedIngredientAdapter;

    /**
     * Recycler view for banned ingredients.
     */
    private RecyclerView mBannedIngredientsRecyclerView;

    /**
     * Adapter for ingredients that have not been added to the query.
     */
    private NonQueriedIngredientAdapter mNonQueriedIngredientAdapter;

    /**
     * Recycler view for non-queried ingredients.
     */
    private RecyclerView mNonQueriedIngredientsRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton mSaveChangesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingredient_query_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Initializes all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.IngredientQueryMaterialToolbar);
        mSaveChangesButton = findViewById(R.id.IngredientQuerySaveChangesButton);
        mIncludedIngredientsRecyclerView =
                findViewById(R.id.IngredientQueryIncludedIngredientsRecyclerView);
        mBannedIngredientsRecyclerView =
                findViewById(R.id.IngredientQueryBannedIngredientsRecyclerView);
        mNonQueriedIngredientsRecyclerView =
                findViewById(R.id.IngredientQueryNonQueriedIngredientsRecyclerView);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        assert mViewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery query = mViewModel.queryLiveData.getValue();

        mDatabaseExecutor.execute(() -> {
            final List<Pair<Integer, Ingredient>>
                    includedIngredients =
                    mViewModel.sqliteHelper.getQueryIncludedIngredientsWithProperties(query);
            final List<Pair<Integer, Ingredient>>
                    bannedIngredients =
                    mViewModel.sqliteHelper.getQueryBannedIngredientsWithProperties(query);
            final List<Pair<Integer, Ingredient>>
                    nonQueriedIngredients =
                    mViewModel.sqliteHelper.getAllIngredientsExcept(query);

            mMainThreadHandler.post(() -> {
                mIncludedIngredientAdapter = new IncludedIngredientAdapter(
                        includedIngredients,
                        getOnIncludedIngredientButtonsClickListener()
                );
                mBannedIngredientAdapter = new BannedIngredientAdapter(
                        bannedIngredients,
                        getOnBannedIngredientButtonsClickListener()
                );
                mNonQueriedIngredientAdapter = new NonQueriedIngredientAdapter(
                        nonQueriedIngredients,
                        getOnNonQueriedIngredientButtonsClickListener()
                );
                mIncludedIngredientsRecyclerView.setAdapter(mIncludedIngredientAdapter);
                mBannedIngredientsRecyclerView.setAdapter(mBannedIngredientAdapter);
                mNonQueriedIngredientsRecyclerView.setAdapter(mNonQueriedIngredientAdapter);

                mIncludedIngredientsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                mBannedIngredientsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                mNonQueriedIngredientsRecyclerView.setLayoutManager(
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
        assert mViewModel.queryLiveData.getValue() != null : "Current query can't be null";

        final RecipeQuery modifiedCopy = new RecipeQuery(mViewModel.queryLiveData.getValue()) {{
            clearAllIngredients();

            mIncludedIngredientAdapter.getIngredients().forEach(pair ->
                    includeIngredient(pair.first, false)
            );

            mBannedIngredientAdapter.getIngredients().forEach(pair ->
                    banIngredient(pair.first, false)
            );
        }};

        mViewModel.postQuery(modifiedCopy);

        Toast.makeText(
                this,
                R.string.ingredients_saved_toast,
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    /**
     * @return The {@link IncludedIngredientAdapter.OnButtonsClickListener} implementation that
     * will run when an included ingredient's card buttons are clicked.
     */
    private IncludedIngredientAdapter.OnButtonsClickListener
    getOnIncludedIngredientButtonsClickListener() {
        return new IncludedIngredientAdapter.OnButtonsClickListener() {
            @Override
            public void onBanButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                mIncludedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                mBannedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }

            @Override
            public void onRemoveButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                mIncludedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                mNonQueriedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }
        };
    }

    /**
     * @return The {@link BannedIngredientAdapter.OnButtonsClickListener} implementation that
     * will run when a banned ingredient's card buttons are clicked.
     */
    private BannedIngredientAdapter.OnButtonsClickListener
    getOnBannedIngredientButtonsClickListener() {
        return new BannedIngredientAdapter.OnButtonsClickListener() {
            @Override
            public void onIncludeButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                mBannedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                mIncludedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }

            @Override
            public void onRemoveButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                mBannedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                mNonQueriedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }
        };
    }

    /**
     * @return The {@link NonQueriedIngredientAdapter.OnButtonsClickListener} implementation that
     * will run when a non-queried ingredient's card buttons are clicked.
     */
    private NonQueriedIngredientAdapter.OnButtonsClickListener
    getOnNonQueriedIngredientButtonsClickListener() {
        return new NonQueriedIngredientAdapter.OnButtonsClickListener() {
            @Override
            public void onIncludeButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                mNonQueriedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                mIncludedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }

            @Override
            public void onBanButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                mNonQueriedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                mBannedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }
        };
    }
}
