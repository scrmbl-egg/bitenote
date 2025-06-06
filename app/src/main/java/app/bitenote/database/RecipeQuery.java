package app.bitenote.database;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Represents an object that allows the database to query recipes.
 * @see BiteNoteSQLiteHelper#getQueriedRecipes(RecipeQuery)
 * @author Daniel N.
 */
public class RecipeQuery {
    /**
     * Minimum diners of the recipe.
     */
    public int minDiners;

    /**
     * Maximum budget of the recipe.
     */
    public int maxBudget;

    /**
     * Map of ingredients in the query. The key represents the ID of the ingredient, while the
     * value is a {@code boolean} that represents whether the ingredient must be present or not. A
     * {@code true} value means the ingredient MUST BE PRESENT in the recipe, on the other hand,
     * {@code false} means the ingredient is BANNED.
     */
    private final HashMap<Integer, Boolean> mIngredientQuery;

    /**
     * Map of utensils in the query. The key represents the ID of the utensil, while the
     * value is a {@code boolean} that represents whether the utensil must be present or not. A
     * {@code true} value means the utensil MUST BE PRESENT in the recipe, on the other hand,
     * {@code false} means the utensil is BANNED.
     */
    private final HashMap<Integer, Boolean> mUtensilQuery;

    /**
     * Basic {@link RecipeQuery} constructor.
     */
    public RecipeQuery() {
        this(
                new HashMap<>(),
                new HashMap<>(),
                0,
                1
        );
    }

    /**
     * Advanced {@link RecipeQuery} constructor.
     * @param ingredientQuery {@link HashMap} where the key is an ingredient ID, and the value is a
     * {@code boolean}. If the value is {@code true}, the ingredient ID must be present in the
     * recipe, but if it's {@code false}, it must be banned from the recipe.
     * @param utensilQuery {@link HashMap} where the key is an utensil ID, and the value is a
     * {@code boolean}. If the value is {@code true}, the utensil ID must be present in the
     * recipe, but if it's {@code false}, it must be banned from the recipe.
     * @param maxBudget Maximum budget of the recipe.
     * @param minDiners Minimum diners of the recipe.
     */
    public RecipeQuery(
            @NonNull HashMap<Integer, Boolean> ingredientQuery,
            @NonNull HashMap<Integer, Boolean> utensilQuery,
            int maxBudget,
            int minDiners
    ) {
        this.mIngredientQuery = ingredientQuery;
        this.mUtensilQuery = utensilQuery;
        this.maxBudget = maxBudget;
        this.minDiners = minDiners;
    }

    public RecipeQuery(@NonNull RecipeQuery base) {
        this.maxBudget = base.maxBudget;
        this.minDiners = base.minDiners;

        /// for a true copy of a recipe, maps and sets must be deep copied.
        this.mIngredientQuery = new HashMap<>();
        this.mUtensilQuery = new HashMap<>();
        this.mIngredientQuery.putAll(base.mIngredientQuery);
        this.mUtensilQuery.putAll(base.mUtensilQuery);
    }

    /**
     * Gets the present ingredients in the query.
     * @return A list of integers, each representing a present ingredient ID.
     */
    public List<Integer> getIncludedIngredients() {
        final ArrayList<Integer> idList = new ArrayList<>();

        mIngredientQuery.forEach((ingredientId, isPresent) -> {
            if (!isPresent) return;

            idList.add(ingredientId);
        });

        return Collections.unmodifiableList(idList);
    }

    /**
     * Gets the banned ingredients in the query.
     * @return A list of integers, each representing a banned ingredient ID.
     */
    public List<Integer> getBannedIngredients() {
        final ArrayList<Integer> idList = new ArrayList<>();

        mIngredientQuery.forEach((ingredientId, isPresent) -> {
            if (isPresent) return;

            idList.add(ingredientId);
        });

        return Collections.unmodifiableList(idList);
    }

    /**
     * Gets all ingredients that are in the query, whether they are included or not.
     * @return A list of integers, each representing an utensil ID.
     */
    public List<Integer> getQueriedIngredients() {
        final ArrayList<Integer> idList = new ArrayList<>();

        mIngredientQuery.forEach((ingredientId, isPresent) -> idList.add(ingredientId));

        return Collections.unmodifiableList(idList);
    }

    /**
     * Gets the present utensils in the query.
     * @return A list of integers, each representing a present utensil ID.
     */
    public List<Integer> getIncludedUtensils() {
        final ArrayList<Integer> idList = new ArrayList<>();

        mUtensilQuery.forEach((utensilId, isPresent) -> {
            if (!isPresent) return;

            idList.add(utensilId);
        });

        return Collections.unmodifiableList(idList);
    }

