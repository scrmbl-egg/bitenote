package app.bitenote.recipes;

import androidx.annotation.NonNull;

import java.util.Optional;

/**
 * Represents an instance of an ingredient and the amount necessary for the recipe.
 * Ingredients can be of different types, like apples, lemons, beef, eggs... etc. These types
 * determine how the ingredient will be measured in the recipe.
 * @see IngredientMeasurementType
 * @author Daniel N.
 */
public class Ingredient {
    /**
     * Type of the ingredient.
     */
    private IngredientType type;

    /**
     * Type of measurement for the ingredient.
     */
    private IngredientMeasurementType measurementType;

    /**
     * Optional alternative name for the ingredient. For example, an ingredient might be of
     * type cheese, but there are many kinds of cheese. In this case, the type would be of cheese,
     * but the alternative name could be "Parmesan Cheese".
     */
    private String alternativeName;

    /**
     * Amount necessary for the recipe. The measurement type determines what this value will
     * represent.
     * @see IngredientMeasurementType
     */
    public float amount;

    /**
     * Basic Ingredient constructor.
     * @param type Ingredient type.
     * @param amount Ingredient amount.
     */
    public Ingredient(@NonNull IngredientType type, float amount) {
        this.alternativeName = null;
        this.type = type;
        this.amount = amount;
        this.measurementType = IngredientMeasurementType.getMeasurementType(type);
    }

    /**
     * Advanced Ingredient constructor. This one allows to specify a name, for more specific
     * ingredients. For example: the cheese type may not be specific enough, so a good name could be
     * "Parmesan Cheese".
     * @param name Name of the ingredient.
     * @param type Ingredient type.
     * @param amount Ingredient amount.
     */
    public Ingredient(@NonNull String name, @NonNull IngredientType type, float amount) {
        this.alternativeName = name;
        this.type = type;
        this.amount = amount;
        this.measurementType = IngredientMeasurementType.getMeasurementType(type);
    }

    public void setType(IngredientType type) {
        this.type = type;
        this.alternativeName = null;
        this.measurementType = IngredientMeasurementType.getMeasurementType(type);
    }

    public String getName() {
        return Optional.ofNullable(alternativeName).orElse(type.toString());
    }
}
