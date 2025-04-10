package app.bitenote.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import app.bitenote.R;
import app.bitenote.instances.Recipe;

/**
 * Adapter for displaying {@link Recipe} data in a {@link RecyclerView} with cards.
 * @see RecipeViewHolder
 * @author Daniel N.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final Pair<Integer, Recipe>[] recipes;

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_card, parent, false);

        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        final int id = recipes[position].first;
        final Recipe recipe = recipes[position].second;

        holder.bind(id, recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.length;
    }

    /**
     * Recipe adapter constructor.
     * @param recipes Array of {@link Pair}s, where the first element is the recipe's ID, and the
     */
    public RecipeAdapter(Pair<Integer, Recipe>[] recipes) {
        this.recipes = recipes;
    }

    /**
     * View holder for recipes.
     * @see RecipeAdapter
     * @author Daniel N.
     */
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        /**
         * Recipe ID the view holder references.
         */
        private int recipeId;

        /**
         * {@link TextView} instance that references the name of the recipe in the card.
         * @see Recipe#name
         */
        private final TextView nameTextView;

        /**
         * {@link TextView} instance that references the body of the recipe in the card.
         * @see Recipe#body
         */
        private final TextView bodyTextView;

        /**
         * Recipe view holder constructor.
         * @param itemView {@link View} instance.
         */
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            /// init views
            this.nameTextView = itemView.findViewById(R.id.recipe_name_text_view);
            this.bodyTextView = itemView.findViewById(R.id.recipe_body_text_view);
        }

        /**
         * Binds recipe data to the view.
         * @param recipeId ID of the database recipe.
         * @param recipe {@link Recipe} instance that holds the new data.
         */
        public void bind(int recipeId, @NonNull Recipe recipe) {
            this.recipeId = recipeId;
            nameTextView.setText(recipe.name);
            bodyTextView.setText(recipe.body);

            itemView.setContentDescription(itemView.getContentDescription() + ": " + recipe.name);
            /// :-(
        }
    }
}
