package app.bitenote.recipes;

import androidx.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

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
     * Necessary budget for the recipe.
     */
    public int budget;

    /**
     * The amount of diners the recipe is designed for.
     */
    public int diners;

    /**
     * Date when the recipe was created.
     */
    public final Calendar creationDate;

    /**
     * Ingredient HashMap. The key is the ingredient id, and the value stores the amount of that
     * ingredient.
     */
    private final HashMap<Integer, Float> ingredients;

    /**
     * Utensil flags.
     */
    private int utensilFlags;

    /**
     * Basic Recipe constructor. Creates a new Recipe instance, with its creation date set to the
     * system time.
     */
    public Recipe() {
        this.name = "";
        this.body = "";
        this.ingredients = new HashMap<>();
        this.utensilFlags = 0;
        this.budget = 0;
        this.diners = 0;
        this.creationDate = Calendar.getInstance();
    }

    /**
     * Advanced Recipe constructor.
     *
     * @param name Title of the recipe.
     * @param body Body text of the recipe.
     * @param ingredients Recipe ingredients.
     * @param creationDate Date when the recipe was created.
     * @param utensilFlags Recipe utensil flags.
     * @param budget Necessary budget for the recipe.
     * @param diners Amount of diners the recipe is designed for.
     */
    public Recipe(
            @NonNull String name,
            @NonNull String body,
            @NonNull HashMap<Integer, Float> ingredients,
            @NonNull Calendar creationDate,
            int utensilFlags,
            int budget,
            int diners
    ) {
        this.name = name;
        this.body = body;
        this.ingredients = ingredients;
        this.creationDate = creationDate;
        this.utensilFlags = utensilFlags;
        this.budget = budget;
        this.diners = diners;
    }

    /**
     * Gets the creation date in a format which can be passed as an SQL argument.
     * @return A string representation of the creation date in the "yyyy-MM-dd" format.
     */
    public String getCreationDateAsSQLDateString() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        );

        return simpleDateFormat.format(creationDate.getTime());
    }

    /**
     * Adds an utensil to the recipe.
     * @param utensilId ID of the utensil to be added.
     */
    public void addUtensil(int utensilId) {
        utensilFlags |= 1 << utensilId;
    }

    /**
     * Removes an utensil from the recipe.
     * @param utensilId ID of the utensil to be removed.
     */
    public void removeUtensil(int utensilId) {
        utensilFlags &= ~(1 << utensilId);
    }

    /**
     * Removes all utensils from the recipe.
     */
    public void clearUtensils() {
        utensilFlags = 0;
    }

    /**
     * Checks if an utensil is present in the recipe.
     * @param utensilId ID of the utensil.
     * @return True if the utensil is present.
     */
    public boolean containsUtensil(int utensilId) {
        return (utensilFlags & 1 << utensilId) != 0;
    }

    /**
     * Puts an ingredient into the recipe.
     * @param ingredientId ID of the ingredient.
     * @param amount Ingredient amount.
     */
    public void putIngredient(int ingredientId, float amount) {
        ingredients.put(ingredientId, amount);
    }

    /**
     * Removes an ingredient from the recipe.
     * @param ingredientId ID of the ingredient.
     */
    public void removeIngredient(int ingredientId) {
        ingredients.remove(ingredientId);
    }

    /**
     * Removes all ingredients from the recipe.
     */
    public void removeAllIngredients() {
        ingredients.clear();
    }
}
