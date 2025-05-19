package app.bitenote.activities.text.editing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.bitenote.R;
import app.bitenote.activities.text.WriteRecipeActivity;

/**
 * Class that represents the activity where the user can edit the basic properties of a recipe.
 */
public final class EditRecipePropertiesActivity extends AppCompatActivity {
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

        /// init views
        materialToolbar = findViewById(R.id.EditRecipePropertiesMaterialToolbar);
        budgetEditText = findViewById(R.id.EditRecipePropertiesBudgetEditText);
        dinersSeekBar = findViewById(R.id.EditRecipePropertiesDinersSeekBar);
        dinersSeekBarProgressTextView =
                findViewById(R.id.EditRecipePropertiesDinersSeekBarProgressTextView);
        editIngredientsButton = findViewById(R.id.EditRecipePropertiesEditIngredientsButton);
        editUtensilsButton = findViewById(R.id.EditRecipePropertiesEditUtensilsButton);
        saveChangesButton = findViewById(R.id.EditRecipePropertiesSaveChangesButton);

        /// set toolbar
        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        /// set budget edit text
        budgetEditText.setText(String.valueOf(WriteRecipeActivity.currentRecipeData.budget));

        /// set diners seek bar
        dinersSeekBar.setProgress(WriteRecipeActivity.currentRecipeData.diners - 1);
        dinersSeekBar.setOnSeekBarChangeListener(getOnDinersSeekBarChangeListener());
        dinersSeekBarProgressTextView.setText(
                String.valueOf(WriteRecipeActivity.currentRecipeData.diners)
        );

        /// set edit ingredients button
        editIngredientsButton.setOnClickListener(this::onEditIngredientsButtonClick);

        /// set edit utensils button
        editUtensilsButton.setOnClickListener(this::onEditUtensilsButtonClick);

        /// set save button
        saveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
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
        WriteRecipeActivity.currentRecipeData.budget =
                Integer.parseInt(budgetEditText.getText().toString());
        WriteRecipeActivity.currentRecipeData.diners = dinersSeekBar.getProgress() + 1;

        finish();
    }

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
