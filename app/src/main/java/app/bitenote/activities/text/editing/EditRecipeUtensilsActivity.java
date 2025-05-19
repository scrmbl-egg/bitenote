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
import app.bitenote.adapters.recipe.utensil.AddedRecipeUtensilAdapter;
import app.bitenote.adapters.recipe.utensil.NonAddedRecipeUtensilAdapter;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user can edit the recipe's utensils.
 */
public final class EditRecipeUtensilsActivity extends AppCompatActivity {
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
     * todo: make docs
     */
    private RecyclerView addedUtensilRecyclerView;

    /**
     * Adapter for utensils that have not been added to the recipe.
     */
    private NonAddedRecipeUtensilAdapter nonAddedUtensilAdapter;

    /**
     * todo: make docs
     */
    private RecyclerView nonAddedUtensilRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton saveChangesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe_utensils_activity);

        /// init viewmodel
        final ViewModelProvider.Factory factory =
                new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(BiteNoteViewModel.class);

        /// init views
        materialToolbar = findViewById(R.id.EditRecipeUtensilsMaterialToolbar);
        saveChangesButton = findViewById(R.id.EditRecipeUtensilsSaveChangesButton);
        addedUtensilRecyclerView = findViewById(R.id.EditRecipeUtensilsAddedUtensilsRecyclerView);
        nonAddedUtensilRecyclerView =
                findViewById(R.id.EditRecipeUtensilsNonAddedUtensilsRecyclerView);

        /// set material toolbar
        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        /// set recycler views, adapters and click listeners
        addedUtensilRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addedUtensilAdapter = new AddedRecipeUtensilAdapter(
                viewModel.sqliteHelper.getRecipeUtensilsWithProperties(
                        WriteRecipeActivity.currentRecipeData
                ),
                getOnAddedUtensilButtonClickListener()
        );
        addedUtensilRecyclerView.setAdapter(addedUtensilAdapter);

        nonAddedUtensilRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nonAddedUtensilAdapter = new NonAddedRecipeUtensilAdapter(
                viewModel.sqliteHelper.getAllUtensilsExcept(
                        WriteRecipeActivity.currentRecipeData
                ),
                getOnNonAddedUtensilButtonClickListener()
        );
        nonAddedUtensilRecyclerView.setAdapter(nonAddedUtensilAdapter);

        /// set save changes button
        saveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Function called when {@link #saveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        WriteRecipeActivity.currentRecipeData.clearUtensils();

        addedUtensilAdapter.getUtensils().forEach(pair ->
                WriteRecipeActivity.currentRecipeData.addUtensil(pair.first)
        );

        Toast.makeText(this, "utensils saved", Toast.LENGTH_SHORT).show();
        // todo: use translated string

        finish();
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
