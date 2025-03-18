package app.bitenote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.NonNull;
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

    /**
     * Constructor for the SQLite recipes interface.
     * @param context Context.
     */
    public BiteNoteSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // SQLiteOpenHelper doesn't expose its context, so it must be referenced again.
        this.context = context;
    }

    /**
     * Inserts a new recipe into the database and returns its ID.
     * @param recipe Recipe instance.
     * @return An {@link Optional} instance that wraps the nullable ID.
     */
    public Optional<Integer> insertRecipe(@NonNull Recipe recipe) {
        try (final SQLiteDatabase database = getWritableDatabase()) {
            Optional<Integer> idOption = insertInRecipesTable(database, recipe);

            if (idOption.isEmpty()) {
                return Optional.empty();
            }

            insertInRecipeIngredientsTable(database, recipe, idOption.get());
            insertInRecipeUtensilsTable(database, recipe, idOption.get());

            return idOption;
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
     * Gets an {@link Ingredient} instance from its table row ID.
     * @param ingredientId ID of the ingredient.
     * @return An {@link Ingredient} instance that represents the queried row.
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
     * Gets a {@link MeasurementType} instance from its table row ID.
     * @param measurementTypeId ID of the ingredient.
     * @return An {@link MeasurementType} instance that represents the queried row.
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
     * Gets an {@link Utensil} instance from its table row ID.
     * @param utensilId ID of the ingredient.
     * @return An {@link Utensil} instance that represents the queried row.
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
     * Inserts a new recipe row in the 'recipes' SQLite table.
     * @param writeableDatabase SQLiteDatabase instance.
     * @param recipe Recipe instance.
     * @return An Optional instance that wraps the nullable ID of the inserted recipe row.
     */
    private static Optional<Integer> insertInRecipesTable(
            @NonNull SQLiteDatabase writeableDatabase,
            @NonNull Recipe recipe
    ) {
        final String insertionSql = "INSERT INTO " +
                "recipes(name, body, budget, diners, creationDate) VALUES (?, ?, ?, ?, ?);";
        final Object[] insertionArgs = {
                recipe.name,
                recipe.body,
                recipe.budget,
                recipe.diners,
                recipe.getCreationDateAsSQLDateString()
        };
        // this query string should get the last inserted recipe id
        final String querySql = "SELECT id FROM recipes ORDER BY id DESC LIMIT 1;";
        final String[] queryArgs = {};

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

        // get (nullable) id
        try (final Cursor cursor = writeableDatabase.rawQuery(querySql, queryArgs)) {
            if (!cursor.moveToFirst()) {
                Log.e(null, "Couldn't get the recipe ID when inserting it.");
                return Optional.empty();
            }

            final int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

            return Optional.of(id);
        }
    }

    /**
     * Inserts a row in the 'recipeIngredients' SQLite table.
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

        recipeInstance.getIngredients().forEach((ingredientId, amount) -> {
            final Object[] args = {
                    recipeId,
                    ingredientId,
                    amount
            };

            writeableDatabase.execSQL(sql, args);
        });
    }

    /**
     * Inserts a row in the 'recipeUtensils' SQLite table.
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

        recipeInstance.getUtensils().forEach((utensilId) -> {
            final Object[] args = {
                    recipeId,
                    utensilId
            };

            writeableDatabase.execSQL(sql, args);
        });
    }
}
