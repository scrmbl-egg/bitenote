package app.bitenote.database;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.bitenote.R;
import app.bitenote.instances.Ingredient;
import app.bitenote.instances.MeasurementType;
import app.bitenote.instances.Recipe;
import app.bitenote.instances.Utensil;

/**
 * Instance of an SQLite interface for creating, reading, updating and deleting tables in the
 * recipes database.
 * @author Daniel N.
 */
public final class BiteNoteSQLiteHelper extends SQLiteOpenHelper {
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

    /**
     * Array of {@link Pair}s, in which the first element represents the ID of the ingredient,
     * and the second element represents the data that the ID references contained in a
     * {@link Ingredient} instance.
     */
    private List<Pair<Integer, Ingredient>> ingredients = null;

    /**
     * Amount of total ingredients in the database.
     * @implNote Cached to prevent unnecessary computations.
     */
    private Integer ingredientCount = null;

    /**
     * Array of {@link Pair}s, in which the first element represents the ID of the utensil,
     * and the second element represents the data that the ID references contained in a
     * {@link Utensil} instance.
     */
    private List<Pair<Integer, Utensil>> utensils = null;

    /**
     * Amount of total utensils in the database.
     * @implNote Cached to prevent unnecessary computations.
     */
    private Integer utensilCount = null;

    /**
     * Array of {@link Pair}s, in which the first element represents the ID of the measurement type,
     * and the second element represents the data that the ID references contained in a
     * {@link MeasurementType} instance.
     */
    private List<Pair<Integer, MeasurementType>> measurementTypes = null;

