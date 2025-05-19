package app.bitenote.activities.text;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Optional;

import app.bitenote.R;
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
     * Application view model. Grants access to the app's database.
     */
    private BiteNoteViewModel viewModel;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar materialToolbar;

    /**
     * Floating action button for editing the recipe.
     */
    private FloatingActionButton editRecipeButton;

    /**
     * Text view where the recipe's name is contained.
     */
    private TextView nameTextView;

    /**
     * Text view where the recipe's body is contained.
     */
    private TextView bodyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_recipe_activity);

        /// init views
        materialToolbar = findViewById(R.id.ReadRecipeMaterialToolbar);
        editRecipeButton = findViewById(R.id.ReadRecipeEditRecipeButton);
        nameTextView = findViewById(R.id.ReadRecipeNameTextView);
        bodyTextView = findViewById(R.id.ReadRecipeBodyTextView);

        /// init viewmodel
        viewModel = new ViewModelProvider(this).get(BiteNoteViewModel.class);

        /// set material toolbar
        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationOnClickListener(view -> finish());

        /// bind recipe data from ID in intent extra
        bindRecipeData();

        /// set edit recipe button
        editRecipeButton.setOnClickListener(this::onEditRecipeButtonClick);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindRecipeData();
    }

    /**
     * Binds the data from obtained from {@link ReadRecipeActivity#INTENT_EXTRA_RECIPE_ID}
     * into the text views. If the {@link ReadRecipeActivity#INTENT_EXTRA_RECIPE_ID} extra points
     * to a recipe that doesn't exist in the database, the data won't be bound.
     */
    private void bindRecipeData() {
        final int recipeId = getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0);
        final Optional<Recipe> recipeOption = viewModel.sqliteHelper.getRecipeFromId(recipeId);
        recipeOption.ifPresent(recipe -> {
            nameTextView.setText(recipe.name);
            bodyTextView.setText(recipe.body);
        });
    }

    /**
     * Function called when {@link #editRecipeButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onEditRecipeButtonClick(@NonNull View view) {
        final int recipeId = getIntent().getIntExtra(INTENT_EXTRA_RECIPE_ID, 0);
        final Intent intent = new Intent(this, WriteRecipeActivity.class);

        intent.putExtra(WriteRecipeActivity.INTENT_EXTRA_RECIPE_ID, recipeId);

        startActivity(intent);
    }
}
