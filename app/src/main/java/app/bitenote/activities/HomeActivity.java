package app.bitenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.activities.query.RecipeQueryActivity;
import app.bitenote.activities.text.WriteRecipeActivity;
import app.bitenote.activities.text.ReadRecipeActivity;
import app.bitenote.adapters.recipe.RecipeAdapter;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.instances.Recipe;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the application starts.
 * @author Daniel N.
 */
public final class HomeActivity extends AppCompatActivity {
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
     * Adapter for recipe recycler view.
     */
    private RecipeAdapter mRecipeAdapter;

    /**
     * Activity's Material toolbar.
     */
    private MaterialToolbar mMaterialToolbar;

    /**
     * Recycler view for recipe cards.
     */
    private RecyclerView mRecyclerView;

    /**
     * Floating action button for creating a new recipe.
     */
    private FloatingActionButton mNewRecipeButton;

    /**
     * Floating action button for making a query.
     */
    private FloatingActionButton mMakeQueryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /// update adapter
        mDatabaseExecutor.execute(() -> {
            final List<Pair<Integer, Recipe>> allRecipes = mViewModel.sqliteHelper.getAllRecipes();

            mMainThreadHandler.post(() -> mRecipeAdapter.setRecipes(allRecipes));
        });
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.HomeMaterialToolbar);
        mRecyclerView = findViewById(R.id.HomeRecipeRecyclerView);
        mNewRecipeButton = findViewById(R.id.HomeNewRecipeButton);
        mMakeQueryButton = findViewById(R.id.HomeMakeQueryButton);

        setSupportActionBar(mMaterialToolbar); // no navigation icon

        mDatabaseExecutor.execute(() -> {
            final List<Pair<Integer, Recipe>> allRecipes = mViewModel.sqliteHelper.getAllRecipes();

            mMainThreadHandler.post(() -> {
                mRecipeAdapter = new RecipeAdapter(
                        allRecipes,
                        getOnRecipeCardClickListener()
                );
                mRecyclerView.setAdapter(mRecipeAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            });
        });

        mNewRecipeButton.setOnClickListener(this::onNewRecipeButtonClick);
        mMakeQueryButton.setOnClickListener(this::onMakeQueryButtonClick);
    }

    /**
     * @return The {@link RecipeAdapter.OnClickListener} implementation that will run when a recipe
     * card is clicked.
     */
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
                            mDatabaseExecutor.execute(() -> {
                                mViewModel.sqliteHelper.deleteRecipe(recipeId);

                                mMainThreadHandler.post(() ->
                                        mRecipeAdapter.setRecipes(
                                                mViewModel.sqliteHelper.getAllRecipes()
                                        )
                                );
                            });

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
     * Function that is called when {@link #mNewRecipeButton} is clicked.
     * @param view {@link View} instance.
     */
    private void onNewRecipeButtonClick(@NonNull View view) {
        /// no id extra means a new recipe will be inserted
        startActivity(new Intent(this, WriteRecipeActivity.class));
    }

    /**
     * Function called when {@link #mMakeQueryButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onMakeQueryButtonClick(@NonNull View view) {
        startActivity(new Intent(this, RecipeQueryActivity.class));
    }
}
