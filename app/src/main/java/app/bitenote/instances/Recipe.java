package app.bitenote.instances;

import android.util.Log;
import androidx.annotation.NonNull;
import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    public final Date creationDate;

    /**
     * Ingredient HashMap. The key is the ingredient ID, and the value stores the amount of that
     * ingredient.
     */
    private final HashMap<Integer, Float> ingredients;

    /**
     * Utensil HashSet. Each element is an utensil ID.
     */
    private final HashSet<Integer> utensils;

    /**
     * Basic Recipe constructor. Creates a new Recipe instance, with its creation date set to the
     * system time.
     */
    public Recipe() {
        this.name = "";
        this.body = "";
        this.ingredients = new HashMap<>();
        this.utensils = new HashSet<>();
        this.budget = 0;
        this.diners = 0;
        this.creationDate = new Date(System.currentTimeMillis());
    }

    /**
     * Advanced Recipe constructor.
     *
     * @param name Title of the recipe.
     * @param body Body text of the recipe.
     * @param ingredients Recipe ingredients HashMap.
     * @param creationDate Date when the recipe was created.
     * @param utensils Recipe utensil HashSet.
     * @param budget Necessary budget for the recipe.
     * @param diners Amount of diners the recipe is designed for.
     */
    public Recipe(
            @NonNull String name,
            @NonNull String body,
            @NonNull HashMap<Integer, Float> ingredients,
            @NonNull HashSet<Integer> utensils,
            @NonNull Date creationDate,
            int budget,
            int diners
    ) {
        this.name = name;
        this.body = body;
        this.ingredients = ingredients;
        this.utensils = utensils;
        this.creationDate = creationDate;
        this.budget = budget;
        this.diners = diners;
    }

    /**
     * Gets the utensils of the recipe.
     * @return An unmodifiable set view of the utensils. Each element represents an ID of an
     * utensil.
     */
    public Set<Integer> getUtensils() {
        return Collections.unmodifiableSet(utensils);
    }

    /**
     * Adds an utensil to the recipe.
     * @param utensilId ID of the utensil.
     */
    public void addUtensil(int utensilId) {
        final boolean addedId = utensils.add(utensilId);
        if (!addedId) {
            Log.w(null, "Attempted to add an utensil that was already present.");
        }
    }

    /**
     * Removes an utensil from the recipe.
     * @param utensilId ID of the utensil.
     */
    public void removeUtensil(int utensilId) {
        final boolean containedId = utensils.remove(utensilId);
        if (!containedId) {
            Log.w(null, "Attempted to remove an utensil that wasn't present.");
        }
    }

    /**
     * Removes all utensils from the recipe.
     */
    public void clearUtensils() {
        utensils.clear();
    }

    /**
     * Checks if an utensil is present in the recipe.
     * @param utensilId ID of the utensil.
     * @return True if the utensil is present.
     */
    public boolean containsUtensil(int utensilId) {
        return utensils.contains(utensilId);
    }

    /**
     * Gets the ingredients of the recipe.
     * @return An unmodifiable map view of the ingredients. The key represents the ingredient ID,
     * and the value represents the amount of that ingredient.
     */
    public Map<Integer, Float> getIngredients() {
        return Collections.unmodifiableMap(ingredients);
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
    public void clearIngredients() {
        ingredients.clear();
    }

    /**
     * Checks if an ingredient is present in the recipe.
     * @param ingredientId ID of the ingredient.
     * @return True if the recipe contains the ingredient.
     */
    public boolean containsIngredient(int ingredientId) {
        return ingredients.containsKey(ingredientId);
    }
}
