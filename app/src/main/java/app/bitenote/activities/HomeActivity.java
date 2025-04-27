package app.bitenote.activities;

import android.content.Intent;
import android.os.Bundle;
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
import app.bitenote.adapters.OnRecipeCardClickListener;
import app.bitenote.adapters.RecipeAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        /// init views
        final MaterialToolbar materialToolbar = findViewById(R.id.HomeMaterialToolbar);
        final RecyclerView recyclerView = findViewById(R.id.HomeRecipeRecyclerView);
        final FloatingActionButton newRecipeButton = findViewById(R.id.HomeNewRecipeButton);
        final FloatingActionButton makeQueryButton = findViewById(R.id.HomeMakeQueryButton);

        /// init viewmodel
        final ViewModelProvider.Factory factory =
                new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(BiteNoteViewModel.class);

        /// set material toolbar
        setSupportActionBar(materialToolbar);

        /// set recycler view, adapter, and click listeners
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final OnRecipeCardClickListener recipeCardClickListener = (recipeId, recipe) -> {
            final Intent readRecipeIntent =
                    new Intent(this, ReadRecipeActivity.class);
            readRecipeIntent.putExtra(ReadRecipeActivity.INTENT_EXTRA_RECIPE_ID, recipeId);
            startActivity(readRecipeIntent);
        };
        recipeAdapter = new RecipeAdapter(
                viewModel.sqliteHelper.getAllRecipes(),
                recipeCardClickListener
        );
        recyclerView.setAdapter(recipeAdapter);

        /// set new recipe button
        newRecipeButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, WriteRecipeActivity.class);
            intent.putExtra(WriteRecipeActivity.INTENT_EXTRA_IS_NEW_RECIPE, true);

            startActivity(intent);
        });

        /// set make query button
        makeQueryButton.setOnClickListener(view ->
                startActivity(new Intent(this, RecipeQueryActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        /// update adapter
        recipeAdapter.setRecipes(viewModel.sqliteHelper.getAllRecipes());
    }
}
