package app.bitenote.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModelProvider;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the BiteNote application (added in the manifest).
 */
public class BiteNoteApplication extends Application {
    /**
     * Name of app shared preferences.
     */
    private static final String PREFS_NAME = "bitenote_prefs";

    /**
     * Shared preference name for checking if the app is on its first run.
     */
    private static final String PREF_IS_FIRST_RUN = "is_first_run";

    /**
     * Application scoped view model. Grants access to the database API and shared live data.
     */
    private BiteNoteViewModel mAppViewModel;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppViewModel = new ViewModelProvider.AndroidViewModelFactory(this)
                .create(BiteNoteViewModel.class);

        final SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final boolean isFirstRun = sharedPrefs.getBoolean(PREF_IS_FIRST_RUN, true);
        if (isFirstRun) {
            mAppViewModel.sqliteHelper.insertExampleRecipes();
            sharedPrefs.edit().putBoolean(PREF_IS_FIRST_RUN, false).apply();
        }
    }

    /**
     * Gets the application's view model.
     * @return The application scoped view model. Grants access to the database API and shared live
     * data.
     */
    public BiteNoteViewModel getAppViewModel() {
        return mAppViewModel;
    }
}
