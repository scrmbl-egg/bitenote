package app.bitenote.database;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents an object that allows the database to query recipes.
 * @see BiteNoteSQLiteHelper#getQueriedRecipes(RecipeQuery)
 * @author Daniel N.
 */
public final class RecipeQuery {
    /**
     * The search query the user of the app inputs to find a recipe's name.
     */
    public String nameSearchQuery;

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
    private final HashMap<Integer, Boolean> ingredientQuery;

    /**
     * Map of utensils in the query. The key represents the ID of the utensil, while the
     * value is a {@code boolean} that represents whether the utensil must be present or not. A
     * {@code true} value means the utensil MUST BE PRESENT in the recipe, on the other hand,
     * {@code false} means the utensil is BANNED.
     */
    private final HashMap<Integer, Boolean> utensilQuery;

    /**
     * Basic {@link RecipeQuery} constructor.
     */
    public RecipeQuery() {
        this(
                "",
                new HashMap<>(),
                new HashMap<>(),
                0,
                0
        );
    }

    /**
     * Advanced {@link RecipeQuery} constructor.
     * @param nameSearchQuery Search query user input.
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
            @NonNull String nameSearchQuery,
            @NonNull HashMap<Integer, Boolean> ingredientQuery,
            @NonNull HashMap<Integer, Boolean> utensilQuery,
            int maxBudget,
            int minDiners
    ) {
        this.nameSearchQuery = nameSearchQuery;
        this.ingredientQuery = ingredientQuery;
        this.utensilQuery = utensilQuery;
        this.maxBudget = maxBudget;
        this.minDiners = minDiners;
    }

    /**
     * Gets the present ingredients in the query.
     * @return An array of integers, each representing a present ingredient ID.
     */
    public int[] getPresentIngredients() {
        final ArrayList<Integer> idList = new ArrayList<>();

        ingredientQuery.forEach((ingredientId, isPresent) -> {
            if (!isPresent) return;

            idList.add(ingredientId);
        });

        return idList.stream().mapToInt(Integer::intValue).toArray(); // returns the list as int[]
    }

    /**
     * Gets the banned ingredients in the query.
     * @return An array of integers, each representing a banned ingredient ID.
     */
    public int[] getBannedIngredients() {
        final ArrayList<Integer> idList = new ArrayList<>();

        ingredientQuery.forEach((ingredientId, isPresent) -> {
            if (isPresent) return;

            idList.add(ingredientId);
        });

        return idList.stream().mapToInt(Integer::intValue).toArray(); // returns the list as int[]
    }

    /**
     * Gets the present utensils in the query.
     * @return An array of integers, each representing a present utensil ID.
     */
    public int[] getPresentUtensils() {
        final ArrayList<Integer> idList = new ArrayList<>();

        utensilQuery.forEach((utensilId, isPresent) -> {
            if (!isPresent) return;

            idList.add(utensilId);
        });

        return idList.stream().mapToInt(Integer::intValue).toArray(); // returns the list as int[]
    }

    /**
     * Gets the banned utensils in the query.
     * @return An array of integers, each representing a banned utensil ID.
     */
    public int[] getBannedUtensils() {
        final ArrayList<Integer> idList = new ArrayList<>();

        utensilQuery.forEach((utensilId, isPresent) -> {
            if (isPresent) return;

            idList.add(utensilId);
        });

        return idList.stream().mapToInt(Integer::intValue).toArray(); // returns the list as int[]
    }

