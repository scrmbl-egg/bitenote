package app.bitenote.activities.text.editing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.R;
import app.bitenote.adapters.recipe.utensil.AddedRecipeUtensilAdapter;
import app.bitenote.adapters.recipe.utensil.NonAddedRecipeUtensilAdapter;
import app.bitenote.app.BiteNoteApplication;
import app.bitenote.instances.Recipe;
import app.bitenote.instances.Utensil;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the user can edit the recipe's utensils.
 */
public final class EditRecipeUtensilsActivity extends AppCompatActivity {
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
     * Adapter for utensils that have been added to the recipe.
     */
    private AddedRecipeUtensilAdapter mAddedUtensilAdapter;

    /**
     * Recycler view for added utensils.
     */
    private RecyclerView mAddedUtensilsRecyclerView;

    /**
     * Adapter for utensils that have not been added to the recipe.
     */
    private NonAddedRecipeUtensilAdapter mNonAddedUtensilAdapter;

    /**
     * Recycler view for non-added utensils.
     */
    private RecyclerView mNonAddedUtensilsRecyclerView;

    /**
     * Floating action button for saving changes.
     */
    private FloatingActionButton mSaveChangesButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe_utensils_activity);

        /// init viewmodel
        mViewModel = ((BiteNoteApplication) getApplication()).getAppViewModel();

        setupViews();
    }

    /**
     * Sets up all the views in the activity.
     */
    private void setupViews() {
        mMaterialToolbar = findViewById(R.id.EditRecipeUtensilsMaterialToolbar);
        mSaveChangesButton = findViewById(R.id.EditRecipeUtensilsSaveChangesButton);
        mAddedUtensilsRecyclerView = findViewById(R.id.EditRecipeUtensilsAddedUtensilsRecyclerView);
        mNonAddedUtensilsRecyclerView =
                findViewById(R.id.EditRecipeUtensilsNonAddedUtensilsRecyclerView);

        setSupportActionBar(mMaterialToolbar);
        mMaterialToolbar.setNavigationOnClickListener(view -> finish());

        assert mViewModel.recipeLiveData.getValue() != null : "Recipe live data can't be null";

        final Recipe recipe = mViewModel.recipeLiveData.getValue().second;

        mDatabaseExecutor.execute(() -> {
            final List<Pair<Integer, Utensil>>
                    addedUtensils =
                    mViewModel.sqliteHelper.getRecipeUtensilsWithProperties(recipe);
            final List<Pair<Integer, Utensil>>
                    nonAddedUtensils =
                    mViewModel.sqliteHelper.getAllUtensilsExcept(recipe);

            mMainThreadHandler.post(() -> {
                mAddedUtensilAdapter = new AddedRecipeUtensilAdapter(
                        addedUtensils,
                        getOnAddedUtensilButtonClickListener()
                );
                mNonAddedUtensilAdapter = new NonAddedRecipeUtensilAdapter(
                        nonAddedUtensils,
                        getOnNonAddedUtensilButtonClickListener()
                );
                mAddedUtensilsRecyclerView.setAdapter(mAddedUtensilAdapter);
                mNonAddedUtensilsRecyclerView.setAdapter(mNonAddedUtensilAdapter);

                mAddedUtensilsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mNonAddedUtensilsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this)
                );
            });
        });

        mSaveChangesButton.setOnClickListener(this::onSaveChangesButtonClick);
    }

    /**
     * Function called when {@link #mSaveChangesButton} is clicked.
     * @param view {@link View} reference.
     */
    private void onSaveChangesButtonClick(@NonNull View view) {
        assert mViewModel.recipeLiveData.getValue() != null : "Current recipe can't be null";

        final int id = mViewModel.recipeLiveData.getValue().first;
        final Recipe modifiedCopy = new Recipe(mViewModel.recipeLiveData.getValue().second) {{
            clearUtensils();

            mAddedUtensilAdapter.getUtensils().forEach(pair -> addUtensil(pair.first));
        }};

        mDatabaseExecutor.execute(() -> {
            mViewModel.sqliteHelper.updateRecipe(id, modifiedCopy);

            mMainThreadHandler.post(() -> {
                mViewModel.postRecipe(modifiedCopy);
                Toast.makeText(
                        this,
                        R.string.utensils_saved_toast,
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        });
    }

    /**
     * @return The {@link AddedRecipeUtensilAdapter.OnButtonClickListener} implementation that
     * will run when an added utensil's card remove button is clicked.
     */
    private AddedRecipeUtensilAdapter.OnButtonClickListener
    getOnAddedUtensilButtonClickListener() {
        return (utensilId, utensil) -> {
            mAddedUtensilAdapter.removeUtensil(utensilId, utensil);
            mNonAddedUtensilAdapter.addUtensil(utensilId, utensil);
        };
    }

    /**
     * @return The {@link NonAddedRecipeUtensilAdapter.OnButtonClickListener} implementation that
     * will run when a non-added utensil's card add button is clicked.
     */
    private NonAddedRecipeUtensilAdapter.OnButtonClickListener
    getOnNonAddedUtensilButtonClickListener() {
        return (utensilId, utensil) -> {
            mNonAddedUtensilAdapter.removeUtensil(utensilId, utensil);
            mAddedUtensilAdapter.addUtensil(utensilId, utensil);
        };
    }
}
