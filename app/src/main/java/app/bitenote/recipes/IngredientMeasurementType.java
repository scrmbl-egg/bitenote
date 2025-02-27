package app.bitenote.recipes;

import androidx.annotation.NonNull;

/**
 * Represents how the ingredient will be measured (units, g/kg or mL/L).
 * @author Daniel N.
 */
public enum IngredientMeasurementType {
    /**
     * No measurement type enumeration.
     */
    NONE,

    /**
     * Measurement type for ingredients that are measured in units, or fractions of said units.
     * For example: 1 apple, 1/2 an orange.
     */
    UNITS_AND_FRACTIONS,

    /**
     * Measurement type for ingredients that are measured in weight.
     * For example: 200g of sugar, 1kg of beef.
     */
    WEIGHT,

    /**
     * Measurement type for ingredients that are measured in volume. For example:
     * 1L of milk, 250mL of water.
     */
    VOLUME;

    /**
     * Gets the measurement type of the passed ingredient type.
     * @param type Type of ingredient.
     * @return Measurement type.
     */
    public static IngredientMeasurementType getMeasurementType(@NonNull IngredientType type) {
        switch (type) {
            case APPLE:
            case AVOCADO:
            case BANANA:
            case BELL_PEPPERS:
            case BERRIES:
            case BROCCOLI:
            case CARROT:
            case EGG:
            case LEMON:
            case MUSHROOM:
            case NUTS:
            case ONION:
            case POTATO:
            case TOMATO:
                return UNITS_AND_FRACTIONS;
            case BEEF:
            case BEANS:
            case CELERY:
            case CHEESE:
            case CHICKEN:
            case CHOCOLATE:
            case FISH:
            case GARLIC:
            case LETTUCE:
            case PASTA:
            case PORK:
            case RICE:
            case TOFU:
            case VANILLA:
                return WEIGHT;
            case BUTTER:
            case HONEY:
            case MILK:
            case WATER:
                return VOLUME;
        }

        return NONE;
    }
}
