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
public class RecipesSQLite extends SQLiteOpenHelper {
    /**
     * Context.
     */
    private final Context context;

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
     * @param context Context.
     */
    public RecipesSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // SQLiteHelper doesn't expose its context, so it must be referenced again.
        this.context = context;
    }

    /**
     * Creates all the necessary tables in the database.
     * @param database SQLiteDatabase instance.
     */
    private void createTables(@NonNull SQLiteDatabase database) {
        final String createUtensilsTable = "CREATE TABLE utensils (" +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL" +
                ");";

        final String createMeasurementTypesTable = "CREATE TABLE measurementTypes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL" +
                ");";

        final String createRecipesTable = "CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL," +
                "body MEDIUMTEXT NOT NULL DEFAULT ''," +
                "budget INTEGER," +
                "diners INTEGER" +
                ");";

        final String createIngredientsTable = "CREATE TABLE ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL," +
                "measurement_id INTEGER NOT NULL," +
                "can_be_measured_in_units BOOLEAN NOT NULL," +
                "FOREIGN KEY (measurement_id) REFERENCES measurementTypes(id)" +
                ");";

        final String createRecipeIngredientsTable = "CREATE TABLE recipeIngredients (" +
                "recipe_id INTEGER PRIMARY KEY," +
                "ingredient_id INTEGER PRIMARY KEY," +
                "amount FLOAT," +
                "FOREIGN KEY (recipe_id) REFERENCES recipes(id)," +
                "FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)" +
                ");";

        final String createRecipeUtensilsTable = "CREATE TABLE recipeUtensils (" +
                "recipe_id INTEGER PRIMARY KEY," +
                "utensil_id INTEGER PRIMARY KEY," +
                "FOREIGN KEY (recipe_id) REFERENCES recipes(id)," +
                "FOREIGN KEY (utensil_id) REFERENCES utensils(id)" +
                ");";

        database.execSQL(createUtensilsTable);
        database.execSQL(createMeasurementTypesTable);
        database.execSQL(createRecipesTable);
        database.execSQL(createIngredientsTable);
        database.execSQL(createRecipeIngredientsTable);
        database.execSQL(createRecipeUtensilsTable);
    }

    private void populateTables(@NonNull SQLiteDatabase database) {
        // ingredients table is populated with xml data
        // todo: get xmlresourceparser from context and use it to populate the hardcoded tables
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase database) {
        createTables(database);
        populateTables(database);
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
