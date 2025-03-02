package app.bitenote.database;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.Optional;

import app.bitenote.R;

/**
 * Helper package class to handle immutable table initialisation and XML parsing.
 */
class BiteNoteSQLiteTableHelper {
    /**
     * Ingredient tag name.
     */
    static final String INGREDIENT_TAG = "ingredient";

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

        final String createRecipeIngrsTable = "CREATE TABLE recipeIngrs (" +
                "recipe_id INTEGER PRIMARY KEY," +
                "ingredient_id INTEGER PRIMARY KEY," +
                "amount FLOAT," +
                "FOREIGN KEY (recipe_id) REFERENCES recipes(id)," +
                "FOREIGN KEY (ingredient_id) REFERENCES ingrs(id)" +
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
        database.execSQL(createRecipeIngrsTable);
        database.execSQL(createRecipeUtensilsTable);
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
        final XmlResourceParser parser = context.getResources().getXml(R.xml.utensils);

        try {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    parser.next();
                    continue;
                }

                String tag = parser.getName();
                if (!tag.equals(UTENSIL_TAG)) continue;

                // alloc array for sql args
                Object[] args = {
                        parser.getAttributeValue(null, UTENSIL_NAME_ATTRIBUTE)
                };

                database.execSQL(sql, args);
            }
        } catch (XmlPullParserException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Populates the 'measurementTypes' SQLite table with the XML data.
     * @param database SQLite database instance.
     * @param context Context.
     */
    private static void populateMeasurementTypesTable(
            @NonNull SQLiteDatabase database,
            @NonNull Context context
    ) {
        final String sql = "INSERT INTO measurementTypes(name) VALUES (?);";
        final XmlResourceParser parser = context.getResources().getXml(R.xml.utensils);

        try {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    parser.next();
                    continue;
                }

                String tag = parser.getName();
                if (!tag.equals(MEASUREMENT_TYPE_TAG)) continue;

                // alloc array for sql args
                Object[] args = {
                        parser.getAttributeValue(null, MEASUREMENT_TYPE_NAME_ATTRIBUTE)
                };

                database.execSQL(sql, args);
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(null, Optional.ofNullable(e.getMessage()).orElse("Missing message."));
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
        // todo: populate ingredients table.
    }
}
