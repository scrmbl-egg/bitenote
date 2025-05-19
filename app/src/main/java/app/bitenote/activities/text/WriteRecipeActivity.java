package app.bitenote.activities.text;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Optional;

import app.bitenote.R;
import app.bitenote.activities.text.editing.EditRecipePropertiesActivity;
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
     * The data of the recipe being edited.
     * @implNote This field is {@code static} because the reference needs to be accessed from other
     * activities that mutate it, and passing an ID, or a {@link android.os.Parcelable} will only
     * create copies. Since only one recipe can be edited at a time, this should not be a problem.
     */
    public static Recipe currentRecipeData;

    /**
     * The integer ID of the recipe being edited.
     */
    private static int currentRecipeId;

    /**
     * Application view model. Grants access to the app's database.
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

        /// init views
        materialToolbar = findViewById(R.id.WriteRecipeMaterialToolbar);
        nameEditText = findViewById(R.id.WriteRecipeInputNameTextView);
        bodyEditText = findViewById(R.id.WriteRecipeInputBodyTextView);
        saveChangesButton = findViewById(R.id.WriteRecipeSaveChangesButton);
        propertiesButton = findViewById(R.id.WriteRecipeEditPropertiesButton);

        /// set toolbar
        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        /// init view model
        viewModel = new ViewModelProvider(this).get(BiteNoteViewModel.class);

        /// get recipe data, and bind
        currentRecipeId = getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0);
        final boolean isNewRecipe = currentRecipeId == 0;
        if (isNewRecipe) {
            /// create new instance, insert into database, and reassign id
            currentRecipeData = new Recipe();
            currentRecipeId = viewModel.sqliteHelper.insertRecipe(currentRecipeData);
        } else {
            final Optional<Recipe> recipeOption =
                    viewModel.sqliteHelper.getRecipeFromId(currentRecipeId);

            recipeOption.ifPresent(recipe -> currentRecipeData = recipe);
        }
        bindRecipeData();

        /// set properties button
        propertiesButton.setOnClickListener(this::onPropertiesButtonClick);

        /// set save changes button
        saveChangesButton.setOnClickListener(this::onSaveButtonClick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindRecipeData();
    }

    /**
     * Binds the data contained in {@link WriteRecipeActivity#currentRecipeData} to the text views.
     */
    private void bindRecipeData() {
        nameEditText.setText(currentRecipeData.name);
        bodyEditText.setText(currentRecipeData.body);
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
    private void onSaveButtonClick(@NonNull View view) {
        currentRecipeData.name = nameEditText.getText().toString();
        currentRecipeData.body = bodyEditText.getText().toString();

        viewModel.sqliteHelper.updateRecipe(currentRecipeId, currentRecipeData);

        finish();
    }
}