    /**
     * Amount of total measurement types in the database.
     * @implNote Cached to prevent unnecessary computations.
     */
    private Integer measurementTypeCount = null;

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
         * We do this to call functions that need access to the project XMLs, which is only
         * obtained through the application context.
         * This SHOULD be safe because onCreate() isn't called until the getWriteableDatabase() or
         * getReadableDatabase() functions are called for the first time.
         */
        this.context = context;
    }

    /**
     * Inserts the example recipes from 'test_recipes.xml' into the database.
     * @return The ID array of the inserted recipes, ordered by creation.
     * @see BiteNoteSQLiteHelper#getRecipeFromId(int)
     */
    public int[] insertExampleRecipes() {
        final ArrayList<Integer> exampleIdList = new ArrayList<>();
        final Recipe currentRecipeData = new Recipe();
        String lastFoundXmlTag = "";
        int currentIngredientId = 0;
        int currentUtensilId = 0;

        try (
                final XmlResourceParser parser = context.getResources().getXml(
                        R.xml.example_recipes
                )
        ) {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    switch (parser.getName()) {
                        case Recipe.XML_RECIPE_NAME_TAG:
                        case Recipe.XML_RECIPE_BODY_TAG:
                        case Recipe.XML_RECIPE_DINERS_TAG:
                        case Recipe.XML_RECIPE_BUDGET_TAG:
                        case Recipe.XML_RECIPE_CREATION_DATE_TAG:
                            lastFoundXmlTag = parser.getName();
                            parser.next();
                            break;
                        case Recipe.XML_RECIPE_INGREDIENT_TAG:
                            lastFoundXmlTag = parser.getName();
                            currentIngredientId = parser.getAttributeIntValue(
                                    null,
                                    Recipe.XML_RECIPE_INGREDIENT_ID_ATTRIBUTE,
                                    0
                            );
                            parser.next();
                            break;
                        case Recipe.XML_RECIPE_UTENSIL_TAG:
                            lastFoundXmlTag = parser.getName();
                            currentUtensilId = parser.getAttributeIntValue(
                                    null,
                                    Recipe.XML_RECIPE_UTENSIL_ID_ATTRIBUTE,
                                    0
                            );
                            currentRecipeData.addUtensil(currentUtensilId);
                            parser.next();
                            break;
                        default:
                            parser.next();
                            continue;
                    }

                    if (parser.getEventType() != XmlPullParser.TEXT) {
                        parser.next();
                        continue;
                    }

                    switch (lastFoundXmlTag) {
                        case Recipe.XML_RECIPE_NAME_TAG:
                            currentRecipeData.name = parser.getText().trim();
                            break;
                        case Recipe.XML_RECIPE_BODY_TAG:
                            /// replace file line endings and tabs, and collapse spaces
                            currentRecipeData.body = parser.getText()
                                    .trim()
                                    .replaceAll("(\\r\\n|\\n)", "\n")
                                    .replaceAll("\\n+", " ")
                                    .replaceAll("\\t+", " ")
                                    .replaceAll(" +", " ");
                            break;
                        case Recipe.XML_RECIPE_DINERS_TAG:
                            currentRecipeData.diners = Integer.valueOf(parser.getText().trim());
                            break;
                        case Recipe.XML_RECIPE_BUDGET_TAG:
                            currentRecipeData.budget = Integer.valueOf(parser.getText().trim());
                            break;
                        case Recipe.XML_RECIPE_CREATION_DATE_TAG:
                            currentRecipeData.creationDate = Date.valueOf(parser.getText().trim());
                            break;
                        case Recipe.XML_RECIPE_INGREDIENT_TAG:
                            currentRecipeData.putIngredient(
                                    currentIngredientId,
                                    Integer.valueOf(parser.getText().trim())
                            );
                            break;
                            // utensil case doesn't need to be handled since there is no TEXT
                    }
                } else if (
                        parser.getEventType() == XmlPullParser.END_TAG
                        && parser.getName().equals(Recipe.XML_RECIPE_TAG)
                ) {
                    /// all data is gathered, insert into the database
                    int parsedExampleId = insertRecipe(currentRecipeData);
                    exampleIdList.add(parsedExampleId);

                    /// clear recipe data sets and maps
                    currentRecipeData.clearIngredients();
                    currentRecipeData.clearUtensils();
                }

                parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        }

        /// sort ids by their creation date in descending order
        exampleIdList.sort((a, b) -> {
            final long aCreationDateTime = getRecipeFromId(a).get().creationDate.getTime();
            final long bCreationDateTime = getRecipeFromId(b).get().creationDate.getTime();

            /*
             * comparators are weird and expect the following values depending on the comparison
             * 1 -> greater than
             * 0 -> equal
             * -1 -> less than
             * but these must be inverted for it to be sorted in decending order
             *
             * why
             */
            if (aCreationDateTime > bCreationDateTime) return -1;
            else if (aCreationDateTime == bCreationDateTime) return 0;
            else return 1;
        });

        return exampleIdList.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Gets the amount of recipe rows in the database.
     * @return An integer representing the number of recipes in the database.
     * @implNote Getting the amount of recipes in the database requires an SQL query each time this
     * function is called. It is recommended to cache the return value when it is used multiple
     * times when no modifications are done to the database.
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
     * @param recipeInstance {@link Recipe} instance which holds the new data for the rows.
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
            insertInRecipeIngredientsTable(database, recipeInstance, recipeId);

            /// delete and reinsert utensils
            deleteRecipeUtensilRows(database, recipeId);
            insertInRecipeUtensilsTable(database, recipeInstance, recipeId);
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
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    /**
     * Deletes many rows from the 'recipes' table in the database, along with rows in other tables
     * that reference them.
     * @param recipeIds Recipe ID array.
     */
    public void deleteRecipes(int[] recipeIds) {
        for (int i = 0; i < recipeIds.length; i++) {
            deleteRecipe(recipeIds[i]);
        }
    }

    /**
     * Gets a {@link Recipe} instance from its table row ID.
     * @param recipeId ID of the ingredient.
     * @return An {@link Optional} instance that wraps the nullable {@link Recipe}.
     */
    public Optional<Recipe> getRecipeFromId(int recipeId) {
        assert recipeId != 0 : "Recipe ID can't be 0";

        try (final SQLiteDatabase database = getReadableDatabase()) {
            final Optional<Recipe> recipeOption = getRecipeRowData(database, recipeId);
            if (recipeOption.isEmpty()) {
                return Optional.empty();
            }

            /// insert other table data into the recipe instance
            populateRecipeInstanceUtensils(database, recipeOption.get(), recipeId);
            populateRecipeInstanceIngredients(database, recipeOption.get(), recipeId);

            return recipeOption;
        }
    }

    /**
     * Gets all recipes in the database ordered from newest to oldest.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * recipe, and the second element represents the data that the ID references contained in a
     * {@link Recipe} instance. The elements are ordered from newest to oldest, see:
     * {@link Recipe#creationDate}.
     * @implNote Calling this function costs more than other similar functions like
     * {@link #getAllIngredients()}, {@link #getAllUtensils()} or {@link #getAllMeasurementTypes()}
     * because the recipes table is mutable, which means the result can't be internally cached.
     */
    public List<Pair<Integer, Recipe>> getAllRecipes() {
        /// query for all recipes, orders from newest to oldest
        final String sql = "SELECT id FROM recipes ORDER BY creation_date DESC;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args)
        ) {
            if (!cursor.moveToFirst()) return new ArrayList<>();

            final List<Pair<Integer, Recipe>> recipeList = new ArrayList<>(cursor.getCount());

            do {
                final int id = cursor.getInt(cursor.getColumnIndex("id"));
                final Recipe recipeInstance = getRecipeFromId(id).get();

                recipeList.add(Pair.create(id, recipeInstance));
            } while (cursor.moveToNext());

            return Collections.unmodifiableList(recipeList);
        }
    }

    /**
     * Gets all the recipes ordered from newest to oldest that meet the conditions of a
     * {@link RecipeQuery}.
     * @param rQuery {@link RecipeQuery} instance. Contains the data that will be filtered.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * recipe, and the second element represents the data that the ID references contained in a
     * {@link Recipe} instance.
     */
    public List<Pair<Integer, Recipe>> getQueriedRecipes(@NonNull RecipeQuery rQuery) {
        final String[] args = {}; // arguments are handled in RecipeQuery.toSQLString

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(rQuery.toSQLString(), args)
        ) {
            if (!cursor.moveToFirst()) return new ArrayList<>();

            /// allocate new array of ids
            final List<Pair<Integer, Recipe>> recipeList = new ArrayList<>(cursor.getCount());
            do {
                final int id = cursor.getInt(cursor.getColumnIndex("id"));
                final Recipe recipeInstance = getRecipeFromId(id).get();

                recipeList.add(Pair.create(id, recipeInstance));
            } while (cursor.moveToNext());

            return recipeList;
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

            final String fullName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            final int measurementId =
                    cursor.getInt(cursor.getColumnIndexOrThrow("measurement_id"));
            final boolean canBeMeasuredInUnits =
                    cursor.getInt(cursor.getColumnIndexOrThrow("can_be_measured_in_units")) != 0;

            ingredient = new Ingredient(fullName, measurementId, canBeMeasuredInUnits);
        } catch (IllegalArgumentException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return Optional.ofNullable(ingredient);
    }

    /**
     * Gets all the ingredients in the database in ascending order.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * ingredient, and the second element represents the data that the ID references contained in a
     * {@link Ingredient} instance.
     */
    public List<Pair<Integer, Ingredient>> getAllIngredients() {
        if (ingredients != null) return Collections.unmodifiableList(ingredients);

        final String sql = "SELECT id FROM ingredients ORDER BY id ASC;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args)
        ) {
            if (!cursor.moveToFirst()) return new ArrayList<>();

            final List<Pair<Integer, Ingredient>> ingredientList = new ArrayList<>();

            do {
                final int id = cursor.getInt(cursor.getColumnIndex("id"));
                final Ingredient ingredientInstance = getIngredientFromId(id).get();

                ingredientList.add(Pair.create(id, ingredientInstance));
            } while (cursor.moveToNext());

            ingredients = ingredientList;
            return Collections.unmodifiableList(ingredients);
        }
    }

    /**
     * Gets all ingredients in the database, except the ones specified by the caller.
     * @param except {@link Recipe} that contains the ingredients that are going to be ignored.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * ingredient, and the second element represents the data that the ID references contained in a
     * {@link Ingredient} instance.
     */
    public List<Pair<Integer, Ingredient>> getAllIngredientsExcept(@NonNull Recipe except) {
        return getAllIngredientsExcept(except.getIngredients().keySet());
    }

    /**
     * Gets all ingredients in the database, except the ones specified by the caller.
     * @param except {@link Map} where the key is an integer ID that represents an ingredients
     * that will be excluded from the selection (values are ignored).
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * ingredient, and the second element represents the data that the ID references contained in a
     * {@link Ingredient} instance.
     */
    public List<Pair<Integer, Ingredient>> getAllIngredientsExcept(
            @NonNull Map<Integer, ?> except
    ) {
        return getAllIngredientsExcept(except.keySet());
    }

    public List<Pair<Integer, Ingredient>> getAllIngredientsExcept(@NonNull RecipeQuery except) {
        final Set<Integer> exceptSet = new HashSet<>();
        for (int id: except.getQueriedIngredients()) {
            exceptSet.add(id);
        }

        return getAllIngredientsExcept(exceptSet);
    }

    /**
     * Gets all ingredients in the database, except the ones specified by the caller.
     * @param except {@link Set} of integer IDs that represent the ingredients that will be
     * excluded from the selection.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * ingredient, and the second element represents the data that the ID references contained in a
     * {@link Ingredient} instance.
     */
    public List<Pair<Integer, Ingredient>> getAllIngredientsExcept(@NonNull Set<Integer> except) {
        final List<Pair<Integer, Ingredient>> filteredList =
                new ArrayList<>(getAllIngredients().size() - except.size());

        for (Pair<Integer, Ingredient> pair: getAllIngredients()) {
            if (except.contains(pair.first)) continue;

            filteredList.add(pair);
        }

        return filteredList;
    }

    /**
     * Gets all ingredient data from a recipe as a pair list.
     * @param recipe The {@link Recipe} instance where the ingredients are going to be obtained
     * from.
     * @return An array of {@link Pair}s, where the first element is the ID of the ingredient,
     * and the second element is another {@link Pair}, whose first element is the ingredient data
     * wrapped in an {@link Ingredient} instance, and the second element is the amount of that
     * ingredient in the recipe.
     */
    public List<Pair<Pair<Integer, Ingredient>, Ingredient.InRecipeProperties>> // oh...
    getRecipeIngredientsWithProperties(
            @NonNull Recipe recipe
    ) {
        final List<Pair<Pair<Integer, Ingredient>, Ingredient.InRecipeProperties>> // jeez
                recipeIngredientsList = new ArrayList<>(recipe.getIngredients().size());

        for (
                Map.Entry<Integer, Ingredient.InRecipeProperties> entry:
                recipe.getIngredients().entrySet()
        ) {
            final int id = entry.getKey();
            final Ingredient ingredient = getIngredientFromId(id).get();
            final Pair<Integer, Ingredient> ingredientPair = Pair.create(id, ingredient);
            final Ingredient.InRecipeProperties properties = entry.getValue();

            recipeIngredientsList.add(Pair.create(ingredientPair, properties));
        }

        return recipeIngredientsList;
    }

    public List<Pair<Integer, Ingredient>> getQueryIncludedIngredientsWithProperties(
            @NonNull RecipeQuery query
    ) {
        final List<Pair<Integer, Ingredient>> list =
                new ArrayList<>(query.getIncludedIngredients().size());

        query.getIncludedIngredients().forEach(id ->
                list.add(Pair.create(id, getIngredientFromId(id).get()))
        );

        return list;
    }

    public List<Pair<Integer, Ingredient>> getQueryBannedIngredientsWithProperties(
            @NonNull RecipeQuery query
    ) {
        final List<Pair<Integer, Ingredient>> list =
                new ArrayList<>(query.getBannedIngredients().size());

        query.getBannedIngredients().forEach(id ->
                list.add(Pair.create(id, getIngredientFromId(id).get()))
        );

        return list;
    }

    /**
     * Gets the amount of ingredient rows in the database.
     * @return An integer representing the amount of ingredients in the database.
     */
    public int getIngredientCount() {
        if (ingredientCount != null) {
            return ingredientCount;
        }

        final String sql = "SELECT count(*) AS ingredient_count FROM ingredients;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args);
        ) {
            cursor.moveToFirst(); // this operation should be guaranteed

            /// shouldn't return -1
            ingredientCount = cursor.getInt(cursor.getColumnIndex("ingredient_count"));
            return ingredientCount;
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

            final String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            measurementType = new MeasurementType(name);
        } catch (IllegalArgumentException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return Optional.ofNullable(measurementType);
    }

    /**
     * Gets all the measurement types in the database in ascending order.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * measurement types, and the second element represents the data that the ID references
     * contained in a {@link MeasurementType} instance.
     */
    public List<Pair<Integer, MeasurementType>> getAllMeasurementTypes() {
        if (measurementTypes != null) return measurementTypes;

        final String sql = "SELECT id FROM measurement_types ORDER BY id ASC;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args)
        ) {
            if (!cursor.moveToFirst()) return new ArrayList<>();

            final List<Pair<Integer, MeasurementType>> mTypeList =
                    new ArrayList<>(cursor.getCount());

            do {
                final int id = cursor.getInt(cursor.getColumnIndex("id"));
                final MeasurementType mTypeInstance = getMeasurementTypeFromId(id).get();

                mTypeList.add(Pair.create(id, mTypeInstance));
            } while (cursor.moveToNext());

            measurementTypes = mTypeList;
            return Collections.unmodifiableList(measurementTypes);
        }
    }

    /**
     * Gets the amount of measurement type rows in the database.
     * @return An integer representing the amount of measurement types in the database.
     */
    public int getMeasurementTypeCount() {
        if (measurementTypeCount != null) {
            return measurementTypeCount;
        }

        final String sql = "SELECT count(*) AS measurement_type_count FROM measurement_types;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args);
        ) {
            cursor.moveToFirst(); // this operation should be guaranteed

            /// shouldn't return -1
            measurementTypeCount = cursor.getInt(
                    cursor.getColumnIndex("measurement_type_count")
            );
            return measurementTypeCount;
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
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return Optional.ofNullable(utensil);
    }

    /**
     * Gets all the ingredients in the database in ascending order.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * utensil, and the second element represents the data that the ID references contained in a
     * {@link Utensil} instance.
     */
    public List<Pair<Integer, Utensil>> getAllUtensils() {
        if (utensils != null) return utensils;

        final String sql = "SELECT id FROM utensils ORDER BY id ASC;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args)
        ) {
            if (!cursor.moveToFirst()) return new ArrayList<>();

            final List<Pair<Integer, Utensil>> utensilList = new ArrayList<>(cursor.getCount());

            do {
                final int id = cursor.getInt(cursor.getColumnIndex("id"));
                final Utensil utensilInstance = getUtensilFromId(id).get();

                utensilList.add(Pair.create(id, utensilInstance));
            } while (cursor.moveToNext());

            utensils = utensilList;
            return Collections.unmodifiableList(utensils);
        }
    }

    /**
     * Gets all utensils in the database, except the ones specified by the caller.
     * @param except {@link Recipe} that contains the ingredients that are going to be ignored.
     * @return An array of {@link Pair}s, in which the first element represents the ID of the
     * ingredient, and the second element represents the data that the ID references contained in a
     * {@link Ingredient} instance.
     */
    public List<Pair<Integer, Utensil>> getAllUtensilsExcept(@NonNull Recipe except) {
        return getAllUtensilsExcept(except.getUtensils());
    }

    public List<Pair<Integer, Utensil>> getAllUtensilsExcept(@NonNull RecipeQuery except) {
        final Set<Integer> exceptSet = new HashSet<>();
        for (int id: except.getQueriedUtensils()) {
            exceptSet.add(id);
        }

        return getAllUtensilsExcept(exceptSet);
    }

    /**
     * Gets all utensils in the database, except the ones specified by the caller.
     * @param except {@link Set} of integer IDs that represent the utensils that will be excluded
     * from the selection.
     * @return An unmodifiable {@link List} of {@link Pair}s, in which the first element represents
     * the ID of the utensil, and the second element represents the data that the ID references
     * contained in a {@link Utensil} instance.
     */
    public List<Pair<Integer, Utensil>> getAllUtensilsExcept(@NonNull Set<Integer> except) {
        final List<Pair<Integer, Utensil>> filteredList =
                new ArrayList<>(getAllUtensils().size() - except.size());

        for (Pair<Integer, Utensil> pair: getAllUtensils()) {
            if (except.contains(pair.first)) continue;

            filteredList.add(pair);
        }

        return filteredList;
    }

    /**
     * Gets all utensil data from a recipe as a pair array.
     * @param recipe The {@link Recipe} instance where the utensils are going to be obtained from.
     * @return An array of {@link Pair}s, where the first element is the ID of the ingredient,
     * and the second element is another {@link Pair}, whose first element is the ingredient data
     * wrapped in an {@link Ingredient} instance, and the second element is the amount of that
     * ingredient in the recipe.
     */
    public List<Pair<Integer, Utensil>> getRecipeUtensilsWithProperties(
            @NonNull Recipe recipe
    ) {
        final Set<Integer> utensilIdSet = recipe.getUtensils();
        final List<Pair<Integer, Utensil>> list = new ArrayList<>(utensilIdSet.size());

        for (Integer id: utensilIdSet) {
            final Utensil utensilData = getUtensilFromId(id).get();
            list.add(Pair.create(id, utensilData));
        }

        return list;
    }

    public List<Pair<Integer, Utensil>> getQueryIncludedUtensilsWithProperties(
            @NonNull RecipeQuery query
    ) {
        final List<Pair<Integer, Utensil>> list =
                new ArrayList<>(query.getIncludedUtensils().size());

        query.getIncludedUtensils().forEach(id ->
                list.add(Pair.create(id, getUtensilFromId(id).get()))
        );

        return list;
    }

    public List<Pair<Integer, Utensil>> getQueryBannedUtensilsWithProperties(
            @NonNull RecipeQuery query
    ) {
        final List<Pair<Integer, Utensil>> list =
                new ArrayList<>(query.getBannedUtensils().size());

        query.getBannedUtensils().forEach(id ->
                list.add(Pair.create(id, getUtensilFromId(id).get()))
        );

        return list;
    }

    /**
     * Gets the amount of utensil rows in the database.
     * @return An integer representing the amount of utensils in the database.
     */
    public int getUtensilCount() {
        if (utensilCount != null) {
            return utensilCount;
        }

        final String sql = "SELECT count(*) AS utensil_count FROM utensils;";
        final String[] args = {};

        try (
                final SQLiteDatabase database = getReadableDatabase();
                final Cursor cursor = database.rawQuery(sql, args);
        ) {
            cursor.moveToFirst(); // this operation should be guaranteed

            /// shouldn't return -1
            utensilCount = cursor.getInt(cursor.getColumnIndex("utensil_count"));
            return utensilCount;
        }
    }

    /**
     * Inserts a new recipe row in the 'recipes' SQLite table.
     * @param writeableDatabase SQLiteDatabase instance.
     * @param recipe Recipe instance.
     * @return The ID of the inserted recipe.
     */
    private int insertInRecipesTable(
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
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        } finally {
            writeableDatabase.endTransaction();
        }

        // get id
        try (final Cursor cursor = writeableDatabase.rawQuery(querySql, queryArgs)) {
            cursor.moveToFirst(); // this operation should be guaranteed.

            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        } catch (IllegalArgumentException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }

        return id;
    }

    /**
     * Updates a row in the 'recipes' table.
     * @param writeableDatabase {@link SQLiteDatabase} instance.
     * @param recipeInstance {@link Recipe} instance which holds the new data for the row.
     * @param recipeId ID of the recipe.
     */
    private void updateRecipeRow(
            @NonNull SQLiteDatabase writeableDatabase,
            @NonNull Recipe recipeInstance,
            int recipeId
    ) {
        final String[] updateRecipeStatements = {
                "UPDATE recipes SET name = ? WHERE id = ?;",
                "UPDATE recipes SET body = ? WHERE id = ?;",
                "UPDATE recipes SET budget = ? WHERE id = ?;",
                "UPDATE recipes SET diners = ? WHERE id = ?;"
                // creation_date is not updated
        };
        final Object[][] updateRecipeArgs = {
                {recipeInstance.name, recipeId},
                {recipeInstance.body, recipeId},
                {recipeInstance.budget, recipeId},
                {recipeInstance.diners, recipeId},
                // creation_date is not updated
        };
        writeableDatabase.beginTransaction();
        try {
            for (int i = 0; i < updateRecipeStatements.length; i++) {
                writeableDatabase.execSQL(updateRecipeStatements[i], updateRecipeArgs[i]);
            }
            writeableDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message."));
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
    private void insertInRecipeIngredientsTable(
            @NonNull SQLiteDatabase writeableDatabase,
            @NonNull Recipe recipeInstance,
            int recipeId
    ) {
        final String sql = "INSERT INTO recipe_ingredients" +
                "(recipe_id, ingredient_id, amount, is_measured_in_units) VALUES (?, ?, ?, ?);";

        writeableDatabase.beginTransaction();
        try {
            recipeInstance.getIngredients().forEach((ingredientId, properties) -> {
                final Object[] args = {
                        recipeId,
                        ingredientId,
                        properties.amount,
                        properties.isMeasuredInUnits
                };

                writeableDatabase.execSQL(sql, args);
            });

            writeableDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
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
    private void insertInRecipeUtensilsTable(
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
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
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
    private Optional<Recipe> getRecipeRowData(
            @NonNull SQLiteDatabase database,
            int recipeId
    ) {
        final String sql = "SELECT * FROM recipes WHERE id = ? ORDER BY id ASC LIMIT 1;";
        final String[] args = {String.valueOf(recipeId)};
        Recipe recipe = null;

        try (final Cursor cursor = database.rawQuery(sql, args)) {
            if (!cursor.moveToFirst()) return Optional.empty();

            // get row data (with empty sets and maps)
            final String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            final String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            final HashMap<Integer, Ingredient.InRecipeProperties> ingredients = new HashMap<>();
            final HashSet<Integer> utensils = new HashSet<>();
            final int budget = cursor.getInt(cursor.getColumnIndexOrThrow("budget"));
            final int diners = cursor.getInt(cursor.getColumnIndexOrThrow("diners"));
            final Date creationDate = Date.valueOf(
                    cursor.getString(cursor.getColumnIndexOrThrow("creation_date"))
            );

            recipe = new Recipe(name, body, ingredients, utensils, creationDate, budget, diners);
        } catch (IllegalArgumentException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
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
    private void populateRecipeInstanceIngredients(
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
                final int id =
                        cursor.getInt(cursor.getColumnIndexOrThrow("ingredient_id"));
                final int amount =
                        cursor.getInt(cursor.getColumnIndexOrThrow("amount"));
                final boolean isMeasuredInUnits =
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_measured_in_units")) != 0;

                final Ingredient.InRecipeProperties properties = new Ingredient.InRecipeProperties(
                        getIngredientFromId(id).get(),
                        amount,
                        isMeasuredInUnits
                );

                recipeInstance.putIngredient(id, properties);
            } while (cursor.moveToNext());
        } catch (IllegalArgumentException e) {
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }
    }

    /**
     * Deletes all rows from the 'recipe_ingredients' table that reference a specified recipe row
     * ID.
     * @param database {@link SQLiteDatabase} instance.
     * @param recipeId ID of the recipe.
     */
    private void deleteRecipeIngredientRows(
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
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message."));
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
    private void populateRecipeInstanceUtensils(
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
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message"));
        }
    }

    /**
     * Deletes all rows from the 'recipe_utensils' table that reference a specified recipe row ID.
     * @param database {@link SQLiteDatabase} instance.
     * @param recipeId ID of the recipe.
     */
    private void deleteRecipeUtensilRows(
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
            Log.e("db", Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }
}
