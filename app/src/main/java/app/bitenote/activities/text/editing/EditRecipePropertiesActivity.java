package app.bitenote.activities.text.editing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.instances.Recipe;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user can edit the basic properties of a recipe.
 */
public final class EditRecipePropertiesActivity extends AppCompatActivity {
    /**
     * Activity executor that creates a background thread for database operations.
     */
    private final Executor databaseExecutor = Executors.newSingleThreadExecutor();

    /**
     * Activity's handler for the main thread.
     */
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Application view model. Grants access to the database.
     */
    private BiteNoteViewModel viewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar materialToolbar;

    /**
     * Text input where the recipe's budget is edited.
     */
    private EditText budgetEditText;

    /**
     * Seek bar where the recipe's diners are edited.
     */
    private SeekBar dinersSeekBar;

    /**
     * Text view that indicates the progress of {@link #dinersSeekBar}.
     */
    private TextView dinersSeekBarProgressTextView;

    /**
     * Button that allows the user to go to the {@link EditRecipeIngredientsActivity} activity.
     */
    private Button editIngredientsButton;

    /**
     * Button that allows the user to go to the {@link EditRecipeUtensilsActivity} activity.
     */
    private Button editUtensilsButton;

    /**
     * Floating button to save changes in the recipe's properties.
     */
    private FloatingActionButton saveChangesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe_properties_activity);

        /// init viewmodel
        viewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();

        /// bind to live data
        viewModel.recipeLiveData.observe(this, idRecipePair -> bind(idRecipePair.second));
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        materialToolbar = findViewById(R.id.EditRecipePropertiesMaterialToolbar);
        budgetEditText = findViewById(R.id.EditRecipePropertiesBudgetEditText);
        dinersSeekBar = findViewById(R.id.EditRecipePropertiesDinersSeekBar);
        dinersSeekBarProgressTextView =
                findViewById(R.id.EditRecipePropertiesDinersSeekBarProgressTextView);
        editIngredientsButton = findViewById(R.id.EditRecipePropertiesEditIngredientsButton);
        editUtensilsButton = findViewById(R.id.EditRecipePropertiesEditUtensilsButton);
        saveChangesButton = findViewById(R.id.EditRecipePropertiesSaveChangesButton);

        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        editIngredientsButton.setOnClickListener(this::onEditIngredientsButtonClick);
        editUtensilsButton.setOnClickListener(this::onEditUtensilsButtonClick);
        saveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Binds a recipe's data into the views of the activity.
     * @param recipe {@link Recipe} instance.
     */
    private void bind(@NonNull Recipe recipe) {
        budgetEditText.setText(String.valueOf(recipe.budget));

        /// set diners seek bar
        dinersSeekBar.setProgress(recipe.diners - 1);
        dinersSeekBar.setOnSeekBarChangeListener(getOnDinersSeekBarChangeListener());
        dinersSeekBarProgressTextView.setText(String.valueOf(recipe.diners));
    }

    /**
     * Function called when {@link #editIngredientsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditIngredientsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, EditRecipeIngredientsActivity.class));
    }

    /**
     * Function called when {@link #editUtensilsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditUtensilsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, EditRecipeUtensilsActivity.class));
    }

    /**
     * Function called when {@link #saveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        assert viewModel.recipeLiveData.getValue() != null : "Recipe live data can't be null";

        int id = viewModel.recipeLiveData.getValue().first;
        final Recipe modifiedCopy = new Recipe(viewModel.recipeLiveData.getValue().second) {{
            diners = dinersSeekBar.getProgress() + 1;

            try {
                budget = Integer.parseInt(budgetEditText.getText().toString());
            } catch (NumberFormatException e) {
                budget = 0; // set to 0 if edit text is empty
            }
        }};

        databaseExecutor.execute(() -> {
            viewModel.sqliteHelper.updateRecipe(id, modifiedCopy);

            mainThreadHandler.post(() -> {
                viewModel.postRecipeWithId(id, modifiedCopy);

                Toast.makeText(
                        this,
                        R.string.properties_saved_toast,
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        });
    }

    /**
     * @return The {@link SeekBar.OnSeekBarChangeListener} implementation that will run when
     * {@link #dinersSeekBar} is changed.
     */
    private SeekBar.OnSeekBarChangeListener getOnDinersSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                dinersSeekBarProgressTextView.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }
}
