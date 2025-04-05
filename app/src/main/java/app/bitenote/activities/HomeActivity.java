package app.bitenote.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.bitenote.R;
import app.bitenote.activities.query.RecipeQueryActivity;

/**
 * Class that represents the activity where the application starts.
 */
public final class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        /// set material toolbar
        final MaterialToolbar materialToolbar = findViewById(R.id.material_home_toolbar);
        setSupportActionBar(materialToolbar);

        /// set make query button
        final FloatingActionButton makeQueryButton = findViewById(R.id.make_query_button);
        makeQueryButton.setOnClickListener((listener) ->
                startActivity(new Intent(this, RecipeQueryActivity.class))
        );
    }
}
