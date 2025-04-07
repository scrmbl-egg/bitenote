package app.bitenote.activities.text;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import app.bitenote.R;

/**
 * Class that represents the activity where the user edits or writes a recipe.
 * @author Daniel N.
 */
public final class WriteRecipeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_recipe_activity);

        /// todo: include "save" button
    }
}
