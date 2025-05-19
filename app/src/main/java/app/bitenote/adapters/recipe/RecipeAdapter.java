package app.bitenote.adapters.recipe;

import android.annotation.SuppressLint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

import app.bitenote.R;
import app.bitenote.database.BiteNoteSQLiteHelper;
import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Recipe;

/**
 * Adapter for displaying {@link Recipe} data in a {@link RecyclerView} with cards.
 * @see ViewHolder
 * @author Daniel N.
 */
public final class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    /**
     * Array of recipes in the adapter. The first element of the pair represents the
     * integer ID of the recipe in the database, and the second element represents the data of that
     * recipe, wrapped in a {@link Recipe} instance.
     */
    private List<Pair<Integer, Recipe>> recipes;

    /**
     * {@link OnClickListener} implementation, which will determine the code the
     * {@link ViewHolder} will execute when a card is clicked.
     */
    private final OnClickListener listener;

    /**
     * Recipe adapter constructor.
     * @param recipes Array of {@link Pair}s, where the first element of a pair is the integer ID
     * of the recipe in the database, and the second element is an instance of {@link Recipe} where
     * the recipe's data is wrapped.
     * See: {@link BiteNoteSQLiteHelper#getQueriedRecipes(RecipeQuery)},
     * {@link BiteNoteSQLiteHelper#getAllRecipes()}
     * @param listener {@link OnClickListener} implementation, which
     * will determine the code the {@link ViewHolder} will execute when a card is clicked.
     */
    public RecipeAdapter(
            @NonNull List<Pair<Integer, Recipe>> recipes,
            @NonNull OnClickListener listener
    ) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int id = recipes.get(position).first;
        final Recipe recipe = recipes.get(position).second;

        holder.bind(id, recipe, listener);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Sets the recipes of the adapter.
     * @param recipes Array of {@link Pair}s, where the first element of a pair is the integer ID
     * of the recipe in the database, and the second element is an instance of {@link Recipe} where
     * the recipe's data is wrapped.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setRecipes(@NonNull List<Pair<Integer, Recipe>> recipes) {
        this.recipes = recipes;

        notifyDataSetChanged();
    }

    /**
     * View holder for a single recipe.
     * @see RecipeAdapter
     * @author Daniel N.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * {@link TextView} instance that displays the name of the recipe in the card.
         * @see Recipe#name
         */
        private final TextView nameTextView;

        /**
         * {@link TextView} instance that displays the body of the recipe in the card.
         * @see Recipe#body
         */
        private final TextView bodyTextView;

        /**
         * {@link TextView} instance that displays the creation date of the recipe in the card.
         * @see Recipe#creationDate
         */
        private final TextView creationDateTextView;

        /**
         * {@link TextView} instance that displays the amount of diners of the recipe in the card.
         * @see Recipe#diners
         */
        private final TextView dinersTextView;

        /**
         * {@link TextView} instance that displays the budget of the recipe in the card.
         * @see Recipe#budget
         */
        private final TextView budgetTextView;

        /**
         * Recipe view holder constructor.
         * @param itemView {@link View} instance.
         */
        public ViewHolder(@NonNull View itemView) {
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
         * @param listener {@link OnClickListener} implementation, which will determine
         * the code the {@link ViewHolder} will execute when a card is clicked.
         */
        private void bind(
                int recipeId,
                @NonNull Recipe recipe,
                @NonNull OnClickListener listener
        ) {
            nameTextView.setText(recipe.name);
            bodyTextView.setText(recipe.body);
            creationDateTextView.setText(recipe.creationDate.toString());
            dinersTextView.setText(String.valueOf(recipe.diners));
            budgetTextView.setText(String.format(Locale.getDefault(),"%d$", recipe.budget));
            // todo: change dollar for language currency

            itemView.setContentDescription(itemView.getContentDescription() + ": " + recipe.name);
            /// :-(

            itemView.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onClick(recipeId, recipe);
            });

            itemView.setOnLongClickListener(view -> {
                /// onLongClickListener implementation expects a boolean
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return false;

                listener.onLongClick(recipeId, recipe);
                return true; // long click handled
            });
        }
    }

    /**
     * Interface that determines what a recipe card will do when clicked.
     * @author Daniel N.
     */
    public interface OnClickListener {
        /**
         * Function that will be called when a recipe card is clicked.
         * @param recipeId ID of the recipe in the database.
         * @param recipe Instance of {@link Recipe} that wraps the data.
         */
        void onClick(int recipeId, @NonNull Recipe recipe);

        /**
         * Function that will be called when a recipe card is clicked for a long period.
         * @param recipeId ID of the recipe in the database.
         * @param recipe Instance of {@link Recipe} that wraps the data.
         */
        void onLongClick(int recipeId, @NonNull Recipe recipe);
    }
}
