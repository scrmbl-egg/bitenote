package app.bitenote.activities.query;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.bitenote.R;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.database.RecipeQuery;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user creates a query for filtering recipes.
 * @author Daniel N.
 */
public final class RecipeQueryActivity extends AppCompatActivity {
    /**
     * App's view model. Grants access to the database and shared live data.
     */
    private  BiteNoteViewModel viewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar materialToolbar;

    /**
     * Text input where the query's max budget is edited.
     */
    private EditText maxBudgetEditText;

    /**
     * Seek bar where the query's minimum diners are edited.
     */
    private SeekBar minDinersSeekBar;

    /**
     * Text view that indicates the progress of {@link #minDinersSeekBar}.
     */
    private TextView minDinersSeekBarProgressTextView;

    /**
     * Button that allows the user to go to the {@link IngredientQueryActivity} activity.
     */
    private Button editIngredientsButton;

    /**
     * Button that allows the user to go to the {@link UtensilQueryActivity} activity.
     */
    private Button editUtensilsButton;

    /**
     * Floating action button for viewing the query.
     */
    private FloatingActionButton viewQueryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_query_activity);

        /// init viewmodel
        viewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        /// post new query
        viewModel.postQuery(new RecipeQuery());

        setupViews();
    }

    private void setupViews() {
        materialToolbar = findViewById(R.id.RecipeQueryMaterialToolbar);
        maxBudgetEditText = findViewById(R.id.RecipeQueryMaxBudgetEditText);
        minDinersSeekBar = findViewById(R.id.RecipeQueryMinDinersSeekBar);
        minDinersSeekBarProgressTextView =
                findViewById(R.id.RecipeQueryMinDinersSeekBarProgressTextView);
        editIngredientsButton = findViewById(R.id.RecipeQueryEditIngredientsButton);
        editUtensilsButton = findViewById(R.id.RecipeQueryEditUtensilsButton);
        viewQueryButton = findViewById(R.id.RecipeQueryViewQueryButton);

        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        minDinersSeekBar.setOnSeekBarChangeListener(getOnMinDinersSeekBarChangeListener());

        editIngredientsButton.setOnClickListener(this::onEditIngredientsButtonClick);
        editUtensilsButton.setOnClickListener(this::onEditUtensilsButtonClick);
        viewQueryButton.setOnClickListener(this::onViewQueryButtonClick);

        /*
         * It is unnecessary to bind the views by observing the live data because a new query object
         * is created when creating this activity. This also allows for some hacks later in the
         */
        maxBudgetEditText.setText(""); // empty text, so we allow the users to NOT specify a budget

        /// set min diners seek bar
        minDinersSeekBar.setProgress(0);
        minDinersSeekBarProgressTextView.setText(String.valueOf(1));
    }

    /**
     * Function called when the {@link #editIngredientsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditIngredientsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, IngredientQueryActivity.class));
    }

    /**
     * Function called when the {@link #editUtensilsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditUtensilsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, UtensilQueryActivity.class));
    }

    /**
     * Function called when {@link #viewQueryButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onViewQueryButtonClick(@NonNull View view) {
        assert viewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery modifiedQuery = new RecipeQuery(viewModel.queryLiveData.getValue()) {{
            minDiners = minDinersSeekBar.getProgress() + 1;

            /*
             * If no budget is specified in the EditText, ignore it completely by setting
             * maxBudget to MAX_VALUE.
             */
            try {
                maxBudget = Integer.parseInt(maxBudgetEditText.getText().toString());
            } catch (NumberFormatException e) {
                maxBudget = Integer.MAX_VALUE;
            }
        }};

        viewModel.postQuery(modifiedQuery);

        startActivity(new Intent(this, ViewQueryActivity.class));
    }

    private SeekBar.OnSeekBarChangeListener getOnMinDinersSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                minDinersSeekBarProgressTextView.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }
}
