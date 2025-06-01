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
    private  BiteNoteViewModel mViewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar mMaterialToolbar;

    /**
     * Text input where the query's max budget is edited.
     */
    private EditText mMaxBudgetEditText;

    /**
     * Seek bar where the query's minimum diners are edited.
     */
    private SeekBar mMinDinersSeekBar;

    /**
     * Text view that indicates the progress of {@link #mMinDinersSeekBar}.
     */
    private TextView mMinDinersSeekBarProgressTextView;

    /**
     * Button that allows the user to go to the {@link IngredientQueryActivity} activity.
     */
    private Button mEditIngredientsButton;

    /**
     * Button that allows the user to go to the {@link UtensilQueryActivity} activity.
     */
    private Button mEditUtensilsButton;

    /**
     * Floating action button for viewing the query.
     */
    private FloatingActionButton mViewQueryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_query_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        /// post new query
        mViewModel.postQuery(new RecipeQuery());

        setupViews();
    }

    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.RecipeQueryMaterialToolbar);
        mMaxBudgetEditText = findViewById(R.id.RecipeQueryMaxBudgetEditText);
        mMinDinersSeekBar = findViewById(R.id.RecipeQueryMinDinersSeekBar);
        mMinDinersSeekBarProgressTextView =
                findViewById(R.id.RecipeQueryMinDinersSeekBarProgressTextView);
        mEditIngredientsButton = findViewById(R.id.RecipeQueryEditIngredientsButton);
        mEditUtensilsButton = findViewById(R.id.RecipeQueryEditUtensilsButton);
        mViewQueryButton = findViewById(R.id.RecipeQueryViewQueryButton);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        mMinDinersSeekBar.setOnSeekBarChangeListener(getOnMinDinersSeekBarChangeListener());

        mEditIngredientsButton.setOnClickListener(this::onEditIngredientsButtonClick);
        mEditUtensilsButton.setOnClickListener(this::onEditUtensilsButtonClick);
        mViewQueryButton.setOnClickListener(this::onViewQueryButtonClick);

        /*
         * It is unnecessary to bind the views by observing the live data because a new query object
         * is created when creating this activity. This also allows for some hacks later in the
         */
        mMaxBudgetEditText.setText(""); // empty text, so we allow the users to NOT specify a budget

        /// set min diners seek bar
        mMinDinersSeekBar.setProgress(0);
        mMinDinersSeekBarProgressTextView.setText(String.valueOf(1));
    }

    /**
     * Function called when the {@link #mEditIngredientsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditIngredientsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, IngredientQueryActivity.class));
    }

    /**
     * Function called when the {@link #mEditUtensilsButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditUtensilsButtonClick(@NonNull View view) {
        startActivity(new Intent(this, UtensilQueryActivity.class));
    }

    /**
     * Function called when {@link #mViewQueryButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onViewQueryButtonClick(@NonNull View view) {
        assert mViewModel.queryLiveData.getValue() != null : "Query live data can't be null";

        final RecipeQuery modifiedQuery = new RecipeQuery(mViewModel.queryLiveData.getValue()) {{
            minDiners = mMinDinersSeekBar.getProgress() + 1;

            /*
             * If no budget is specified in the EditText, ignore it completely by setting
             * maxBudget to MAX_VALUE.
             */
            try {
                maxBudget = Integer.parseInt(mMaxBudgetEditText.getText().toString());
            } catch (NumberFormatException e) {
                maxBudget = Integer.MAX_VALUE;
            }
        }};

        mViewModel.postQuery(modifiedQuery);

        startActivity(new Intent(this, ViewQueryActivity.class));
    }

    private SeekBar.OnSeekBarChangeListener getOnMinDinersSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mMinDinersSeekBarProgressTextView.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }
}
