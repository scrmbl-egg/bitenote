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
    private final Executor mDatabaseExecutor = Executors.newSingleThreadExecutor();

    /**
     * Activity's handler for the main thread.
     */
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Application view model. Grants access to the app's database and shared data.
     */
    private BiteNoteViewModel mViewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar mMaterialToolbar;

    /**
     * Save changes button.
     */
    private FloatingActionButton mSaveChangesButton;

    /**
     * Edit properties button.
     */
    private FloatingActionButton mPropertiesButton;

    /**
     * Text view where the user can input and edit the recipe's name;
     */
    private EditText mNameEditText;

    /**
     * Text view where the user can input and edit the recipe's body;
     */
    private EditText mBodyEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_recipe_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();

        /*
         * NOTE: The data from the currently edited recipe is assigned in this activity and not in
         * ReadRecipeActivity because that activity is not started when a new recipe is created.
         */
        loadData(getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0));

        mViewModel.recipeLiveData.observe(this, pair -> bind(pair.second));
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.WriteRecipeMaterialToolbar);
        mNameEditText = findViewById(R.id.WriteRecipeInputNameTextView);
        mBodyEditText = findViewById(R.id.WriteRecipeInputBodyTextView);
        mSaveChangesButton = findViewById(R.id.WriteRecipeSaveChangesButton);
        mPropertiesButton = findViewById(R.id.WriteRecipeEditPropertiesButton);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        mPropertiesButton.setOnClickListener(this::onPropertiesButtonClick);
        mSaveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
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
            mDatabaseExecutor.execute(() -> {
                final int newId = mViewModel.sqliteHelper.insertRecipe(newRecipe);

                mMainThreadHandler.post(() -> mViewModel.postRecipeWithId(newId, newRecipe));
            });
        } else {
            mDatabaseExecutor.execute(() -> {
                final Optional<Recipe> recipeOption = mViewModel.sqliteHelper.getRecipeFromId(id);

                recipeOption.ifPresent(recipe ->
                        mMainThreadHandler.post(() -> {
                            mViewModel.postRecipeWithId(id, recipe);
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
        mNameEditText.setText(recipe.name);
        mBodyEditText.setText(recipe.body);
    }

    /**
     * Function called when {@link #mPropertiesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onPropertiesButtonClick(@NonNull View view) {
        startActivity(new Intent(this, EditRecipePropertiesActivity.class));
    }

    /**
     * Function called when {@link #mSaveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        assert mViewModel.recipeLiveData.getValue() != null : "Recipe live data value can't be null";

        final Integer recipeId = mViewModel.recipeLiveData.getValue().first;
        final Recipe recipe  = mViewModel.recipeLiveData.getValue().second;

        final Recipe modifiedCopy = new Recipe(recipe) {{
            name = mNameEditText.getText().toString().trim();
            body = mBodyEditText.getText().toString().trim();
        }};

        mDatabaseExecutor.execute(() -> {
            mViewModel.sqliteHelper.updateRecipe(recipeId, modifiedCopy);

            mMainThreadHandler.post(() -> {
                mViewModel.postRecipe(modifiedCopy);

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
