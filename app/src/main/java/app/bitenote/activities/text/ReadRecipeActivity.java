package app.bitenote.activities.text;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.instances.Recipe;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user reads a single recipe.
 * @author Daniel N.
 */
public final class ReadRecipeActivity extends AppCompatActivity {
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
     * Application view model. Grants access to the app's database.
     */
    private BiteNoteViewModel mViewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar mMaterialToolbar;

    /**
     * Floating action button for editing the recipe.
     */
    private FloatingActionButton mEditRecipeButton;

    /**
     * Text view where the recipe's name is contained.
     */
    private TextView mNameTextView;

    /**
     * Text view where the recipe's body is contained.
     */
    private TextView mBodyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_recipe_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();

        /// load data from recipe id
        loadData(getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0));
    }

    @Override
    protected void onResume() {
        super.onResume();

        /// this feels hacky, but ensures data is updated
        loadData(getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0));
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.ReadRecipeMaterialToolbar);
        mEditRecipeButton = findViewById(R.id.ReadRecipeEditRecipeButton);
        mNameTextView = findViewById(R.id.ReadRecipeNameTextView);
        mBodyTextView = findViewById(R.id.ReadRecipeBodyTextView);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        mEditRecipeButton.setOnClickListener(this::onEditRecipeButtonClick);
    }

    /**
     * Loads the necessary recipe's data and binds it to the activity's views.
     * @param id ID of the recipe in the database.
     */
    private void loadData(int id) {
        mDatabaseExecutor.execute(() -> {
            final Optional<Recipe> recipeOption = mViewModel.sqliteHelper.getRecipeFromId(id);

            mMainThreadHandler.post(() -> {
                if (recipeOption.isEmpty()) {
                    finish();
                } else {
                    bind(recipeOption.get());
                }
            });
        });
    }

    /**
     * Binds a recipe's data into the text views of the activity.
     * @param recipe {@link Recipe} instance.
     */
    private void bind(@NonNull Recipe recipe) {
        mNameTextView.setText(recipe.name);
        mBodyTextView.setText(recipe.body);
    }

    /**
     * Function called when {@link #mEditRecipeButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditRecipeButtonClick(@NonNull View view) {
        final int recipeId = getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0);
        final Intent intent = new Intent(this, WriteRecipeActivity.class);

        intent.putExtra(WriteRecipeActivity.INTENT_EXTRA_RECIPE_ID, recipeId);

        startActivity(intent);
    }
}
