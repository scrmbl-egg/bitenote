package app.bitenote.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import app.bitenote.database.BiteNoteSQLiteHelper;

/**
 * Viewmodel for handling database state and persistence.
 * @author Daniel N.
 */
public class BiteNoteViewModel extends AndroidViewModel {
    /**
     * SQLite database helper.
     */
    public final BiteNoteSQLiteHelper sqliteHelper;

    /**
     * BiteNoteViewModel constructor.
     * @param application {@link Application} instance. It is used as context for the database
     * helper.
     */
    public BiteNoteViewModel(@NonNull Application application) {
        super(application);
        sqliteHelper = new BiteNoteSQLiteHelper(application);
    }
}
