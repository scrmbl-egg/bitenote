package app.bitenote.activities.query;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import app.bitenote.R;

/**
 * Class that represents the activity where the user specifies which ingredients the recipes of the
 * query must have or must not have.
 * @author Daniel N.
 */
public final class IngredientQueryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingredient_query_activity);
    }
}
