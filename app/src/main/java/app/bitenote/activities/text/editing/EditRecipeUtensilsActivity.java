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
import app.bitenote.adapters.recipe.utensil.AddedRecipeUtensilAdapter;
import app.bitenote.adapters.recipe.utensil.NonAddedRecipeUtensilAdapter;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.instances.Recipe;
import app.bitenote.instances.Utensil;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user can edit the recipe's utensils.
 */
public final class EditRecipeUtensilsActivity extends AppCompatActivity {
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
     * Adapter for utensils that have been added to the recipe.
     */
    private AddedRecipeUtensilAdapter addedUtensilAdapter;

    /**
     * Recycler view for added utensils.
     */
    private RecyclerView addedUtensilsRecyclerView;

    /**
     * Adapter for utensils that have not been added to the recipe.
     */
    private NonAddedRecipeUtensilAdapter nonAddedUtensilAdapter;

    /**
     * Recycler view for non-added utensils.
     */
    private RecyclerView nonAddedUtensilsRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton saveChangesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe_utensils_activity);

        /// init viewmodel
        viewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        materialToolbar = findViewById(R.id.EditRecipeUtensilsMaterialToolbar);
        saveChangesButton = findViewById(R.id.EditRecipeUtensilsSaveChangesButton);
        addedUtensilsRecyclerView = findViewById(R.id.EditRecipeUtensilsAddedUtensilsRecyclerView);
        nonAddedUtensilsRecyclerView =
                findViewById(R.id.EditRecipeUtensilsNonAddedUtensilsRecyclerView);

        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        assert viewModel.recipeLiveData.getValue() != null : "Recipe live data can't be null";

        final Recipe recipe = viewModel.recipeLiveData.getValue().second;

        databaseExecutor.execute(() -> {
            final List<Pair<Integer, Utensil>>
                    addedUtensils =
                    viewModel.sqliteHelper.getRecipeUtensilsWithProperties(recipe);
            final List<Pair<Integer, Utensil>>
                    nonAddedUtensils =
                    viewModel.sqliteHelper.getAllUtensilsExcept(recipe);

            mainThreadHandler.post(() -> {
                addedUtensilAdapter = new AddedRecipeUtensilAdapter(
                        addedUtensils,
                        getOnAddedUtensilButtonClickListener()
                );
                nonAddedUtensilAdapter = new NonAddedRecipeUtensilAdapter(
                        nonAddedUtensils,
                        getOnNonAddedUtensilButtonClickListener()
                );
                addedUtensilsRecyclerView.setAdapter(addedUtensilAdapter);
                nonAddedUtensilsRecyclerView.setAdapter(nonAddedUtensilAdapter);

                addedUtensilsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                nonAddedUtensilsRecyclerView.setLayoutManager(
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
            clearUtensils();

            addedUtensilAdapter.getUtensils().forEach(pair -> addUtensil(pair.first));
        }};

        databaseExecutor.execute(() -> {
            viewModel.sqliteHelper.updateRecipe(id, modifiedCopy);

            mainThreadHandler.post(() -> {
                viewModel.postRecipe(modifiedCopy);
                Toast.makeText(
                        this,
                        R.string.utensils_saved_toast,
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        });
    }

    private AddedRecipeUtensilAdapter.OnButtonClickListener
    getOnAddedUtensilButtonClickListener() {
        return (utensilId, utensil) -> {
            addedUtensilAdapter.removeUtensil(utensilId, utensil);
            nonAddedUtensilAdapter.addUtensil(utensilId, utensil);
        };
    }

    private NonAddedRecipeUtensilAdapter.OnButtonClickListener
    getOnNonAddedUtensilButtonClickListener() {
        return (utensilId, utensil) -> {
            nonAddedUtensilAdapter.removeUtensil(utensilId, utensil);
            addedUtensilAdapter.addUtensil(utensilId, utensil);
        };
    }
}
