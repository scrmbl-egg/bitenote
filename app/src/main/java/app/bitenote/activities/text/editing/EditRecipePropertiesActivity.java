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
    private final Executor mDatabaseExecutor = Executors.newSingleThreadExecutor();

    /**
     * Activity's handler for the main thread.
     */
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Application view model. Grants access to the database.
     */
    private BiteNoteViewModel mViewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar mMaterialToolbar;

    /**
     * Text input where the recipe's budget is edited.
     */
    private EditText mBudgetEditText;

    /**
     * Seek bar where the recipe's diners are edited.
     */
    private SeekBar mDinersSeekBar;

    /**
     * Text view that indicates the progress of {@link #mDinersSeekBar}.
     */
    private TextView mDinersSeekBarProgressTextView;

    /**
     * Button that allows the user to go to the {@link EditRecipeIngredientsActivity} activity.
     */
    private Button mEditIngredientsButton;

    /**
     * Button that allows the user to go to the {@link EditRecipeUtensilsActivity} activity.
     */
    private Button mEditUtensilsButton;

    /**
     * Floating button to save changes in the recipe's properties.
     */
    private FloatingActionButton mSaveChangesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe_properties_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.EditRecipePropertiesMaterialToolbar);
        mBudgetEditText = findViewById(R.id.EditRecipePropertiesBudgetEditText);
        mDinersSeekBar = findViewById(R.id.EditRecipePropertiesDinersSeekBar);
        mDinersSeekBarProgressTextView =
                findViewById(R.id.EditRecipePropertiesDinersSeekBarProgressTextView);
        mEditIngredientsButton = findViewById(R.id.EditRecipePropertiesEditIngredientsButton);
        mEditUtensilsButton = findViewById(R.id.EditRecipePropertiesEditUtensilsButton);
        mSaveChangesButton = findViewById(R.id.EditRecipePropertiesSaveChangesButton);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        assert mViewModel.recipeLiveData.getValue() != null : "Recipe live data can't be null";

        final Recipe recipe = mViewModel.recipeLiveData.getValue().second;

        bind(recipe); // bind recipe content to views just once

        mEditIngredientsButton.setOnClickListener(this::onEditIngredientsButtonClick);
        mEditUtensilsButton.setOnClickListener(this::onEditUtensilsButtonClick);
        mSaveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Binds a recipe's data into the views of the activity.
     * @param recipe {@link Recipe} instance.
     */
    private void bind(@NonNull Recipe recipe) {
        mBudgetEditText.setText(String.valueOf(recipe.budget));

        /// set diners seek bar
        mDinersSeekBar.setProgress(recipe.diners - 1);
        mDinersSeekBar.setOnSeekBarChangeListener(getOnDinersSeekBarChangeListener());
        mDinersSeekBarProgressTextView.setText(String.valueOf(recipe.diners));
    }

    /**
     * Function called when {@link #mEditIngredientsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditIngredientsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, EditRecipeIngredientsActivity.class));
    }

    /**
     * Function called when {@link #mEditUtensilsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditUtensilsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, EditRecipeUtensilsActivity.class));
    }

    /**
     * Function called when {@link #mSaveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        assert mViewModel.recipeLiveData.getValue() != null : "Recipe live data can't be null";

        int id = mViewModel.recipeLiveData.getValue().first;
        final Recipe modifiedCopy = new Recipe(mViewModel.recipeLiveData.getValue().second) {{
            diners = mDinersSeekBar.getProgress() + 1;

            try {
                budget = Integer.parseInt(mBudgetEditText.getText().toString());
            } catch (NumberFormatException e) {
                budget = 0; // set to 0 if edit text is empty
            }
        }};

        mDatabaseExecutor.execute(() -> {
            mViewModel.sqliteHelper.updateRecipe(id, modifiedCopy);

            mMainThreadHandler.post(() -> {
                mViewModel.postRecipeWithId(id, modifiedCopy);

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
     * {@link #mDinersSeekBar} is changed.
     */
    private SeekBar.OnSeekBarChangeListener getOnDinersSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mDinersSeekBarProgressTextView.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }
}
