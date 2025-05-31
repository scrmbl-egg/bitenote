package app.bitenote.app;

import android.app.Application;
import androidx.lifecycle.ViewModelProvider;

import app.bitenote.viewmodels.BiteNoteViewModel;

/**
 * Class that represents the BiteNote application (added in the manifest).
 */
public class BiteNoteApplication extends Application {
    /**
     * Application scoped view model. Grants access to the database API and shared live data.
     */
    private BiteNoteViewModel appViewModel;

    @Override
    public void onCreate() {
        super.onCreate();

        appViewModel = new ViewModelProvider.AndroidViewModelFactory(this)
                .create(BiteNoteViewModel.class);
    }

    /**
     * Gets the application's view model.
     * @return The application scoped view model. Grants access to the database API and shared live
     * data.
     */
    public BiteNoteViewModel getAppViewModel() {
        return appViewModel;
    }
}
