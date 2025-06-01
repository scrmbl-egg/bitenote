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
     * Adapter for ingredients that have been added to the recipe.
     */
    private AddedRecipeIngredientAdapter mAddedIngredientAdapter;

    /**
     * Recycler view for added ingredients.
     */
    private RecyclerView mAddedIngredientRecyclerView;

    /**
     * Adapter for ingredients that have not been added to the recipe.
     */
    private NonAddedRecipeIngredientAdapter mNonAddedRecipeIngredientAdapter;

    /**
     * Recycler view for non-added ingredients.
     */
    private RecyclerView mNonAddedIngredientRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton mSaveChangesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe_ingredients_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.EditRecipeIngredientsMaterialToolbar);
        mSaveChangesButton = findViewById(R.id.EditRecipeIngredientsSaveChangesButton);
        mAddedIngredientRecyclerView =
                findViewById(R.id.EditRecipeIngredientsAddedIngredientsRecyclerView);
        mNonAddedIngredientRecyclerView =
                findViewById(R.id.EditRecipeIngredientsNonAddedIngredientsRecyclerView);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        assert mViewModel.recipeLiveData.getValue() != null : "Recipe live data can't be null";

        final Recipe recipe = mViewModel.recipeLiveData.getValue().second;

        mDatabaseExecutor.execute(() -> {
            final List<Pair<Pair<Integer, Ingredient>, Ingredient.InRecipeProperties>>
                    addedIngredients =
                    mViewModel.sqliteHelper.getRecipeIngredientsWithProperties(recipe);
            final List<Pair<Integer, Ingredient>>
                    nonAddedIngredients =
                    mViewModel.sqliteHelper.getAllIngredientsExcept(recipe);

            mMainThreadHandler.post(() -> {
                mAddedIngredientAdapter = new AddedRecipeIngredientAdapter(
                        addedIngredients,
                        getOnAddedIngredientButtonsClickListener()
                );
                mNonAddedRecipeIngredientAdapter = new NonAddedRecipeIngredientAdapter(
                        nonAddedIngredients,
                        getOnNonAddedIngredientButtonsClickListener()
                );
                mAddedIngredientRecyclerView.setAdapter(mAddedIngredientAdapter);
                mNonAddedIngredientRecyclerView.setAdapter(mNonAddedRecipeIngredientAdapter);

                mAddedIngredientRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
                mNonAddedIngredientRecyclerView.setLayoutManager(
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
        assert mViewModel.recipeLiveData.getValue() != null : "Current recipe can't be null";

        final int id = mViewModel.recipeLiveData.getValue().first;
        final Recipe modifiedCopy = new Recipe(mViewModel.recipeLiveData.getValue().second) {{
            clearIngredients();

            mAddedIngredientAdapter.getIngredients().forEach(pair -> {
                if (pair.second.amount <= 0) return;

                putIngredient(pair.first.first, pair.second);
            });
        }};

        mDatabaseExecutor.execute(() -> {
            mViewModel.sqliteHelper.updateRecipe(id, modifiedCopy);

            mMainThreadHandler.post(() -> {
                mViewModel.postRecipe(modifiedCopy);

                Toast.makeText(
                        this,
                        R.string.ingredients_saved_toast,
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        });
    }

    /**
     * @return The {@link AddedRecipeIngredientAdapter.OnButtonClickListener} implementation that
     * will run when an added ingredient's card remove button is clicked.
     */
    private AddedRecipeIngredientAdapter.OnButtonClickListener
    getOnAddedIngredientButtonsClickListener() {
        return (ingredientId, ingredient, properties) -> {
            mAddedIngredientAdapter.removeIngredient(ingredientId, ingredient, properties);
            mNonAddedRecipeIngredientAdapter.addIngredient(ingredientId, ingredient);
        };
    }

    /**
     * @return The {@link NonAddedRecipeIngredientAdapter.OnButtonClickListener} implementation that
     * will run when a non-added ingredient's card add button is clicked.
     */
    private NonAddedRecipeIngredientAdapter.OnButtonClickListener
    getOnNonAddedIngredientButtonsClickListener() {
        return (ingredientId, ingredient) -> {
            mNonAddedRecipeIngredientAdapter.removeIngredient(ingredientId, ingredient);
            mAddedIngredientAdapter.addIngredient(ingredientId, ingredient);
        };
    }
}
