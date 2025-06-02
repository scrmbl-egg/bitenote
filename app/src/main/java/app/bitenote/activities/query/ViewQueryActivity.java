package app.bitenote.activities.query;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.activities.text.ReadRecipeActivity;
import app.bitenote.adapters.recipe.RecipeAdapter;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Recipe;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user views the result of his recipe query and can
 * access the many recipes that meet the conditions.
 * @author Daniel N.
 */
public final class ViewQueryActivity extends AppCompatActivity {
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
     * Adapter for recipes.
     */
    private RecipeAdapter mRecipeAdapter;

    /**
     * Recycler view for recipe cards.
     */
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_query_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Initializes all views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.ViewQueryMaterialToolbar);
        mRecyclerView = findViewById(R.id.ViewQueryRecipeRecyclerView);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        assert mViewModel.queryLiveData.getValue() != null : "Query live data can't be null";
        final RecipeQuery query = mViewModel.queryLiveData.getValue();

        mDatabaseExecutor.execute(() -> {
            final List<Pair<Integer, Recipe>> queriedRecipes =
                    mViewModel.sqliteHelper.getQueriedRecipes(query);

            mMainThreadHandler.post(() -> {
                mRecipeAdapter = new RecipeAdapter(queriedRecipes, getOnRecipeCardClickListener());
                mRecyclerView.setAdapter(mRecipeAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            });
        });
    }

    private RecipeAdapter.OnClickListener getOnRecipeCardClickListener() {
        return new RecipeAdapter.OnClickListener() {
            @Override
            public void onClick(int recipeId, @NonNull Recipe recipe) {
                final Intent intent =
                        new Intent(ViewQueryActivity.this, ReadRecipeActivity.class);
                intent.putExtra(ReadRecipeActivity.INTENT_EXTRA_RECIPE_ID, recipeId);

                startActivity(intent);
            }

            @Override
            public void onLongClick(int recipeId, @NonNull Recipe recipe) {
                new AlertDialog.Builder(ViewQueryActivity.this)
                        .setTitle(R.string.home_long_click_dialog_title)
                        .setMessage(getString(R.string.home_long_click_dialog_body, recipe.name))
                        .setPositiveButton(R.string.yes, (dialog, i) -> {
                            /// delete recipe and update adapter
                            mDatabaseExecutor.execute(() -> {
                                mViewModel.sqliteHelper.deleteRecipe(recipeId);

                                mMainThreadHandler.post(() ->
                                        mRecipeAdapter.setRecipes(
                                                mViewModel.sqliteHelper.getAllRecipes()
                                        )
                                );
                            });

                            Toast.makeText(
                                    ViewQueryActivity.this,
                                    getString(
                                            R.string.home_long_click_dialog_positive_toast,
                                            recipe.name
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .setNegativeButton(R.string.no, (dialog, i) -> {
                            Toast.makeText(
                                    ViewQueryActivity.this,
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
}
