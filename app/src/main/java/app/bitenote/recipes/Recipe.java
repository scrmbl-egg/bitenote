package app.bitenote.recipes;

import androidx.annotation.NonNull;
import java.util.Date;
import java.util.HashMap;

/**
 * Represents an instance of a recipe. It stores the text contents of the recipe, and relevant
 * information that help discriminate recipes when querying them.
 * @author Daniel N.
 */
public class Recipe {
    /**
     * Name of the recipe.
     */
    public String name;

    /** Body text of the recipe. */
    public String body;

    /**
     * Ingredient HashMap. The key is the ingredient id, and the value stores the amount of that
     * ingredient.
     */
    public HashMap<Integer, Float> ingredients;

    /* fixme: this implementation of saving the recipe's utensils may not be the most ideal, but
     it will do for now */
    /**
     * Utensil flags.
     */
    public short utensilFlags;

    /**
     * Necessary budget for the recipe.
     */
    public short budget;

    /**
     * The amount of diners the recipe is designed for.
     */
    public short diners;

    /**
     * Date when the recipe was created.
     */
    public final Date creationDate;

    /**
     * Creates a new Recipe instance, with its creation date set to the system time.
     *
     * @param name Title of the recipe.
     * @param body Body text of the recipe.
     * @param ingredients Recipe ingredients.
     * @param utensilFlags Recipe utensil flags.
     * @param budget Necessary budget for the recipe.
     * @param diners Amount of diners the recipe is designed for.
     */
    public Recipe(
            @NonNull String name,
            @NonNull String body,
            @NonNull HashMap<Integer, Float> ingredients,
            short utensilFlags,
            short budget,
            short diners
    ) {
        this.name = name;
        this.body = body;
        this.ingredients = ingredients;
        this.utensilFlags = utensilFlags;
        this.budget = budget;
        this.diners = diners;
        this.creationDate = new Date(System.currentTimeMillis());
    }

    // todo: implement functions to add or remove ingredients or utensils
}
