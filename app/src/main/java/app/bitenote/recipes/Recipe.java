package app.bitenote.recipes;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * Represents an instance of a recipe. It stores the text contents of the recipe, its ingredients,
 * and its
 */
public class Recipe {
    /**
     * Name of the recipe.
     */
    public String title;

    /** Body text of the recipe. */
    public String body;

    /**
     * List of ingredients needed to prepare the recipe.
     */
    public List<Ingredient> ingredients;

    /**
     * Utensil tags.
     */
    public EnumSet<Utensil> utensils;

    /**
     * Date when the recipe was created.
     */
    public final Date creationDate;

    /**
     * Creates a new Recipe instance, with its creation date set to the system time.
     * @param title Title of the recipe.
     * @param body Body text of the recipe.
     * @param ingredients Recipe ingredients enum set.
     * @param utensils Recipe utensils enum set.
     */
    public Recipe(
            @NonNull String title,
            @NonNull String body,
            @NonNull List<Ingredient> ingredients,
            @NonNull EnumSet<Utensil> utensils
    ) {
        this.title = title;
        this.body = body;
        this.ingredients = ingredients;
        this.utensils = utensils;
        this.creationDate = new Date(System.currentTimeMillis());
    }
}
