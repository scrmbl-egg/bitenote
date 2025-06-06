package app.bitenote.instances;

import android.util.Log;
import androidx.annotation.NonNull;
import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import app.bitenote.database.BiteNoteSQLiteHelper;
import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Ingredient.InRecipeProperties;

/**
 * Represents an instance of a recipe. It stores the text contents of the recipe, and relevant
 * information that help discriminate recipes when querying them.
 * @author Daniel N.
 */
public class Recipe {
    /**
     * XML recipe tag in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_TAG = "recipe";

    /**
     * XML recipe name tag in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_NAME_TAG = "name";

    /**
     * XML recipe body in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_BODY_TAG = "body";

    /**
     * XML recipe diners in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_DINERS_TAG = "diners";

    /**
     * XML recipe budget in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_BUDGET_TAG = "budget";

    /**
     * XML recipe creation date in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_CREATION_DATE_TAG = "creation_date";

    /**
     * XML recipe ingredient in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_INGREDIENT_TAG = "ingredient";

    /**
     * XML recipe ingredient {@code id} attribute in the {@code res/xml/example_recipes.xml}
     * document.
     */
    public static final String XML_RECIPE_INGREDIENT_ID_ATTRIBUTE = "id";

    /**
     * XML recipe ingredient {@code is_measured_in_units} attribute in the
     * {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_INGREDIENT_IS_MEASURED_IN_UNITS_ATTRIBUTE =
            "is_measured_in_units";

    /**
     * XML recipe utensil tag in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_UTENSIL_TAG = "utensil";

    /**
     * XML recipe utensil {@code id} attribute in the {@code res/xml/example_recipes.xml} document.
     */
    public static final String XML_RECIPE_UTENSIL_ID_ATTRIBUTE = "id";

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
     * Date when the recipe was created. This property determines how instances will be ordered
     * in database queries.
     * @see BiteNoteSQLiteHelper#getQueriedRecipes(RecipeQuery)
     */
    public Date creationDate;

    /**
     * Ingredient HashMap. The key is the ingredient ID, and the value stores the properties of
     * that ingredient.
     */
    private final HashMap<Integer, InRecipeProperties> mIngredients;

    /**
     * Utensil HashSet. Each element is an utensil ID.
     */
    private final HashSet<Integer> mUtensils;

    /**
     * Basic Recipe constructor. Creates a new Recipe instance, with its creation date set to the
     * system time.
     */
    public Recipe() {
        this.name = "";
        this.body = "";
        this.mIngredients = new HashMap<>();
        this.mUtensils = new HashSet<>();
        this.budget = 0;
        this.diners = 1;
        this.creationDate = new Date(System.currentTimeMillis());
    }

    /**
     * Advanced Recipe constructor.
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
            @NonNull HashMap<Integer, InRecipeProperties> ingredients,
            @NonNull HashSet<Integer> utensils,
            @NonNull Date creationDate,
            int budget,
            int diners
    ) {
        this.name = name;
        this.body = body;
        this.mIngredients = ingredients;
        this.mUtensils = utensils;
        this.creationDate = creationDate;
        this.budget = budget;
        this.diners = diners;
    }

    /**
     * Deep copy {@link Recipe} constructor.
     * @param base {@link Recipe} instance to be copied.
     */
    public Recipe(@NonNull Recipe base) {
        this.name = base.name;
        this.body = base.body;
        this.budget = base.budget;
        this.diners = base.diners;
        this.creationDate = Date.valueOf(base.creationDate.toString());

        /// for a true copy of a recipe, maps and sets must be deep copied.
        this.mIngredients = new HashMap<>();
        this.mUtensils = new HashSet<>();
        this.mIngredients.putAll(base.mIngredients);
        this.mUtensils.addAll(base.mUtensils);
    }

    /**
     * Gets the utensils of the recipe.
     * @return An unmodifiable set view of the utensils. Each element represents an ID of an
     * utensil.
     */
    public Set<Integer> getUtensils() {
        return Collections.unmodifiableSet(mUtensils);
    }

    /**
     * Adds an utensil to the recipe.
     * @param utensilId ID of the utensil.
     */
    public void addUtensil(int utensilId) {
        final boolean addedId = mUtensils.add(utensilId);
        if (!addedId) {
            Log.w("recipe", "Attempted to add an utensil that was already present.");
        }
    }

    /**
     * Removes an utensil from the recipe.
     * @param utensilId ID of the utensil.
     */
    public void removeUtensil(int utensilId) {
        final boolean containedId = mUtensils.remove(utensilId);
        if (!containedId) {
            Log.w("recipe", "Attempted to remove an utensil that wasn't present.");
        }
    }

    /**
     * Removes all utensils from the recipe.
     */
    public void clearUtensils() {
        mUtensils.clear();
    }

    /**
     * Checks if an utensil is present in the recipe.
     * @param utensilId ID of the utensil.
     * @return True if the utensil is present.
     */
    public boolean containsUtensil(int utensilId) {
        return mUtensils.contains(utensilId);
    }

    /**
     * Gets the ingredients of the recipe.
     * @return An unmodifiable map view of the ingredients. The key represents the ingredient ID,
     * and the value represents the properties of that ingredient.
     */
    public Map<Integer, InRecipeProperties> getIngredients() {
        return Collections.unmodifiableMap(mIngredients);
    }

    /**
     * Puts an ingredient into the recipe.
     * @param ingredientId ID of the ingredient.
     * @param amount Amount of the ingredient.
     * @implNote Since {@link InRecipeProperties#isMeasuredInUnits} is not specified, it will be
     * {@code false}.
     */
    public void putIngredient(int ingredientId, int amount) {
        mIngredients.put(ingredientId, new InRecipeProperties(amount, false));
    }

    /**
     * Puts an ingredient into the recipe.
     * @param ingredientId ID of the ingredient.
     * @param properties {@link InRecipeProperties} instance, which specifies the properties of
     * the ingredient in the recipe.
     */
    public void putIngredient(int ingredientId, @NonNull InRecipeProperties properties) {
        mIngredients.put(ingredientId, properties);
    }

    /**
     * Removes an ingredient from the recipe.
     * @param ingredientId ID of the ingredient.
     */
    public void removeIngredient(int ingredientId) {
        mIngredients.remove(ingredientId);
    }

    /**
     * Removes all ingredients from the recipe.
     */
    public void clearIngredients() {
        mIngredients.clear();
    }

    /**
     * Checks if an ingredient is present in the recipe.
     * @param ingredientId ID of the ingredient.
     * @return True if the recipe contains the ingredient.
     */
    public boolean containsIngredient(int ingredientId) {
        return mIngredients.containsKey(ingredientId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return budget == recipe.budget
                && diners == recipe.diners
                && Objects.equals(name, recipe.name)
                && Objects.equals(body, recipe.body)
                && Objects.equals(creationDate, recipe.creationDate)
                && Objects.equals(mIngredients, recipe.mIngredients)
                && Objects.equals(mUtensils, recipe.mUtensils);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, body, budget, diners, creationDate, mIngredients, mUtensils);
    }
}
