package app.bitenote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import app.bitenote.recipes.Recipe;

/**
 * SQLite interface for creating, reading, updating and deleting tables in the recipes database.
 */
public class RecipesSQLite extends SQLiteOpenHelper {
    /**
     * Name of the database.
     */
    public static final String DATABASE_NAME = "bitenote_recipes.db";

    /**
     * Version of the database.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Constructor for the SQLite recipes interface.
     * @param ctx Context.
     */
    public RecipesSQLite(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase database) {
        // todo: create tables
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase database, int oldVersion, int newVersion) {
        final String sql = "DROP TABLE IF EXISTS recipes;";

        database.execSQL(sql);
        onCreate(database);
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
