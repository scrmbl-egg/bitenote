package app.bitenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.bitenote.R;
import app.bitenote.activities.query.RecipeQueryActivity;
import app.bitenote.activities.text.WriteRecipeActivity;
import app.bitenote.activities.text.ReadRecipeActivity;
import app.bitenote.adapters.recipe.RecipeAdapter;
import app.bitenote.instances.Recipe;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the application starts.
 * @author Daniel N.
 */
public final class HomeActivity extends AppCompatActivity {
    /**
     * Application view model. Grants access to the app's database.
     */
    private BiteNoteViewModel viewModel;

    /**
     * Adapter for recipe recycler view.
     */
    private RecipeAdapter recipeAdapter;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar materialToolbar;

    /**
     * Recycler view for recipe cards.
     */
    private RecyclerView recyclerView;

    /**
     * Floating action button for creating a new recipe.
     */
    private FloatingActionButton newRecipeButton;

    /**
     * Floating action button for making a query.
     */
    private FloatingActionButton makeQueryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        /// init viewmodel
        final ViewModelProvider.Factory factory =
                new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(BiteNoteViewModel.class);

        /// init views
        materialToolbar = findViewById(R.id.HomeMaterialToolbar);
        recyclerView = findViewById(R.id.HomeRecipeRecyclerView);
        newRecipeButton = findViewById(R.id.HomeNewRecipeButton);
        makeQueryButton = findViewById(R.id.HomeMakeQueryButton);

        /// set material toolbar
        setSupportActionBar(materialToolbar);

        /// set recycler view, adapter, and click listeners
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(
                viewModel.sqliteHelper.getAllRecipes(),
                getOnRecipeCardClickListener()
        );
        recyclerView.setAdapter(recipeAdapter);

        /// set new recipe button
        newRecipeButton.setOnClickListener(this::onNewRecipeButtonClick);

        /// set make query button
        makeQueryButton.setOnClickListener(this::onMakeQueryButtonClick);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /// update adapter
        recipeAdapter.setRecipes(viewModel.sqliteHelper.getAllRecipes());
    }

    private RecipeAdapter.OnClickListener getOnRecipeCardClickListener() {
        return new RecipeAdapter.OnClickListener() {
            @Override
            public void onClick(int recipeId, @NonNull Recipe recipe) {
                final Intent intent =
                        new Intent(HomeActivity.this, ReadRecipeActivity.class);
                intent.putExtra(ReadRecipeActivity.INTENT_EXTRA_RECIPE_ID, recipeId);

                startActivity(intent);
            }

            @Override
            public void onLongClick(int recipeId, @NonNull Recipe recipe) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle(R.string.home_long_click_dialog_title)
                        .setMessage(getString(R.string.home_long_click_dialog_body, recipe.name))
                        .setPositiveButton(R.string.yes, (dialog, i) -> {
                            /// delete recipe and update adapter
                            viewModel.sqliteHelper.deleteRecipe(recipeId);
                            recipeAdapter.setRecipes(viewModel.sqliteHelper.getAllRecipes());

                            Toast.makeText(
                                    HomeActivity.this,
                                    getString(
                                            R.string.home_long_click_dialog_positive_toast,
                                            recipe.name
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .setNegativeButton(R.string.no, (dialog, i) -> {
                            Toast.makeText(
                                    HomeActivity.this,
                                    R.string.home_long_click_dialog_negative_toast,
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }
        };
    }

    /**
     * Function that is called when {@link #newRecipeButton} is clicked.
     * @param view {@link View} instance.
     */
    private void onNewRecipeButtonClick(@NonNull View view) {
        /// no id extra means a new recipe will be inserted
        startActivity(new Intent(this, WriteRecipeActivity.class));
    }

    /**
     * Function called when {@link #makeQueryButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onMakeQueryButtonClick(@NonNull View view) {
        startActivity(new Intent(this, RecipeQueryActivity.class));
    }
}
