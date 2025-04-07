package app.bitenote.activities.query;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.bitenote.R;

/**
 * Class that represents the activity where the user creates a query for filtering recipes.
 * @author Daniel N.
 */
public final class RecipeQueryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_query_activity);

        /// set material toolbar
        final MaterialToolbar materialToolbar = findViewById(R.id.material_recipe_query_toolbar);
        setSupportActionBar(materialToolbar);

        /// set material toolbar back button
        materialToolbar.setNavigationOnClickListener((listener) -> finish());

        /// set view query button
        final FloatingActionButton viewQueryButton = findViewById(R.id.make_query_button);
        viewQueryButton.setOnClickListener((listener) ->
                startActivity(new Intent(this, ViewQueryActivity.class))
        );
    }
}
