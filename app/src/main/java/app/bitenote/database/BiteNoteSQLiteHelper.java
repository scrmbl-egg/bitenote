package app.bitenote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import app.bitenote.instances.Ingredient;
import app.bitenote.instances.MeasurementType;
import app.bitenote.instances.Recipe;
import app.bitenote.instances.Utensil;

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
        BiteNoteSQLiteTableHelper.dropTables(database);

        // init tables again
        onCreate(database);
    }

    @Override
    public void onConfigure(@NonNull SQLiteDatabase database) {
        super.onConfigure(database);
        database.enableWriteAheadLogging();
    }

    /**
     * Constructor for the SQLite recipes interface.
     * @param context Context.
     */
    public BiteNoteSQLiteHelper(@NonNull Context context) {
        this(DATABASE_NAME, context);
    }

    /**
     * Constructor for the SQLite recipes interface with support for a custom name. Use this
     * constructor for testing purposes only.
     * @param context Context.
     */
    public BiteNoteSQLiteHelper(@NonNull String databaseName, @NonNull Context context) {
        super(context, databaseName, null, DATABASE_VERSION);

        /*
         * SQLiteOpenHelper doesn't expose its context, so it must be referenced again.
         * We do this to call the BiteNoteSQLiteTableHelper functions, which need the application
         * context because it needs a path to the XML's that hold the immutable tables' data.
         * This SHOULD be safe because onCreate() isn't called until the getWriteableDatabase() or
         * getReadableDatabase() functions are called for the first time.
         */
        this.context = context;
    }

    /**
     * Gets the amount of recipe rows in the database.
     * @return An integer representing the number of recipes in the database.
     */
    public int getRecipeCount() {
        final String sql = "SELECT count(*) AS recipe_count FROM recipes;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args);
        ) {
            cursor.moveToFirst(); // this operation should be guaranteed

            return cursor.getInt(cursor.getColumnIndex("recipe_count")); // shouldn't return -1
        }
    }

    /**
     * Inserts a new recipe into the database and returns its ID.
     * @param recipe Recipe instance.
     * @return The ID of the inserted recipe.
     */
    public int insertRecipe(@NonNull Recipe recipe) {
        try (final SQLiteDatabase database = getWritableDatabase()) {
            int id = insertInRecipesTable(database, recipe);
            insertInRecipeIngredientsTable(database, recipe, id);
            insertInRecipeUtensilsTable(database, recipe, id);

            return id;
        }
    }

    /**
     * Updates a recipe row from the database and all other rows that reference it.
     * @param recipeId Recipe ID.
     * @param recipeInstance {@link Recipe} instance which holds the new data for the row.
     */
    public void updateRecipe(int recipeId, @NonNull Recipe recipeInstance) {
        assert recipeId != 0 : "Recipe ID can't be 0";

        /*
         * For this function, we update the 'recipes' table row, however, we delete and reinsert
         * other table rows that reference the recipe ID.
         *
         * No transaction is necessary here, since all operations in the called functions are
         * wrapped by transactions themselves.
         */
        try (final SQLiteDatabase database = getWritableDatabase()) {
            updateRecipeRow(database, recipeInstance, recipeId);

            /// delete and reinsert ingredients
            deleteRecipeIngredientRows(database, recipeId);
            populateRecipeInstanceIngredients(database, recipeInstance, recipeId);

            /// delete and reinsert utensils
            deleteRecipeUtensilRows(database, recipeId);
            populateRecipeInstanceUtensils(database, recipeInstance, recipeId);
        }
    }

    /**
     * Deletes a row from the 'recipes' table in the database, along with rows in other tables that
     * reference it.
     * @param recipeId Recipe ID.
     */
    public void deleteRecipe(int recipeId) {
        assert recipeId != 0 : "Recipe ID can't be 0.";

        final SQLiteDatabase database = getWritableDatabase();
        final String delRecipeSql = "DELETE FROM recipes WHERE id = ?;";
        final String delRecipeIngredientsSql =
                "DELETE FROM recipe_ingredients where recipe_id = ?;";
        final String delRecipeUtensilsSql = "DELETE FROM recipe_utensils where recipe_id = ?;";
        final Object[] args = {recipeId};

        database.beginTransaction();
        try {
            database.execSQL(delRecipeSql, args);
            database.execSQL(delRecipeIngredientsSql, args);
            database.execSQL(delRecipeUtensilsSql, args);

            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    /**
     * Gets a {@link Recipe} instance from its table row ID.
     * @param recipeId ID of the ingredient.
     * @return An {@link Optional} instance that wraps the nullable ID.
     */
    public Optional<Recipe> getRecipeFromId(int recipeId) {
        assert recipeId != 0 : "Recipe ID can't be 0";

        try (final SQLiteDatabase database = getReadableDatabase()) {
            final Optional<Recipe> recipeOption = getRecipeRowData(database, recipeId);
            if (recipeOption.isEmpty()) {
                return Optional.empty();
            }

            return recipeOption;
        }
    }

    /**
     * Gets an {@link Ingredient} instance from its table row ID.
     * @param ingredientId ID of the ingredient.
     * @return An {@link Optional} instance that wraps the obtained data.
     */
    public Optional<Ingredient> getIngredientFromId(int ingredientId) {
        assert ingredientId != 0 : "Ingredient ID can't be 0.";

        final String sql = "SELECT * FROM ingredients WHERE id = ? ORDER BY id ASC LIMIT 1;";
        final String[] args = {String.valueOf(ingredientId)};
        Ingredient ingredient = null;

        /// transaction isn't necessary here
        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args)
        ) {
            if (!cursor.moveToFirst()) {
                return Optional.empty();
            }

            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int measurementId = cursor.getInt(cursor.getColumnIndexOrThrow("measurement_id"));
            boolean canBeMeasuredInUnits =
                    cursor.getInt(cursor.getColumnIndexOrThrow("can_be_measured_in_units")) != 0;

            ingredient = new Ingredient(fullName, measurementId, canBeMeasuredInUnits);
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return Optional.ofNullable(ingredient);
    }

    /**
     * Gets the amount of ingredient rows in the database.
     * @return An integer representing the amount of ingredients in the database.
     * @implNote The result of this function is obtained through an SQL query, but the expectation
     * is that the function should always return the same result. Cache the result instead of
     * calling the function multiple times to reduce operations.
     */
    public int getIngredientCount() {
        final String sql = "SELECT count(*) AS ingredient_count FROM ingredients;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args);
        ) {
            cursor.moveToFirst(); // this operation should be guaranteed

            /// shouldn't return -1
            return cursor.getInt(cursor.getColumnIndex("ingredient_count"));
        }
    }

    /**
     * Gets a {@link MeasurementType} instance from its table row ID.
     * @param measurementTypeId ID of the ingredient.
     * @return An {@link Optional} instance that wraps the obtained data.
     */
    public Optional<MeasurementType> getMeasurementTypeFromId(int measurementTypeId) {
        assert measurementTypeId != 0 : "Measurement type ID can't be 0.";

        final String sql = "SELECT * FROM measurement_types WHERE id = ? ORDER BY id ASC LIMIT 1;";
        final String[] args = {String.valueOf(measurementTypeId)};
        MeasurementType measurementType = null;

        /// transaction isn't necessary here
        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args)
        ) {
            if (!cursor.moveToFirst()) {
                return Optional.empty();
            }

            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            measurementType = new MeasurementType(name);
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return Optional.ofNullable(measurementType);
    }

    /**
     * Gets the amount of measurement type rows in the database.
     * @return An integer representing the amount of measurement types in the database.
     * @implNote The result of this function is obtained through an SQL query, but the expectation
     * is that the function should always return the same result. Cache the result instead of
     * calling the function multiple times to reduce operations.
     */
    public int getMeasurementTypeCount() {
        final String sql = "SELECT count(*) AS measurement_type_count FROM measurement_types;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args);
        ) {
            cursor.moveToFirst(); // this operation should be guaranteed

            /// shouldn't return -1
            return cursor.getInt(cursor.getColumnIndex("measurement_type_count"));
        }
    }

    /**
     * Gets an {@link Utensil} instance from its table row ID.
     * @param utensilId ID of the ingredient.
     * @return An {@link Optional} instance that wraps the obtained data.
     */
    public Optional<Utensil> getUtensilFromId(int utensilId) {
        assert utensilId != 0 : "Utensil ID can't be 0.";

        final String sql = "SELECT * FROM utensils WHERE id = ? ORDER BY id ASC LIMIT 1;";
        final String[] args = {String.valueOf(utensilId)};
        Utensil utensil = null;

        /// transaction isn't necessary here
        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args)
        ) {
            if (!cursor.moveToFirst()) {
                return Optional.empty();
            }

            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            utensil = new Utensil(name);
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return Optional.ofNullable(utensil);
    }

    /**
     * Gets the amount of utensil rows in the database.
     * @return An integer representing the amount of utensils in the database.
     * @implNote The result of this function is obtained through an SQL query, but the expectation
     * is that the function should always return the same result. Cache the result instead of
     * calling the function multiple times to reduce operations.
     */
    public int getUtensilCount() {
        final String sql = "SELECT count(*) AS utensil_count FROM utensils;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args);
        ) {
            cursor.moveToFirst(); // this operation should be guaranteed

            return cursor.getInt(cursor.getColumnIndex("utensil_count")); // shouldn't return -1
        }
    }

    /**
     * Inserts a new recipe row in the 'recipes' SQLite table.
     * @param writeableDatabase SQLiteDatabase instance.
     * @param recipe Recipe instance.
     * @return The ID of the inserted recipe.
     */
    private static int insertInRecipesTable(
            @NonNull SQLiteDatabase writeableDatabase,
            @NonNull Recipe recipe
    ) {
        final String insertionSql = "INSERT INTO " +
                "recipes(name, body, budget, diners, creation_date) VALUES (?, ?, ?, ?, ?);";
        final Object[] insertionArgs = {
                recipe.name,
                recipe.body,
                recipe.budget,
                recipe.diners,
                recipe.creationDate.toString()
        };
        // this query string should get the last inserted recipe id
        final String querySql = "SELECT id FROM recipes ORDER BY id DESC LIMIT 1;";
        final String[] queryArgs = {};
        int id = 0;

        // insert row
        writeableDatabase.beginTransaction();
        try {
            writeableDatabase.execSQL(insertionSql, insertionArgs);
            writeableDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        } finally {
            writeableDatabase.endTransaction();
        }

        // get id
        try (final Cursor cursor = writeableDatabase.rawQuery(querySql, queryArgs)) {
            cursor.moveToFirst(); // this operation should be guaranteed.

            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return id;
    }

    /**
     * Updates a row in the 'recipes' table.
     * @param writeableDatabase {@link SQLiteDatabase} instance.
     * @param recipeInstance {@link Recipe} instance which holds the new data for the row.
     * @param recipeId ID of the recipe.
     */
    private static void updateRecipeRow(
            @NonNull SQLiteDatabase writeableDatabase,
            @NonNull Recipe recipeInstance,
            int recipeId
    ) {
        final String[] updateRecipeStatements = {
                "UPDATE recipes SET name = ? WHERE id = ?;",
                "UPDATE recipes SET body = ? WHERE id = ?;",
                "UPDATE recipes SET budget = ? WHERE id = ?;",
                "UPDATE recipes SET diners = ? WHERE id = ?;",
                "UPDATE recipes SET creation_date = ? WHERE id = ?;"
        };
        final Object[][] updateRecipeArgs = {
                {recipeInstance.name, recipeId},
                {recipeInstance.body, recipeId},
                {recipeInstance.budget, recipeId},
                {recipeInstance.diners, recipeId},
        };
        writeableDatabase.beginTransaction();
        try {
            for (int i = 0; i < updateRecipeStatements.length; i++) {
                writeableDatabase.execSQL(updateRecipeStatements[i], updateRecipeArgs[i]);
            }
            writeableDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            writeableDatabase.endTransaction();
        }
    }

    /**
     * Inserts a row in the 'recipe_ingredients' table.
     * @param writeableDatabase Writeable SQLiteDatabase instance.
     * @param recipeInstance Instance of the recipe.
     * @param recipeId The recipe ID in the SQLite database.
     */
    private static void insertInRecipeIngredientsTable(
            @NonNull SQLiteDatabase writeableDatabase,
            @NonNull Recipe recipeInstance,
            int recipeId
    ) {
        final String sql = "INSERT INTO recipe_ingredients(recipe_id, ingredient_id, amount)" +
                "VALUES (?, ?, ?);";

        writeableDatabase.beginTransaction();
        try {
            recipeInstance.getIngredients().forEach((ingredientId, amount) -> {
                final Object[] args = {
                        recipeId,
                        ingredientId,
                        amount
                };

                writeableDatabase.execSQL(sql, args);
            });

            writeableDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        } finally {
            writeableDatabase.endTransaction();
        }
    }

    /**
     * Inserts a row in the 'recipe_utensils' table.
     * @param writeableDatabase Writeable SQLiteDatabase instance.
     * @param recipeInstance Instance of the recipe.
     * @param recipeId The recipe ID in the SQLite database.
     */
    private static void insertInRecipeUtensilsTable(
            @NonNull SQLiteDatabase writeableDatabase,
            @NonNull Recipe recipeInstance,
            int recipeId
    ) {
        final String sql = "INSERT INTO recipe_utensils(recipe_id, utensil_id) VALUES (?, ?);";

        writeableDatabase.beginTransaction();
        try {
            recipeInstance.getUtensils().forEach((utensilId) -> {
                final Object[] args = {
                        recipeId,
                        utensilId
                };

                writeableDatabase.execSQL(sql, args);
            });

            writeableDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        } finally {
            writeableDatabase.endTransaction();
        }
    }

    /**
     * Gets the data from a 'recipes' table row.
     * @param database {@link SQLiteDatabase} instance.
     * @param recipeId ID of the recipe.
     * @return An {@link Optional} instance that may contain the recipe.
     */
    private static Optional<Recipe> getRecipeRowData(
            @NonNull SQLiteDatabase database,
            int recipeId
    ) {
        final String sql = "SELECT * FROM recipes WHERE id = ? ORDER BY id ASC LIMIT 1;";
        final String[] args = {String.valueOf(recipeId)};
        Recipe recipe = null;

        try (final Cursor cursor = database.rawQuery(sql, args)) {
            if (!cursor.moveToNext()) return Optional.empty();

            // get row data (with empty sets and maps)
            final String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            final String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            final HashMap<Integer, Float> ingredients = new HashMap<>();
            final HashSet<Integer> utensils = new HashSet<>();
            final int budget = cursor.getInt(cursor.getColumnIndexOrThrow("budget"));
            final int diners = cursor.getInt(cursor.getColumnIndexOrThrow("diners"));
            final Date creationDate = Date.valueOf(
                    cursor.getString(cursor.getColumnIndexOrThrow("creation_date"))
            );

            recipe = new Recipe(name, body, ingredients, utensils, creationDate, budget, diners);
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return Optional.ofNullable(recipe);
    }

    /**
     * Inserts elements from the 'recipe_ingredients' table into the {@link Recipe#ingredients}
     * field of a {@link Recipe} instance.
     * @param database {@link SQLiteDatabase} instance.
     * @param recipeInstance {@link Recipe} instance.
     * @param recipeId ID of the recipe.
     */
    private static void populateRecipeInstanceIngredients(
            @NonNull SQLiteDatabase database,
            @NonNull Recipe recipeInstance,
            int recipeId
    ) {
        final String sql = "SELECT * FROM recipe_ingredients WHERE recipe_id = ? " +
                "ORDER BY recipe_id ASC;";
        final String[] args = {String.valueOf(recipeId)};

        try (final Cursor cursor = database.rawQuery(sql, args)) {
            if (!cursor.moveToFirst()) return;

            do {
                int ingredientId = cursor.getInt(cursor.getColumnIndexOrThrow("ingredient_id"));
                float amount = cursor.getFloat(cursor.getColumnIndexOrThrow("amount"));

                recipeInstance.putIngredient(ingredientId, amount);
            } while (cursor.moveToNext());
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }
    }

    /**
     * Deletes all rows from the 'recipe_ingredients' table that reference a specified recipe row
     * ID.
     * @param database {@link SQLiteDatabase} instance.
     * @param recipeId ID of the recipe.
     */
    private static void deleteRecipeIngredientRows(
            @NonNull SQLiteDatabase database,
            int recipeId
    ) {
        final String sql = "DELETE FROM recipe_ingredients WHERE recipe_id = ?;";
        final Object[] args = {recipeId};

        database.beginTransaction();
        try {
            database.execSQL(sql, args);

            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Inserts elements from the 'recipe_utensils' table into the {@link Recipe#utensils} field of
     * a {@link Recipe} instance.
     * @param database {@link SQLiteDatabase} instance.
     * @param recipeInstance {@link Recipe} instance.
     * @param recipeId ID of the recipe.
     */
    private static void populateRecipeInstanceUtensils(
            @NonNull SQLiteDatabase database,
            @NonNull Recipe recipeInstance,
            int recipeId
    ) {
        final String sql =
                "SELECT * FROM recipe_utensils WHERE recipe_id = ? ORDER BY recipe_id ASC;";
        final String[] args = {String.valueOf(recipeId)};

        try (final Cursor cursor = database.rawQuery(sql, args)) {
            if (!cursor.moveToFirst()) return;

            do {
                int utensilId = cursor.getInt(cursor.getColumnIndexOrThrow("utensil_id"));

                recipeInstance.addUtensil(utensilId);
            } while (cursor.moveToNext());
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }
    }

    /**
     * Deletes all rows from the 'recipe_utensils' table that reference a specified recipe row ID.
     * @param database {@link SQLiteDatabase} instance.
     * @param recipeId ID of the recipe.
     */
    private static void deleteRecipeUtensilRows(
            @NonNull SQLiteDatabase database,
            int recipeId
    ) {
        final String sql = "DELETE FROM recipe_utensils WHERE recipe_id = ?;";
        final Object[] args = {recipeId};

        database.beginTransaction();
        try {
            database.execSQL(sql, args);

            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }
}
