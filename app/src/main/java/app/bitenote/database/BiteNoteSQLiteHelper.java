package app.bitenote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import app.bitenote.recipes.Recipe;

/**
 * Instance of an SQLite interface for creating, reading, updating and deleting tables in the
 * recipes database.
 * @author Daniel N.
 */
public class BiteNoteSQLiteHelper extends SQLiteOpenHelper {
    /**
     * Name of the database.
     */
    public static final String DATABASE_NAME = "bitenote_recipes.db";

    /**
     * Version of the database.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Context.
     */
    private final Context context;

    @Override
    public void onCreate(@NonNull SQLiteDatabase database) {
        BiteNoteSQLiteTableHelper.createTables(database);
        BiteNoteSQLiteTableHelper.populateImmutableTables(database, context);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase database, int oldVersion, int newVersion) {
    }

    /**
     * Constructor for the SQLite recipes interface.
     * @param context Context.
     */
    public BiteNoteSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // SQLiteHelper doesn't expose its context, so it must be referenced again.
        this.context = context;
    }

    /**
     * Inserts a new recipe into the database.
     * @param recipe Recipe instance.
     */
    public void insertNewRecipe(@NonNull Recipe recipe) {
        // todo: implement recipe insertion
    }

    /**
     * Deletes a recipe from the database.
     * @param recipeId Recipe ID.
     */
    public void deleteRecipe(int recipeId) {
        // todo: implement recipe deletion
    }
}
