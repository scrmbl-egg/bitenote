package app.bitenote.activities.text;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.activities.text.editing.EditRecipePropertiesActivity;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.instances.Recipe;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user edits or writes a recipe.
 * @author Daniel N.
 */
public final class WriteRecipeActivity extends AppCompatActivity {
    /**
     * Name of the {@link Intent} extra that holds the integer recipe ID.
     */
    public static final String INTENT_EXTRA_RECIPE_ID = "recipe_id";

    /**
     * Activity executor that creates a background thread for database operations.
     */
    private final Executor databaseExecutor = Executors.newSingleThreadExecutor();

    /**
     * Activity's handler for the main thread.
     */
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Application view model. Grants access to the app's database and shared data.
     */
    private BiteNoteViewModel viewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar materialToolbar;

    /**
     * Save changes button.
     */
    private FloatingActionButton saveChangesButton;

    /**
     * Edit properties button.
     */
    private FloatingActionButton propertiesButton;

    /**
     * Text view where the user can input and edit the recipe's name;
     */
    private EditText nameEditText;

    /**
     * Text view where the user can input and edit the recipe's body;
     */
    private EditText bodyEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_recipe_activity);

        /// init viewmodel
        viewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();

        /*
         * NOTE: The data from the currently edited recipe is assigned in this activity and not in
         * ReadRecipeActivity because that activity is not started when a new recipe is created.
         */
        loadData(getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0));

        viewModel.recipeLiveData.observe(this, pair -> bind(pair.second));
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        materialToolbar = findViewById(R.id.WriteRecipeMaterialToolbar);
        nameEditText = findViewById(R.id.WriteRecipeInputNameTextView);
        bodyEditText = findViewById(R.id.WriteRecipeInputBodyTextView);
        saveChangesButton = findViewById(R.id.WriteRecipeSaveChangesButton);
        propertiesButton = findViewById(R.id.WriteRecipeEditPropertiesButton);

        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        propertiesButton.setOnClickListener(this::onPropertiesButtonClick);
        saveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Loads the necessary recipe's data and binds it to the activity's views.
     * @param id ID of the recipe in the database. If {@code 0} is passed, it means that a new
     * recipe is being created and edited by the user.
     */
    private void loadData(int id) {
        final boolean isNewRecipe = id == 0;
        if (isNewRecipe) {
            /// set new instance, insert into database, and reassign id
            final Recipe newRecipe = new Recipe();
            databaseExecutor.execute(() -> {
                final int newId = viewModel.sqliteHelper.insertRecipe(newRecipe);

                mainThreadHandler.post(() -> viewModel.postRecipeWithId(newId, newRecipe));
            });
        } else {
            databaseExecutor.execute(() -> {
                final Optional<Recipe> recipeOption = viewModel.sqliteHelper.getRecipeFromId(id);

                recipeOption.ifPresent(recipe ->
                        mainThreadHandler.post(() -> {
                            viewModel.postRecipeWithId(id, recipe);
                        })
                );
            });
        }
    }

    /**
     * Binds a recipe's data into the text views of the activity.
     * @param recipe {@link Recipe} instance.
     */
    private void bind(@NonNull Recipe recipe) {
        nameEditText.setText(recipe.name);
        bodyEditText.setText(recipe.body);
    }

    /**
     * Function called when {@link #propertiesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onPropertiesButtonClick(@NonNull View view) {
        startActivity(new Intent(this, EditRecipePropertiesActivity.class));
    }

    /**
     * Function called when {@link #saveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        assert viewModel.recipeLiveData.getValue() != null : "Recipe live data value can't be null";

        final Integer recipeId = viewModel.recipeLiveData.getValue().first;
        final Recipe recipe  = viewModel.recipeLiveData.getValue().second;

        final Recipe modifiedCopy = new Recipe(recipe) {{
            name = nameEditText.getText().toString().trim();
            body = bodyEditText.getText().toString().trim();
        }};

        databaseExecutor.execute(() -> {
            viewModel.sqliteHelper.updateRecipe(recipeId, modifiedCopy);

            mainThreadHandler.post(() -> {
                viewModel.postRecipe(modifiedCopy);

                Toast.makeText(
                        this,
                        getString(R.string.recipe_saved_toast, modifiedCopy.name),
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        });
    }
}
