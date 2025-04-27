package app.bitenote.adapters;

import androidx.annotation.NonNull;

import app.bitenote.instances.Recipe;

/**
 * Interface that determines what a recipe card will do when clicked.
 * @author Daniel N.
 */
public interface OnRecipeCardClickListener {
    /**
     * Function that will be called when a recipe card is clicked.
     * @param recipeId ID of the recipe in the database.
     * @param recipe Instance of {@link Recipe} that wraps the data.
     */
    void onRecipeCardClick(int recipeId, @NonNull Recipe recipe);
}
