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
     * Adapter for ingredients that are included in the query.
     */
    private IncludedIngredientAdapter includedIngredientAdapter;

    /**
     * Recycler view for included ingredients.
     */
    private RecyclerView includedIngredientsRecyclerView;

    /**
     * Adapter for ingredients that have been banned from the query.
     */
    private BannedIngredientAdapter bannedIngredientAdapter;

    /**
     * Recycler view for banned ingredients.
     */
    private RecyclerView bannedIngredientsRecyclerView;

    /**
     * Adapter for ingredients that have not been added to the query.
     */
    private NonQueriedIngredientAdapter nonQueriedIngredientAdapter;

    /**
     * Recycler view for non-queried ingredients.
     */
    private RecyclerView nonQueriedIngredientsRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton saveChangesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingredient_query_activity);

        /// init viewmodel
        viewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Initializes all the views in the activity.
     */
    private void setupViews() {
        materialToolbar = findViewById(R.id.IngredientQueryMaterialToolbar);
        saveChangesButton = findViewById(R.id.IngredientQuerySaveChangesButton);
        includedIngredientsRecyclerView =
                findViewById(R.id.IngredientQueryIncludedIngredientsRecyclerView);
        bannedIngredientsRecyclerView =
                findViewById(R.id.IngredientQueryBannedIngredientsRecyclerView);
        nonQueriedIngredientsRecyclerView =
                findViewById(R.id.IngredientQueryNonQueriedIngredientsRecyclerView);

        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        assert viewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery query = viewModel.queryLiveData.getValue();

        databaseExecutor.execute(() -> {
            final List<Pair<Integer, Ingredient>>
                    includedIngredients =
                    viewModel.sqliteHelper.getQueryIncludedIngredientsWithProperties(query);
            final List<Pair<Integer, Ingredient>>
                    bannedIngredients =
                    viewModel.sqliteHelper.getQueryBannedIngredientsWithProperties(query);
            final List<Pair<Integer, Ingredient>>
                    nonQueriedIngredients =
                    viewModel.sqliteHelper.getAllIngredientsExcept(query);

            mainThreadHandler.post(() -> {
                includedIngredientAdapter = new IncludedIngredientAdapter(
                        includedIngredients,
                        getOnIncludedIngredientButtonsClickListener()
                );
                bannedIngredientAdapter = new BannedIngredientAdapter(
                        bannedIngredients,
                        getOnBannedIngredientButtonsClickListener()
                );
                nonQueriedIngredientAdapter = new NonQueriedIngredientAdapter(
                        nonQueriedIngredients,
                        getOnNonQueriedIngredientButtonsClickListener()
                );
                includedIngredientsRecyclerView.setAdapter(includedIngredientAdapter);
                bannedIngredientsRecyclerView.setAdapter(bannedIngredientAdapter);
                nonQueriedIngredientsRecyclerView.setAdapter(nonQueriedIngredientAdapter);

                includedIngredientsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                bannedIngredientsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                nonQueriedIngredientsRecyclerView.setLayoutManager(
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
        assert viewModel.queryLiveData.getValue() != null : "Current query can't be null";

        final RecipeQuery modifiedCopy = new RecipeQuery(viewModel.queryLiveData.getValue()) {{
            clearAllIngredients();

            includedIngredientAdapter.getIngredients().forEach(pair ->
                    includeIngredient(pair.first, false)
            );

            bannedIngredientAdapter.getIngredients().forEach(pair ->
                    banIngredient(pair.first, false)
            );
        }};

        viewModel.postQuery(modifiedCopy);

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
                includedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                bannedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }

            @Override
            public void onRemoveButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                includedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                nonQueriedIngredientAdapter.addIngredient(ingredientId, ingredient);
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
                bannedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                includedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }

            @Override
            public void onRemoveButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                bannedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                nonQueriedIngredientAdapter.addIngredient(ingredientId, ingredient);
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
                nonQueriedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                includedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }

            @Override
            public void onBanButtonClick(int ingredientId, @NonNull Ingredient ingredient) {
                nonQueriedIngredientAdapter.removeIngredient(ingredientId, ingredient);
                bannedIngredientAdapter.addIngredient(ingredientId, ingredient);
            }
        };
    }
}
