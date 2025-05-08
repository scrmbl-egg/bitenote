package app.bitenote.adapters;

import android.annotation.SuppressLint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;

import app.bitenote.R;
import app.bitenote.database.BiteNoteSQLiteHelper;
import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Recipe;

/**
 * Adapter for displaying {@link Recipe} data in a {@link RecyclerView} with cards.
 * @see RecipeViewHolder
 * @author Daniel N.
 */
public final class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    /**
     * Array of recipes in the adapter. The first element of the pair represents the
     * integer ID of the recipe in the database, and the second element represents the data of that
     * recipe, wrapped in a {@link Recipe} instance.
     */
    private Pair<Integer, Recipe>[] recipes;

    /**
     * {@link OnRecipeCardClickListener} implementation, which will determine the code the
     * {@link RecipeViewHolder} will execute when a card is clicked.
     */
    private final OnRecipeCardClickListener onRecipeCardClickListener;

    /**
     * Recipe adapter constructor.
     * @param recipes Array of {@link Pair}s, where the first element of a pair is the integer ID
     * of the recipe in the database, and the second element is an instance of {@link Recipe} where
     * the recipe's data is wrapped.
     * See: {@link BiteNoteSQLiteHelper#getQueriedRecipes(RecipeQuery)},
     * {@link BiteNoteSQLiteHelper#getAllRecipes()}
     * @param onRecipeCardClickListener {@link OnRecipeCardClickListener} implementation, which
     * will determine the code the {@link RecipeViewHolder} will execute when a card is clicked.
     */
    public RecipeAdapter(
            @NonNull Pair<Integer, Recipe>[] recipes,
            @NonNull OnRecipeCardClickListener onRecipeCardClickListener
    ) {
        this.recipes = recipes;
        this.onRecipeCardClickListener = onRecipeCardClickListener;
    }

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

        holder.bind(id, recipe, onRecipeCardClickListener);
    }

    @Override
    public int getItemCount() {
        return recipes.length;
    }

    /**
     * Sets the recipes of the adapter.
     * @param recipes Array of {@link Pair}s, where the first element of a pair is the integer ID
     * of the recipe in the database, and the second element is an instance of {@link Recipe} where
     * the recipe's data is wrapped.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setRecipes(@NonNull Pair<Integer, Recipe>[] recipes) {
        this.recipes = recipes;

        notifyDataSetChanged();
    }

    /**
     * View holder for recipes.
     * @see RecipeAdapter
     * @author Daniel N.
     */
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
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
         * {@link TextView} instance that references the creation date of the recipe in the card.
         * @see Recipe#creationDate
         */
        private final TextView creationDateTextView;

        /**
         * {@link TextView} instance that references the amount of diners of the recipe in the card.
         * @see Recipe#diners
         */
        private final TextView dinersTextView;

        /**
         * {@link TextView} instance that references the budget of the recipe in the card.
         * @see Recipe#budget
         */
        private final TextView budgetTextView;

        /**
         * Recipe view holder constructor.
         * @param itemView {@link View} instance.
         */
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            /// init views
            this.nameTextView = itemView.findViewById(R.id.RecipeCardNameTextView);
            this.bodyTextView = itemView.findViewById(R.id.RecipeCardBodyTextView);
            this.creationDateTextView = itemView.findViewById(R.id.RecipeCardCreationDateTextView);
            this.dinersTextView = itemView.findViewById(R.id.RecipeCardDinersTextView);
            this.budgetTextView = itemView.findViewById(R.id.RecipeCardBudgetTextView);
        }

        /**
         * Binds recipe data to the view.
         * @param recipeId ID of the database recipe.
         * @param recipe {@link Recipe} instance that holds the new data.
         * @param listener {@link OnRecipeCardClickListener} implementation, which will determine
         * the code the {@link RecipeViewHolder} will execute when a card is clicked.
         */
        private void bind(
                int recipeId,
                @NonNull Recipe recipe,
                @NonNull OnRecipeCardClickListener listener
        ) {
            nameTextView.setText(recipe.name);
            bodyTextView.setText(recipe.body);
            creationDateTextView.setText(recipe.creationDate.toString());
            dinersTextView.setText(String.valueOf(recipe.diners));
            budgetTextView.setText(String.format(Locale.getDefault(),"%d$", recipe.budget));

            itemView.setContentDescription(itemView.getContentDescription() + ": " + recipe.name);
            /// :-(

            itemView.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onRecipeCardClick(recipeId, recipe);
            });

            itemView.setOnLongClickListener(view -> {
                /// onLongClickListener implementation expects a boolean
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return false;

                listener.onLongRecipeCardClick(recipeId, recipe);
                return true; // long click handled
            });
        }
    }
}
