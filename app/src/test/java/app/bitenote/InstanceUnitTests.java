package app.bitenote;

import org.junit.Test;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Ingredient;
import app.bitenote.instances.MeasurementType;
import app.bitenote.instances.Recipe;
import app.bitenote.instances.Utensil;

/**
 * Local unit tests to test the functionality of the {@link app.bitenote.instances} package.
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class InstanceUnitTests {
    @Test
    public void isIngredientNameCorrect() {
        final Ingredient banana = new Ingredient(
                "fruit_banana",
                new MeasurementType(MeasurementType.WEIGHT_TYPE_STRING),
                true
        );

        assertEquals("fruit_banana", banana.fullName);
        assertEquals("banana", banana.name);
    }

    @Test
    public void areIngredientsEqual() {
        final String name = "fruit_banana";
        final MeasurementType measurementType =
                new MeasurementType(MeasurementType.WEIGHT_TYPE_STRING);
        final boolean canBeMeasuredInUnits = true;

        final Ingredient i1 = new Ingredient(name, measurementType, canBeMeasuredInUnits);
        final Ingredient i2 = new Ingredient(name, measurementType, canBeMeasuredInUnits);

        assertEquals(i1, i2);
    }

    @Test
    public void areUtensilsEqual() {
        final String name = "oven";

        final Utensil u1 = new Utensil(name);
        final Utensil u2 = new Utensil(name);

        assertEquals(u1, u2);
    }

    @Test
    public void areMeasurementTypesEqual() {
        final String name1 = MeasurementType.VOLUME_TYPE_STRING;
        final String name2 = MeasurementType.WEIGHT_TYPE_STRING;

        final MeasurementType t1 = new MeasurementType(name1);
        final MeasurementType t2 = new MeasurementType(name1);
        final MeasurementType t3 = new MeasurementType(name2);
        final MeasurementType t4 = new MeasurementType(name2);

        assertEquals(t1, t2);
        assertEquals(t3, t4);
    }

    @Test
    public void areRecipesEqual() {
        final String name = "test_recipe";
        final String body = "This is a recipe body.";
        final Date creationDate = Date.valueOf("2000-01-01");
        final int budget = 25;
        final int diners = 2;

        final Recipe r1 = new Recipe(
                name, body, new HashMap<>(), new HashSet<>(), creationDate, budget, diners
        );
        final Recipe r2 = new Recipe(
                name, body, new HashMap<>(), new HashSet<>(), creationDate, budget, diners
        );

        assertEquals(r1, r2);
    }

    @Test
    public void areInclusionsAndBansCorrect() {
        final RecipeQuery rq = new RecipeQuery();
        final boolean incl1 = rq.includeIngredient(1, true);
        final boolean ban1 = rq.banIngredient(1, true);
        final boolean ban2 = rq.banUtensil(1, true);
        final boolean incl2 = rq.includeUtensil(1, true);

        /// test all operations return true
        assertTrue(incl1);
        assertTrue(ban1);
        assertTrue(ban2);
        assertTrue(incl2);

        /// test bans and inclusion checks
        assertTrue(rq.isIngredientBanned(1));
        assertFalse(rq.isIngredientIncluded(1));
        assertTrue(rq.isUtensilIncluded(1));
        assertFalse(rq.isUtensilBanned(1));

        /// test that random ingredients or utensils are neither present or banned
        assertFalse(rq.isIngredientIncluded(10));
        assertFalse(rq.isIngredientBanned(10));
        assertFalse(rq.isUtensilIncluded(10));
        assertFalse(rq.isUtensilBanned(10));
    }

    @Test
    public void areRecipeQueriesEqual() {
        final int maxBudget = 25;
        final int minDiners = 2;

        final RecipeQuery rq1 = new RecipeQuery(
                new HashMap<>(), new HashMap<>(), maxBudget, minDiners
        );
        rq1.includeIngredient(2, false);
        rq1.includeUtensil(3, false);
        rq1.banIngredient(10, false);
        rq1.banUtensil(4, false);

        final RecipeQuery rq2 = new RecipeQuery(
                new HashMap<>(), new HashMap<>(), maxBudget, minDiners
        );
        rq2.includeIngredient(2, false);
        rq2.includeUtensil(3, false);
        rq2.banIngredient(10, false);
        rq2.banUtensil(4, false);

        assertEquals(rq1, rq2);
    }
}
