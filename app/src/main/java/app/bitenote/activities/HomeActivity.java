package app.bitenote.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.bitenote.R;
import app.bitenote.activities.query.RecipeQueryActivity;
import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the activity where the application starts.
 * @author Daniel N.
 */
public final class HomeActivity extends AppCompatActivity {
    /**
     * Database view model.
     */
    private BiteNoteViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        /// init viewmodel
        final ViewModelProvider.Factory factory =
                new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(BiteNoteViewModel.class);

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
