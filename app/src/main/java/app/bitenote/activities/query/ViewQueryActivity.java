package app.bitenote.activities.query;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import app.bitenote.R;

/**
 * Class that represents the activity where the user views the result of his recipe query and can
 * access the many recipes that meet the conditions.
 * @author Daniel N.
 */
public final class ViewQueryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_query_activity);
    }
}