    /**
     * Gets the banned utensils in the query.
     * @return A list of integers, each representing a banned utensil ID.
     */
    public List<Integer> getBannedUtensils() {
        final ArrayList<Integer> idList = new ArrayList<>();

        mUtensilQuery.forEach((utensilId, isPresent) -> {
            if (isPresent) return;

            idList.add(utensilId);
        });

        return Collections.unmodifiableList(idList);
    }

    /**
     * Gets all utensils that are in the query, whether they are included or not.
     * @return A list of integers, each representing an utensil ID.
     */
    public List<Integer> getQueriedUtensils() {
        final ArrayList<Integer> idList = new ArrayList<>();

        mUtensilQuery.forEach((utensilId, utensil) -> idList.add(utensilId));

        return Collections.unmodifiableList(idList);
    }

    /**
     * Includes (marks as present) an ingredient to the query.
     * @param ingredientId ID of the ingredient.
     * @param overrideBans Determines whether the ingredient should be included regardless of
     * if it's already banned or not.
     * @return {@code true} if the ingredient was successfully included or updated.
     */
    public boolean includeIngredient(int ingredientId, boolean overrideBans) {
        final Boolean previousValue = mIngredientQuery.putIfAbsent(ingredientId, true);
        /// previousValue is null if the key/ingredientId was absent

        if (previousValue == null) return true;

        if (overrideBans) {
            mIngredientQuery.put(ingredientId, true);
            return true;
        }

        return false;
    }

    /**
     * Bans an ingredient from the query.
     * @param ingredientId ID of the ingredient.
     * @param overrideInclusions Determines whether the ingredient should be banned regardless of
     * if it's already included or not.
     * @return {@code true} if the ingredient was successfully banned.
     */
    public boolean banIngredient(int ingredientId, boolean overrideInclusions) {
        final Boolean previousValue = mIngredientQuery.putIfAbsent(ingredientId, false);
        /// previousValue is null if the key/ingredientId was absent

        if (previousValue == null) return true;

        if (overrideInclusions) {
            mIngredientQuery.put(ingredientId, false);
            return true;
        }

        return false;
    }

    /**
     * Includes an utensil to the query.
     * @param utensilId ID of the utensil.
     * @param overrideBans Determines whether the utensil should be included regardless of
     * if it's already banned or not.
     * @return {@code true} if the utensil was successfully banned.
     */
    public boolean includeUtensil(int utensilId, boolean overrideBans) {
        final Boolean previousValue = mUtensilQuery.putIfAbsent(utensilId, true);
        /// previousValue is null if the key/utensilId was absent

        if (previousValue == null) return true;

        if (overrideBans) {
            mUtensilQuery.put(utensilId, true);
            return true;
        }

        return false;
    }

    /**
     * Bans an utensil from the query.
     * @param utensilId ID of the ingredient.
     * @param overrideInclusions Determines whether the utensil should be banned regardless of
     * if it's already included or not.
     * @return {@code true} if the utensil was successfully banned.
     */
    public boolean banUtensil(int utensilId, boolean overrideInclusions) {
        final Boolean previousValue = mUtensilQuery.putIfAbsent(utensilId, false);
        /// previousValue is null if the key/utensilId was absent

        if (previousValue == null) return true;

        if (overrideInclusions) {
            mUtensilQuery.put(utensilId, false);
            return true;
        }

        return false;
    }

    /**
     * Checks whether the ingredient is present in the query.
     * @param ingredientId ID of the ingredient.
     * @return {@code true} if the ingredient ID is marked as present in the query.
     */
    public boolean isIngredientPresent(int ingredientId) {
        return mIngredientQuery.containsKey(ingredientId)
                && Boolean.TRUE.equals(mIngredientQuery.get(ingredientId));
    }

    /**
     * Checks whether the ingredient is banned from the query.
     * @param ingredientId ID of the ingredient.
     * @return {@code true} if the ingredient ID is marked as banned from the query.
     */
    public boolean isIngredientBanned(int ingredientId) {
        return mIngredientQuery.containsKey(ingredientId)
                && Boolean.FALSE.equals(mIngredientQuery.get(ingredientId));
    }

    /**
     * Checks whether the utensil is present in the query.
     * @param utensilId ID of the utensil.
     * @return {@code true} if the utensil ID is marked as present in the query.
     */
    public boolean isUtensilPresent(int utensilId) {
        return mUtensilQuery.containsKey(utensilId)
                && Boolean.TRUE.equals(mUtensilQuery.get(utensilId));
    }

