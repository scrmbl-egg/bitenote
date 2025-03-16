package app.bitenote.database;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import androidx.annotation.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.Optional;
import java.util.Stack;

import app.bitenote.R;

/**
 * Helper package class to handle immutable table initialisation and XML parsing.
 * @author Daniel N.
 */
class BiteNoteSQLiteTableHelper {
    static final String INGREDIENT_NAME_DELIMITER = ".";

    /**
     * Ingredient tag name.
     */
    static final String INGREDIENT_TAG = "ingredient";

    /**
     * Ingredient type tag name.
     */
    static final String INGREDIENT_TYPE_TAG = "type";

    /**
     * Ingredient subtype tag name.
     */
    static final String INGREDIENT_SUBTYPE_TAG = "subtype";

    /**
     * Name of the ingredient tag 'name' attribute.
     */
    static final String INGREDIENT_NAME_ATTRIBUTE = "name";

    /**
     * Name of the ingredient tag 'measurement' attribute.
     */
    static final String INGREDIENT_MEASUREMENT_ATTRIBUTE = "measurement";

    /**
     * Name of the ingredient tag 'can_be_measured_in_units' attribute.
     */
    static final String INGREDIENT_CAN_BE_MEASURED_IN_UNITS_ATTRIBUTE =
            "can_be_measured_in_units";

    /**
     * Utensil tag name.
     */
    static final String UTENSIL_TAG = "utensil";

    /**
     * Name of the utensil tag 'name' attribute.
     */
    static final String UTENSIL_NAME_ATTRIBUTE = "name";

    /**
     * Measurement type tag name.
     */
    static final String MEASUREMENT_TYPE_TAG = "type";

    /**
     * Name of the measurement type tag 'name' attribute.
     */
    static final String MEASUREMENT_TYPE_NAME_ATTRIBUTE = "name";

