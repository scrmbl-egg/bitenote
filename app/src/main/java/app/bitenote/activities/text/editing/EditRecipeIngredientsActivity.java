package app.bitenote.activities.text.editing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.adapters.recipe.ingredient.AddedRecipeIngredientAdapter;
import app.bitenote.adapters.recipe.ingredient.NonAddedRecipeIngredientAdapter;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.instances.Ingredient;
import app.bitenote.instances.Recipe;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user edits the recipe's ingredients.
 */
public final class EditRecipeIngredientsActivity extends AppCompatActivity {
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
     * Adapter for ingredients that have been added to the recipe.
     */
    private AddedRecipeIngredientAdapter addedIngredientAdapter;

    /**
     * Recycler view for added ingredients.
     */
    private RecyclerView addedIngredientRecyclerView;

    /**
     * Adapter for ingredients that have not been added to the recipe.
     */
    private NonAddedRecipeIngredientAdapter nonAddedRecipeIngredientAdapter;

    /**
     * Recycler view for non-added ingredients.
     */
    private RecyclerView nonAddedIngredientRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton saveChangesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe_ingredients_activity);

        /// init viewmodel
        viewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        materialToolbar = findViewById(R.id.EditRecipeIngredientsMaterialToolbar);
        saveChangesButton = findViewById(R.id.EditRecipeIngredientsSaveChangesButton);
        addedIngredientRecyclerView =
                findViewById(R.id.EditRecipeIngredientsAddedIngredientsRecyclerView);
        nonAddedIngredientRecyclerView =
                findViewById(R.id.EditRecipeIngredientsNonAddedIngredientsRecyclerView);

        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        assert viewModel.recipeLiveData.getValue() != null : "Recipe live data can't be null";

        final Recipe recipe = viewModel.recipeLiveData.getValue().second;

        databaseExecutor.execute(() -> {
            final List<Pair<Pair<Integer, Ingredient>, Ingredient.InRecipeProperties>>
                    addedIngredients =
                    viewModel.sqliteHelper.getRecipeIngredientsWithProperties(recipe);
            final List<Pair<Integer, Ingredient>>
                    nonAddedIngredients =
                    viewModel.sqliteHelper.getAllIngredientsExcept(recipe);

            mainThreadHandler.post(() -> {
                addedIngredientAdapter = new AddedRecipeIngredientAdapter(
                        addedIngredients,
                        getOnAddedIngredientButtonsClickListener()
                );
                nonAddedRecipeIngredientAdapter = new NonAddedRecipeIngredientAdapter(
                        nonAddedIngredients,
                        getOnNonAddedIngredientButtonsClickListener()
                );
                addedIngredientRecyclerView.setAdapter(addedIngredientAdapter);
                nonAddedIngredientRecyclerView.setAdapter(nonAddedRecipeIngredientAdapter);

                addedIngredientRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                nonAddedIngredientRecyclerView.setLayoutManager(
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
        assert viewModel.recipeLiveData.getValue() != null : "Current recipe can't be null";

        final int id = viewModel.recipeLiveData.getValue().first;
        final Recipe modifiedCopy = new Recipe(viewModel.recipeLiveData.getValue().second) {{
            clearIngredients();

            addedIngredientAdapter.getIngredients().forEach(pair -> {
                if (pair.second.amount <= 0) return;

                putIngredient(pair.first.first, pair.second);
            });
        }};

        databaseExecutor.execute(() -> {
            viewModel.sqliteHelper.updateRecipe(id, modifiedCopy);

            mainThreadHandler.post(() -> {
                viewModel.postRecipe(modifiedCopy);

                Toast.makeText(
                        this,
                        R.string.ingredients_saved_toast,
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        });
    }

    private AddedRecipeIngredientAdapter.OnButtonClickListener
    getOnAddedIngredientButtonsClickListener() {
        return (ingredientId, ingredient, properties) -> {
            addedIngredientAdapter.removeIngredient(ingredientId, ingredient, properties);
            nonAddedRecipeIngredientAdapter.addIngredient(ingredientId, ingredient);
        };
    }

    private NonAddedRecipeIngredientAdapter.OnButtonClickListener
    getOnNonAddedIngredientButtonsClickListener() {
        return (ingredientId, ingredient) -> {
            nonAddedRecipeIngredientAdapter.removeIngredient(ingredientId, ingredient);
            addedIngredientAdapter.addIngredient(ingredientId, ingredient);
        };
    }
}