    /**
     * Checks whether the utensil is banned from the query.
     * @param utensilId ID of the utensil.
     * @return {@code true} if the utensil ID is marked as banned from the query.
     */
    public boolean isUtensilBanned(int utensilId) {
        return mUtensilQuery.containsKey(utensilId)
                && Boolean.FALSE.equals(mUtensilQuery.get(utensilId));
    }

    /**
     * Clears all included ingredients from the query.
     */
    public void clearIncludedIngredients() {
        mIngredientQuery.forEach((ingredientId, isPresent) ->
                mIngredientQuery.remove(ingredientId, true)
        );
    }

    /**
     * Clears all banned ingredients from the query.
     */
    public void clearBannedIngredients() {
        mIngredientQuery.forEach((ingredientId, isPresent) ->
                mIngredientQuery.remove(ingredientId, false)
        );
    }

    /**
     * Clears all ingredients (present or banned) from the query.
     */
    public void clearAllIngredients() {
        mIngredientQuery.clear();
    }

    /**
     * Clears all present utensils from the query.
     */
    public void clearPresentUtensils() {
        mUtensilQuery.forEach(mUtensilQuery::remove);
    }

    /**
     * Clears all banned utensils from the query.
     */
    public void clearBannedUtensils() {
        mUtensilQuery.forEach((utensilId, isPresent) ->
                mUtensilQuery.remove(utensilId, !isPresent)
        );
    }

    /**
     * Clears all the utensils (present or banned) from the query.
     */
    public void clearAllUtensils() {
        mUtensilQuery.clear();
    }

    /**
     * Gets the {@link String} representation of the SQL query.
     * @return A {@link String} that, if passed to a database raw query, will return a
     * {@link android.database.Cursor} with all recipe IDs that meet the conditions defined in the
     * query object.
     */
    @SuppressLint("DefaultLocale")
    String toSQLString() {
        final List<Integer> includedIngredients = getIncludedIngredients();
        final List<Integer> bannedIngredients = getBannedIngredients();
        final List<Integer> includedUtensils = getIncludedUtensils();
        final List<Integer> bannedUtensils = getBannedUtensils();

        final StringBuilder queryStrBuilder = new StringBuilder("SELECT id FROM recipes WHERE ")
                .append("budget <= ").append(maxBudget)
                .append(" AND diners >= ").append(minDiners);

        /// handle included ingredients
        if (!includedIngredients.isEmpty()) {
            queryStrBuilder.append(" AND id IN (SELECT recipe_id FROM recipe_ingredients WHERE " +
                    "ingredient_id IN (");

            for (int i = 0; i < includedIngredients.size(); i++) {
                if (i > 0) queryStrBuilder.append(",");
                queryStrBuilder.append(includedIngredients.get(i));
            }

            queryStrBuilder.append("))");
        }

        /// handle banned ingredients
        if (!bannedIngredients.isEmpty()) {
            queryStrBuilder.append(" AND id NOT IN (SELECT DISTINCT recipe_id FROM " +
                            "recipe_ingredients WHERE ingredient_id IN (");

            for (int i = 0; i < bannedIngredients.size(); i++) {
                if (i > 0) queryStrBuilder.append(",");
                queryStrBuilder.append(bannedIngredients.get(i));
            }

            queryStrBuilder.append("))");
        }

        /// handle included utensils
        if (!includedUtensils.isEmpty()) {
            queryStrBuilder.append(" AND id IN (SELECT recipe_id FROM recipe_utensils WHERE " +
                    "utensil_id IN (");

            for (int i = 0; i < includedUtensils.size(); i++) {
                if (i > 0) queryStrBuilder.append(",");
                queryStrBuilder.append(includedUtensils.get(i));
            }

            queryStrBuilder.append("))");
        }

        /// handle banned utensils
        if (!bannedUtensils.isEmpty()) {
            queryStrBuilder.append(" AND id NOT IN (SELECT DISTINCT recipe_id FROM " +
                    "recipe_utensils WHERE utensil_id IN (");

            for (int i = 0; i < bannedUtensils.size(); i++) {
                if (i > 0) queryStrBuilder.append(",");
                queryStrBuilder.append(bannedUtensils.get(i));
            }

            queryStrBuilder.append("))");
        }

        queryStrBuilder.append(" ORDER BY creation_date DESC;");

        return queryStrBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeQuery that = (RecipeQuery) o;
        return minDiners == that.minDiners
                && maxBudget == that.maxBudget
                && Objects.equals(mIngredientQuery, that.mIngredientQuery)
                && Objects.equals(mUtensilQuery, that.mUtensilQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                minDiners,
                maxBudget,
                mIngredientQuery,
                mUtensilQuery
        );
    }
}