    /**
     * Creates all tables in the database.
     * @param database SQLiteDatabase instance.
     */
    static void createTables(@NonNull SQLiteDatabase database) {
        Log.d(null, "Creating database tables...");

        final String createUtensilsTable = "CREATE TABLE utensils (" +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL" +
                ");";

        final String createMeasurementTypesTable = "CREATE TABLE measurement_types (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL" +
                ");";

        final String createRecipesTable = "CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL," +
                "body MEDIUMTEXT NOT NULL DEFAULT ''," +
                "budget INTEGER NOT NULL," +
                "diners INTEGER NOT NULL," +
                "creation_date DATE NOT NULL" +
                ");";

        final String createIngredientsTable = "CREATE TABLE ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(64) NOT NULL," +
                "measurement_id INTEGER NOT NULL," +
                "can_be_measured_in_units BOOLEAN NOT NULL," +
                "FOREIGN KEY (measurement_id) REFERENCES measurement_types(id)" +
                ");";

        final String createRecipeIngredientsTable = "CREATE TABLE recipe_ingredients (" +
                "recipe_id INTEGER PRIMARY KEY," +
                "ingredient_id INTEGER PRIMARY KEY," +
                "amount FLOAT," +
                "FOREIGN KEY (recipe_id) REFERENCES recipes(id)," +
                "FOREIGN KEY (ingredient_id) REFERENCES ingrs(id)" +
                ");";

        final String createRecipeUtensilsTable = "CREATE TABLE recipe_utensils (" +
                "recipe_id INTEGER PRIMARY KEY," +
                "utensil_id INTEGER PRIMARY KEY," +
                "FOREIGN KEY (recipe_id) REFERENCES recipes(id)," +
                "FOREIGN KEY (utensil_id) REFERENCES utensils(id)" +
                ");";

        database.beginTransaction();
        try {
            database.execSQL(createUtensilsTable);
            database.execSQL(createMeasurementTypesTable);
            database.execSQL(createRecipesTable);
            database.execSQL(createIngredientsTable);
            database.execSQL(createRecipeIngredientsTable);
            database.execSQL(createRecipeUtensilsTable);

            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Drops all tables in the database.
     * @param database SQLiteDatabase instance.
     */
    static void dropTables(@NonNull SQLiteDatabase database) {
        String dropUtensilsTable = "DROP TABLE IF EXISTS utensils;";
        String dropMeasurementTypesTable = "DROP TABLE IF EXISTS measurement_types;";
        String dropRecipesTable = "DROP TABLE IF EXISTS recipes;";
        String dropIngredientsTable = "DROP TABLE IF EXISTS ingredients;";
        String dropRecipeIngredientsTable = "DROP TABLE IF EXISTS recipe_ingredients;";
        String dropRecipeUtensilsTable = "DROP TABLE IF EXISTS recipe_utensils;";

        database.beginTransaction();
        try {
            database.execSQL(dropUtensilsTable);
            database.execSQL(dropMeasurementTypesTable);
            database.execSQL(dropRecipesTable);
            database.execSQL(dropIngredientsTable);
            database.execSQL(dropRecipeIngredientsTable);
            database.execSQL(dropRecipeUtensilsTable);

            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Populates the immutable tables of the database with XML resource data.
     * @param database SQLite database instance.
     * @param context Context.
     */
    static void populateImmutableTables(
            @NonNull SQLiteDatabase database,
            @NonNull Context context
    ) {
        Log.d(null, "Populating immutable tables...");

        populateUtensilsTable(database, context);
        populateMeasurementTypesTable(database, context);
        populateIngredientsTable(database, context);
    }

    /**
     * Populates the 'utensils' SQLite table with the XML data.
     * @param database SQLite database instance.
     * @param context Context.
     */
    private static void populateUtensilsTable(
            @NonNull SQLiteDatabase database,
            @NonNull Context context
    ) {
        final String sql = "INSERT INTO utensils(name) VALUES (?);";

        database.beginTransaction();
        try (final XmlResourceParser parser = context.getResources().getXml(R.xml.utensils)) {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    parser.next();
                    continue;
                }

                String tag = parser.getName();
                if (!tag.equals(UTENSIL_TAG)) {
                    parser.next();
                    continue;
                }

                // alloc array for sql args
                Object[] args = {
                        parser.getAttributeValue(null, UTENSIL_NAME_ATTRIBUTE)
                };

                database.execSQL(sql, args);
            }

            parser.next();
            database.setTransactionSuccessful();
        } catch (XmlPullParserException | IOException | SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Populates the 'measurement_types' SQLite table with the XML data.
     * @param database SQLite database instance.
     * @param context Context.
     */
    private static void populateMeasurementTypesTable(
            @NonNull SQLiteDatabase database,
            @NonNull Context context
    ) {
        final String sql = "INSERT INTO measurement_types(name) VALUES (?);";

        database.beginTransaction();
        try (final XmlResourceParser parser = context.getResources().getXml(R.xml.utensils)) {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    parser.next();
                    continue;
                }

                String tag = parser.getName();
                if (!tag.equals(MEASUREMENT_TYPE_TAG)) {
                    parser.next();
                    continue;
                }

                // alloc array for sql args
                Object[] args = {
                        parser.getAttributeValue(null, MEASUREMENT_TYPE_NAME_ATTRIBUTE)
                };

                database.execSQL(sql, args);
            }

            parser.next();
            database.setTransactionSuccessful();
        } catch (XmlPullParserException | IOException | SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Populates the 'ingredients' SQLiteTable with the XML data.
     * @param database SQLite database instance.
     * @param context Context.
     */
    private static void populateIngredientsTable(
            @NonNull SQLiteDatabase database,
            @NonNull Context context
    ) {
        /*
        Ingredients are a special case when it comes to parsing their names. The name of an
        ingredient should be prefixed with its type and/or subtype. For example:
        for salmon, the name should be: "seafood.fish.salmon".
        That is why, in this function we use a  stack to store the type, subtype and
        ingredient name, so they can be joined.
        */

        final Stack<String> strStack = new Stack<>();
        final String sql = "INSERT INTO ingredients" +
                "(name, measurement_id, can_be_measured_in_units) VALUES (?, ?, ?);";

        database.beginTransaction();
        try (final XmlResourceParser parser = context.getResources().getXml(R.xml.ingredients)) {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    parser.next();
                    continue;
                }

                switch (parser.getName()) {
                    case INGREDIENT_TAG:
                        strStack.push(parser.getAttributeValue(null, INGREDIENT_NAME_ATTRIBUTE));
                        break;
                    case INGREDIENT_TYPE_TAG:
                        strStack.clear();
                    case INGREDIENT_SUBTYPE_TAG:
                        strStack.push(parser.getAttributeValue(null, INGREDIENT_NAME_ATTRIBUTE));
                    default:
                        parser.next();
                        continue;
                }

                Object[] args = {
                        String.join(INGREDIENT_NAME_DELIMITER, strStack),
                        getMeasurementTypeIdFromName(
                                database,
                                parser.getAttributeValue(null, INGREDIENT_MEASUREMENT_ATTRIBUTE)
                        ).orElse(0),
                        parser.getAttributeBooleanValue(
                                null,
                                INGREDIENT_CAN_BE_MEASURED_IN_UNITS_ATTRIBUTE,
                                false
                        )
                };

                strStack.pop();
                database.execSQL(sql, args);
                parser.next();
            }

            database.setTransactionSuccessful();
        } catch (XmlPullParserException | IOException | SQLException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Gets the measurement type ID from its name.
     * @param database SQLiteDatabase instance.
     * @param measurementTypeName Name of the measurement type. This should be obtained from an XML.
     * @return Optional that may contain the ID of the measurement type.
     */
    private static Optional<Integer> getMeasurementTypeIdFromName(
            @NonNull SQLiteDatabase database,
            String measurementTypeName
    ) {
        final String sql = "SELECT id FROM measurement_types WHERE name = ?;";
        final String[] args = {
            measurementTypeName
        };
        Integer result = null;

        try (final Cursor cursor = database.rawQuery(sql, args)) {
            if (!cursor.moveToFirst()) {
                return Optional.empty();
            }

            result = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        } catch (IllegalArgumentException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
        }

        return Optional.ofNullable(result);
    }
}
