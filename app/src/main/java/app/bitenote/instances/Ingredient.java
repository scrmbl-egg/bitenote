package app.bitenote.instances;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;

/**
 * A container for data of one ingredient in the database. The data in {@link Ingredient} instances
 * can't be changed, because every possible ingredient is pre-defined and immutable.
 * @see app.bitenote.database.BiteNoteSQLiteHelper#getIngredientFromId(int)
 * @author Daniel N.
 */
public final class Ingredient {
    /**
     * Ingredient name delimiter.
     */
    public static final String NAME_DELIMITER = ".";

    /**
     * XML ingredient tag in the {@code res/xml/ingredients.xml} document.
     */
    public static final String XML_TAG = "ingredient";

    /**
     * XML ingredient type tag in the {@code res/xml/ingredients.xml} document.
     */
    public static final String XML_TYPE_TAG = "type";

    /**
     * XML ingredient {@code name} attribute in the {@code res/xml/ingredients.xml} document.
     */
    public static final String XML_NAME_ATTRIBUTE = "name";

    /**
     * XML ingredient type {@code name} attribute in the {@code res/xml/ingredients.xml} document.
     */
    public static final String XML_TYPE_NAME_ATTRIBUTE = "name";

    /**
     * XML ingredient subtype tag in the {@code res/xml/ingredients.xml} document.
     */
    public static final String XML_SUBTYPE_TAG = "subtype";

    /**
     * XML ingredient subtype {@code name} attribute in the {@code res/xml/ingredients.xml}
     * document.
     */
    public static final String XML_SUBTYPE_NAME_ATTRIBUTE = "name";

    /**
     * XML ingredient {@code measurement} attribute in the {@code res/xml/ingredients.xml} document.
     */
    public static final String XML_MEASUREMENT_TYPE_ATTRIBUTE = "measurement";

    /**
     * XML ingredient {@code can_be_measured_in_units} attribute in the
     * {@code res/xml/ingredients.xml} document.
     */
    public static final String XML_CAN_BE_MEASURED_IN_UNITS_ATTRIBUTE = "can_be_measured_in_units";

    /**
     * Ingredient's full name. For example: "seafood.fish.salmon".
     */
    public final String fullName;

    /**
     * Ingredient's regular name. For example: "salmon".
     */
    public final String name;

    /**
     * Measurement type ID.
     */
    public final int measurementTypeId;

    /**
     * Determines whether the ingredient can be measured in units. For example: "1 turkey" is a
     * valid measurement, while "1 salt" is not.
     */
    public final boolean canBeMeasuredInUnits;

    /**
     * A basic Ingredient instance constructor.
     * @param fullName Full name of the ingredient (includes type and subtype).
     * @param measurementTypeId ID of the measurement type.
     * @param canBeMeasuredInUnits Determines whether the ingredient can be measured in units.
     * @implNote Using this constructor won't add a row in the 'ingredients' database table.
     * @see app.bitenote.database.BiteNoteSQLiteHelper#getIngredientFromId(int)
     */
    public Ingredient(
            @NonNull String fullName,
            int measurementTypeId,
            boolean canBeMeasuredInUnits
    ) {
        this.fullName = fullName;
        this.measurementTypeId = measurementTypeId;
        this.canBeMeasuredInUnits = canBeMeasuredInUnits;

        /*
         * To reduce computations, it is better to have a field with the regular name rather than
         * splitting the full name each time a "getName" function is called.
         */

        final Stack<String> fullNameStack = new Stack<>();
        fullNameStack.addAll(Arrays.asList(fullName.split("\\" + NAME_DELIMITER)));
        this.name = fullNameStack.pop();
    }

    @Override
    public boolean equals(Object o) {
        /*
         * Since Ingredients are immutable, two references with the same data should be considered
         * equal.
         */

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return measurementTypeId == that.measurementTypeId
                && canBeMeasuredInUnits == that.canBeMeasuredInUnits
                && Objects.equals(fullName, that.fullName)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, name, measurementTypeId, canBeMeasuredInUnits);
    }

    /**
     * Inner {@link Ingredient} static class that is used to store the properties of an ingredient
     * when it's present in a {@link Recipe} instance.
     */
    public static class InRecipeProperties {
        /**
         * Amount of the ingredient in the recipe.
         * @see Ingredient#measurementTypeId
         * @see MeasurementType
         */
        public int amount;

        /**
         * Determines whether the ingredient is being measured in units. If the ingredient can't
         * be measured in units, this field defaults to {@code false}.
         * @see Ingredient#canBeMeasuredInUnits
         */
        public boolean isMeasuredInUnits;

        /**
         * Basic properties constructor.
         * @param amount Amount of the ingredient in the recipe.
         * @param isMeasuredInUnits Determines whether the ingredient is being measured in units.
         * @implNote This constructor doesn't check whether the ingredient can actually be measured
         * in units. Use the following constructor instead for checking:
         * {@link InRecipeProperties#InRecipeProperties(Ingredient, int, boolean)}}
         * @see Ingredient#canBeMeasuredInUnits
         */
        public InRecipeProperties(int amount, boolean isMeasuredInUnits) {
            this.amount = amount;
            this.isMeasuredInUnits = isMeasuredInUnits;
        }

        /**
         * Properties constructor. Does additional checks.
         * @param ingredient {@link Ingredient} instance which the properties reference.
         * @param amount Amount of the ingredient in the recipe.
         * @param isMeasuredInUnits Determines whether the ingredient is being measured in units.
         * Will default to {@code false} if the ingredient can't be measured in units.
         * @see Ingredient#canBeMeasuredInUnits
         */
        public InRecipeProperties(
                @NonNull Ingredient ingredient,
                int amount,
                boolean isMeasuredInUnits
        ) {
            new InRecipeProperties(
                    amount,
                    ingredient.canBeMeasuredInUnits && isMeasuredInUnits
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InRecipeProperties that = (InRecipeProperties) o;
            return amount == that.amount
                    && isMeasuredInUnits == that.isMeasuredInUnits;
        }

        @Override
        public int hashCode() {
            return Objects.hash(amount, isMeasuredInUnits);
        }
    }
}
