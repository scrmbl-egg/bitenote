package app.bitenote;

import org.junit.Test;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

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
    public void areIngredientsEqual() {
        final String name = "fruit.banana";
        final int measurementTypeId = 1;
        final boolean canBeMeasuredInUnits = true;

        final Ingredient i1 = new Ingredient(name, measurementTypeId, canBeMeasuredInUnits);
        final Ingredient i2 = new Ingredient(name, measurementTypeId, canBeMeasuredInUnits);

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
        final String name = "volume";

        final MeasurementType t1 = new MeasurementType(name);
        final MeasurementType t2 = new MeasurementType(name);

        assertEquals(t1, t2);
    }

    @Test
    public void areRecipesNotEqual() {
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

        /// Recipes, despite having the same data, should not be considered equal.
        assertNotEquals(r1, r2);
    }
}
