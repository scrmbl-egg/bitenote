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
    public void recipeInsertionAndRetrievalIsCorrect() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try (
                final BiteNoteSQLiteHelper sqliteHelper =
                     new BiteNoteSQLiteHelper(TEST_DATABASE_NAME, appContext)
        ) {
            assertEquals(0, sqliteHelper.getRecipeCount());

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
            r.putIngredient(1, 2);
            r.putIngredient(3, 2);

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
                    r.getIngredients().keySet().toArray(new Integer[0]),
                    rOption.get().getIngredients().keySet().toArray(new Integer[0])
            );
            assertArrayEquals(
                    r.getIngredients().values().toArray(),
                    rOption.get().getIngredients().values().toArray()
            );
            assertArrayEquals(
                    r.getUtensils().toArray(),
                    rOption.get().getUtensils().toArray()
            );

            sqliteHelper.deleteRecipe(rId);

            final Optional<Recipe> secondRecipeOptional = sqliteHelper.getRecipeFromId(rId);
            assertTrue(secondRecipeOptional.isEmpty());
        }
    }

    @Test
    public void recipeInsertionAndUpdatingIsCorrect() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try (
                final BiteNoteSQLiteHelper sqliteHelper =
                        new BiteNoteSQLiteHelper(TEST_DATABASE_NAME, appContext)
        ) {
            final Recipe r1 = new Recipe(
                    "test_recipe",
                    "This is a recipe body.",
                    new HashMap<>(),
                    new HashSet<>(),
                    Date.valueOf("2020-2-2"),
                    25,
                    2
            );

            final int r1Id = sqliteHelper.insertRecipe(r1);

            final Recipe r2 = new Recipe(
                    "test_recipe_2",
                    "This is another recipe body.",
                    new HashMap<>(),
                    new HashSet<>(),
                    Date.valueOf("2022-2-2"),  // this field shouldn't modify the original
                    30,
                    3
            );
            r2.addUtensil(1);
            r2.addUtensil(2);
            r2.putIngredient(1, 1);
            r2.putIngredient(2, 2);

            sqliteHelper.updateRecipe(r1Id, r2);
            final Optional<Recipe> r2Option = sqliteHelper.getRecipeFromId(r1Id);

            assertTrue(r2Option.isPresent());

            assertEquals(r2.name, r2Option.get().name);
            assertEquals(r2.body, r2Option.get().body);
            assertEquals(r2.budget, r2Option.get().budget);
            assertEquals(r2.diners, r2Option.get().diners);
            assertNotEquals(r2.creationDate, r2Option.get().creationDate); // assert no modification
            assertArrayEquals(
                    r2.getIngredients().keySet().toArray(new Integer[0]),
                    r2Option.get().getIngredients().keySet().toArray(new Integer[0])
            );
            assertArrayEquals(
                    r2.getIngredients().values().toArray(),
                    r2Option.get().getIngredients().values().toArray()
            );
            assertArrayEquals(
                    r2.getUtensils().toArray(),
                    r2Option.get().getUtensils().toArray()
            );

            sqliteHelper.deleteRecipe(r1Id);
            final Optional<Recipe> deletedRecipeOption = sqliteHelper.getRecipeFromId(r1Id);
            assertTrue(deletedRecipeOption.isEmpty());
        }
    }

    @Test
    public void areExampleRecipesInserted() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try (
                final BiteNoteSQLiteHelper sqliteHelper =
                     new BiteNoteSQLiteHelper(TEST_DATABASE_NAME, appContext)
        ) {
            assertEquals(0, sqliteHelper.getRecipeCount());

            final int[] exampleRecipeIds = sqliteHelper.insertExampleRecipes();
            final int[] allRecipeIds =
                    Arrays.stream(sqliteHelper.getAllRecipes())
                    .mapToInt((pair) -> pair.first)
                    .toArray();

            /// assert that, if no other recipes are added, all ids should be the same.
            assertArrayEquals(allRecipeIds, exampleRecipeIds);

            final Optional<Recipe> recipeOption =
                    sqliteHelper.getRecipeFromId(exampleRecipeIds[0]);

            assertTrue(recipeOption.isPresent());

            assertEquals("This is a test recipe", recipeOption.get().name);
            assertEquals("This is a test recipe body.", recipeOption.get().body);
            assertEquals(Date.valueOf("1970-1-1"), recipeOption.get().creationDate);
            assertEquals(2, recipeOption.get().diners);
            assertEquals(10, recipeOption.get().budget);
            assertArrayEquals(
                    new Integer[] {1, 2, 3},
                    recipeOption.get().getIngredients().keySet().toArray(new Integer[0])
            );
            assertArrayEquals(
                    new Float[] {1f, 2f, 3f},
                    recipeOption.get().getIngredients().values().toArray()
            );
            assertArrayEquals(
                    new Integer[] {1, 2},
                    recipeOption.get().getUtensils().toArray()
            );

            sqliteHelper.deleteRecipes(exampleRecipeIds);
            assertEquals(0, sqliteHelper.getRecipeCount());
        }
    }
}
