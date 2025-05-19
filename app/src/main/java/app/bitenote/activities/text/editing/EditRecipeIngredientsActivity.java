package app.bitenote.activities.text.editing;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.bitenote.R;
import app.bitenote.activities.text.WriteRecipeActivity;
import app.bitenote.adapters.recipe.ingredient.AddedRecipeIngredientAdapter;
import app.bitenote.adapters.recipe.ingredient.NonAddedRecipeIngredientAdapter;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user edits the recipe's ingredients.
 */
public final class EditRecipeIngredientsActivity extends AppCompatActivity {
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

    private RecyclerView addedIngredientRecyclerView;

    /**
     * Adapter for ingredients that have not been added to the recipe.
     */
    private NonAddedRecipeIngredientAdapter nonAddedRecipeIngredientAdapter;

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
        final ViewModelProvider.Factory factory =
                new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(BiteNoteViewModel.class);

        /// init views
        materialToolbar = findViewById(R.id.EditRecipeIngredientsMaterialToolbar);
        saveChangesButton = findViewById(R.id.EditRecipeIngredientsSaveChangesButton);
        addedIngredientRecyclerView =
                findViewById(R.id.EditRecipeIngredientsAddedIngredientsRecyclerView);
        nonAddedIngredientRecyclerView =
                findViewById(R.id.EditRecipeIngredientsNonAddedIngredientsRecyclerView);

        /// set material toolbar
        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        /// set recycler views, adapters, and click listeners
        addedIngredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addedIngredientAdapter = new AddedRecipeIngredientAdapter(
                viewModel.sqliteHelper.getRecipeIngredientsWithProperties(
                        WriteRecipeActivity.currentRecipeData
                ),
                getOnAddedIngredientButtonsClickListener()
        );
        addedIngredientRecyclerView.setAdapter(addedIngredientAdapter);

        nonAddedIngredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nonAddedRecipeIngredientAdapter = new NonAddedRecipeIngredientAdapter(
                viewModel.sqliteHelper.getAllIngredientsExcept(
                        WriteRecipeActivity.currentRecipeData
                ),
                getOnNonAddedIngredientButtonsClickListener()
        );
        nonAddedIngredientRecyclerView.setAdapter(nonAddedRecipeIngredientAdapter);

        /// set save changes button
        saveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Function called when {@link #saveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        WriteRecipeActivity.currentRecipeData.clearIngredients();

        addedIngredientAdapter.getIngredients().forEach(pair ->
                WriteRecipeActivity.currentRecipeData.putIngredient(pair.first.first, pair.second)
        );

        Toast.makeText(this, "ingredients saved", Toast.LENGTH_SHORT).show();
        // todo: used translated string

        finish();
    }

    private AddedRecipeIngredientAdapter.OnButtonsClickListener
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
