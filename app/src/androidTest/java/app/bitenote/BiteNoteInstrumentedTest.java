package app.bitenote;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.*;

import app.bitenote.database.BiteNoteSQLiteHelper;
import app.bitenote.instances.Recipe;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BiteNoteInstrumentedTest {
    private static final String TEST_DATABASE_NAME = "test_db.db";

    @Test
    public void useAppContext() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("app.bitenote", appContext.getPackageName());
    }

    @Test
    public void rowCountsAreNotZero() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try (
                final BiteNoteSQLiteHelper sqliteHelper =
                        new BiteNoteSQLiteHelper(TEST_DATABASE_NAME, appContext)
        ) {
            assertNotEquals(0, sqliteHelper.getUtensilCount());
            assertNotEquals(0, sqliteHelper.getMeasurementTypeCount());
            assertNotEquals(0, sqliteHelper.getIngredientCount());
        }
    }

    @Test
    public void insertAndRetrieveRecipe() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try (
                final BiteNoteSQLiteHelper sqliteHelper =
                     new BiteNoteSQLiteHelper(TEST_DATABASE_NAME, appContext)
        ) {
            final Recipe r = new Recipe(
                    "test_recipe",
                    "This is a recipe body.",
                    new HashMap<>(),
                    new HashSet<>(),
                    new Date(System.currentTimeMillis()),
                    25,
                    2
            );
            r.addUtensil(2);
            r.addUtensil(3);
            r.putIngredient(1, 2f);
            r.putIngredient(3, 2f);

            final int rId = sqliteHelper.insertRecipe(r);
            final Optional<Recipe> rOption = sqliteHelper.getRecipeFromId(rId);

            /// first, check if the recipe is present at all
            assertTrue(rOption.isPresent());

            /*
             * References with the same data will not be considered equal, so we manually assert
             * the equality of all recipe fields.
             */
            assertEquals(r.name, rOption.get().name);
            assertEquals(r.body, rOption.get().body);
            assertEquals(r.budget, rOption.get().budget);
            assertEquals(r.diners, rOption.get().diners);
            assertArrayEquals(
                    r.getIngredients().values().toArray(),
                    rOption.get().getIngredients().values().toArray()
            );
            assertArrayEquals(
                    r.getUtensils().toArray(),
                    rOption.get().getUtensils().toArray()
            );
        }
    }
}