    /**
     * Includes (marks as present) an ingredient to the query.
     * @param ingredientId ID of the ingredient.
     * @param overrideBans Determines whether the ingredient should be included regardless of
     * if it's already banned or not.
     * @return {@code true} if the ingredient was successfully included or updated.
     */
    public boolean includeIngredient(int ingredientId, boolean overrideBans) {
        final Boolean previousValue = ingredientQuery.putIfAbsent(ingredientId, true);
        /// previousValue is null if the key/ingredientId was absent

        if (previousValue == null) return true;

        if (overrideBans) {
            ingredientQuery.put(ingredientId, true);
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
        final Boolean previousValue = ingredientQuery.putIfAbsent(ingredientId, false);
        /// previousValue is null if the key/ingredientId was absent

        if (previousValue == null) return true;

        if (overrideInclusions) {
            ingredientQuery.put(ingredientId, false);
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
        final Boolean previousValue = utensilQuery.putIfAbsent(utensilId, true);
        /// previousValue is null if the key/utensilId was absent

        if (previousValue == null) return true;

        if (overrideBans) {
            utensilQuery.put(utensilId, true);
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
        final Boolean previousValue = utensilQuery.putIfAbsent(utensilId, false);
        /// previousValue is null if the key/utensilId was absent

        if (previousValue == null) return true;

        if (overrideInclusions) {
            utensilQuery.put(utensilId, false);
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
        return ingredientQuery.containsKey(ingredientId)
                && Boolean.TRUE.equals(ingredientQuery.get(ingredientId));
    }

    /**
     * Checks whether the ingredient is banned from the query.
     * @param ingredientId ID of the ingredient.
     * @return {@code true} if the ingredient ID is marked as banned from the query.
     */
    public boolean isIngredientBanned(int ingredientId) {
        return ingredientQuery.containsKey(ingredientId)
                && Boolean.FALSE.equals(ingredientQuery.get(ingredientId));
    }

    /**
     * Checks whether the utensil is present in the query.
     * @param utensilId ID of the utensil.
     * @return {@code true} if the utensil ID is marked as present in the query.
     */
    public boolean isUtensilPresent(int utensilId) {
        return utensilQuery.containsKey(utensilId)
                && Boolean.TRUE.equals(utensilQuery.get(utensilId));
    }

    /**
     * Checks whether the utensil is banned from the query.
     * @param utensilId ID of the utensil.
     * @return {@code true} if the utensil ID is marked as banned from the query.
     */
    public boolean isUtensilBanned(int utensilId) {
        return utensilQuery.containsKey(utensilId)
                && Boolean.FALSE.equals(utensilQuery.get(utensilId));
    }

    /**
     * Clears all present ingredients from the query.
     */
    public void clearPresentIngredients() {
        ingredientQuery.forEach(ingredientQuery::remove);
    }

    /**
     * Clears all banned ingredients from the query.
     */
    public void clearBannedIngredients() {
        ingredientQuery.forEach((ingredientId, isPresent) ->
                ingredientQuery.remove(ingredientId, !isPresent)
        );
    }

    /**
     * Clears all ingredients (present or banned) from the query.
     */
    public void clearAllIngredients() {
        ingredientQuery.clear();
    }

    /**
     * Clears all present utensils from the query.
     */
    public void clearPresentUtensils() {
        utensilQuery.forEach(utensilQuery::remove);
    }

    /**
     * Clears all banned utensils from the query.
     */
    public void clearBannedUtensils() {
        utensilQuery.forEach((utensilId, isPresent) ->
                utensilQuery.remove(utensilId, !isPresent)
        );
    }

    /**
     * Clears all the utensils (present or banned) from the query.
     */
    public void clearAllUtensils() {
        utensilQuery.clear();
    }

    /**
     * Gets the {@link String} representation of the SQL query.
     * @return A {@link String} that, if passed to a database, will return a column of all recipe
     * IDs that meet the conditions of the query object.
     */
    @SuppressLint("DefaultLocale")
    String toSQLString() {
        /*
         * This query encapsulates the search of all data contained in the class.
         * First 2 decimal digits are budget and diners.
         * The rest are Strings that do "OR" conditions to list all the possible included and
         * banned ingredients and utensils.
         */
        final String statement = "SELECT id FROM recipes WHERE name LIKE '%%%s%%' AND " +
                "budget <= %d AND diners >= %d AND id IN (SELECT recipe_id FROM " +
                "recipe_ingredients WHERE ingredient_id != 0%s AND ingredient_id != 0%s) AND id " +
                "IN (SELECT recipe_id FROM recipe_utensils WHERE utensil_id != 0%s AND " +
                "utensil_id != 0%s) ORDER BY creation_date DESC;";
        final String includedIngredientStatement = " OR ingredient_id = %d";
        final String bannedIngredientStatement = " OR ingredient_id != %d";
        final String includedUtensilStatement = " OR ingredient_id = %d";
        final String bannedUtensilStatement = " OR ingredient_id != %d";

        final StringBuilder includedIngredientsStrBuilder = new StringBuilder();
        final StringBuilder bannedIngredientsStrBuilder = new StringBuilder();
        final StringBuilder includedUtensilsStrBuilder = new StringBuilder();
        final StringBuilder bannedUtensilsStrBuilder = new StringBuilder();

        /// handle ingredient inclusions and bans
        ingredientQuery.forEach((ingredientId, isPresent) -> {
            if (isPresent) {
                includedIngredientsStrBuilder.append(String.format(
                        includedIngredientStatement,
                        ingredientId
                ));
            } else {
                bannedIngredientsStrBuilder.append(String.format(
                        bannedIngredientStatement,
                        ingredientId
                ));
            }
        });

        /// handle utensil inclusions and bans
        utensilQuery.forEach((utensilId, isPresent) -> {
            if (isPresent) {
                includedUtensilsStrBuilder.append(String.format(
                        includedUtensilStatement,
                        utensilId
                ));
            } else {
                bannedUtensilsStrBuilder.append(String.format(
                        bannedUtensilStatement,
                        utensilId
                ));
            }
        });

        return String.format(
                statement,
                nameSearchQuery,
                maxBudget,
                minDiners,
                includedIngredientsStrBuilder,
                bannedIngredientsStrBuilder,
                includedUtensilsStrBuilder,
                bannedIngredientsStrBuilder
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeQuery that = (RecipeQuery) o;
        return minDiners == that.minDiners
                && maxBudget == that.maxBudget
                && Objects.equals(nameSearchQuery, that.nameSearchQuery)
                && Objects.equals(ingredientQuery, that.ingredientQuery)
                && Objects.equals(utensilQuery, that.utensilQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                nameSearchQuery,
                minDiners,
                maxBudget,
                ingredientQuery,
                utensilQuery
        );
    }
}
