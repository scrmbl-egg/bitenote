package app.bitenote.adapters.query.ingredient;

import android.annotation.SuppressLint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import app.bitenote.R;
import app.bitenote.database.BiteNoteSQLiteHelper;
import app.bitenote.instances.Ingredient;

/**
 * Adapter for displaying banned {@link Ingredient} data in a {@link RecyclerView} with cards
 * in {@link app.bitenote.activities.query.IngredientQueryActivity}.
 * @see ViewHolder
 * @author Daniel N.
 */
public final class BannedIngredientAdapter extends
        RecyclerView.Adapter<BannedIngredientAdapter.ViewHolder>
{
    /**
     * List of ingredients in the adapter. The first element of the pair represents the
     * integer ID of the ingredient in the database, and the second element represents the data of
     * that ingredient, wrapped in an {@link Ingredient} instance.
     */
    private List<Pair<Integer, Ingredient>> mIngredients;

    /**
     * {@link OnButtonsClickListener} implementation, which will determine
     * the code the {@link ViewHolder} will execute when the buttons are clicked.
     */
    private final OnButtonsClickListener mListener;

    /**
     * Banned ingredient adapter constructor.
     * @param ingredients List of {@link Pair}s, where the first element of a pair is the integer
     * ID of the ingredient in the database, and the second element is an instance of
     * {@link Ingredient} where the ingredient's data is wrapped.
     * See: {@link BiteNoteSQLiteHelper#getAllIngredients()},
     * {@link BiteNoteSQLiteHelper#getAllIngredientsExcept(Set)}.
     * @param listener {@link OnButtonsClickListener} implementation, which
     * will determine the code the {@link ViewHolder} will execute when a card is clicked.
     */
    public BannedIngredientAdapter(
            @NonNull List<Pair<Integer, Ingredient>> ingredients,
            @NonNull OnButtonsClickListener listener
    ) {
        mIngredients = ingredients;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.banned_ingredient_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int id = mIngredients.get(position).first;
        final Ingredient ingredient = mIngredients.get(position).second;

        holder.bind(id, ingredient, mListener);
    }

    @Override
    public int getItemCount() {
        return mIngredients.size();
    }

    /**
     * Gets all the ingredients in the adapter.
     * @return Unmodifiable list of ingredients in the adapter. The first element of a pair
     * represents the integer ID of the ingredient in the database, and the second element
     * represents the data of that ingredient, wrapped in an {@link Ingredient} instance.
     */
    public List<Pair<Integer, Ingredient>> getIngredients() {
        return Collections.unmodifiableList(mIngredients);
    }

    /**
     * Sets the ingredients of the adapter.
     * @param ingredients List of {@link Pair}s, where the first element of a pair is the integer
     * ID of the ingredient in the database, and the second element is an instance of
     * {@link Ingredient} where the ingredient's data is wrapped.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setIngredients(@NonNull List<Pair<Integer, Ingredient>> ingredients) {
        mIngredients = ingredients;

        notifyDataSetChanged();
    }

    /**
     * Adds an ingredient to the adapter.
     * @param ingredientId ID of the ingredient in the database.
     * @param ingredient {@link Ingredient} instance.
     */
    public void addIngredient(int ingredientId, @NonNull Ingredient ingredient) {
        final Pair<Integer, Ingredient> pairToAdd = Pair.create(ingredientId, ingredient);
        addIngredient(pairToAdd);
    }

    /**
     * Adds an ingredient to the adapter.
     * @param pair {@link Pair} instance, where the first element of the pair represents the
     * integer ID of the ingredient in the database, and the second element represents the data of
     * that ingredient, wrapped in an {@link Ingredient} instance.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void addIngredient(@NonNull Pair<Integer, Ingredient> pair) {
        mIngredients.add(pair);
        mIngredients.sort(Comparator.comparing(pairA -> pairA.first)); // sort elements again

        notifyDataSetChanged();
    }

    /**
     * Removes an ingredient from the adapter.
     * @param ingredientId ID of the ingredient in the database.
     * @param ingredient {@link Ingredient} instance.
     */
    public void removeIngredient(int ingredientId, @NonNull Ingredient ingredient) {
        final Pair<Integer, Ingredient> pairToRemove = Pair.create(ingredientId, ingredient);
        removeIngredient(pairToRemove);
    }

    /**
     * Removes an ingredient from the adapter.
     * @param pair {@link Pair} instance, where the first element of the pair represents the
     * integer ID of the ingredient in the database, and the second element represents the data of
     * that ingredient, wrapped in an {@link Ingredient} instance.
     */
    public void removeIngredient(@NonNull Pair<Integer, Ingredient> pair) {
        if (!mIngredients.contains(pair)) return;

        final int position = mIngredients.indexOf(pair);
        mIngredients.remove(pair);

        notifyItemRemoved(position);
    }

    /**
     * View holder for a single banned ingredient.
     * @author Daniel N.
     */
    public static final class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * {@link TextView} instance that displays the translated name of the ingredient in the
         * card.
         * @see Ingredient#fullName
         */
        private final TextView mNameTextView;

        /**
         * {@link ImageButton} instance that is used for including an ingredient in the query.
         */
        private final ImageButton mIncludeButton;

        /**
         * {@link ImageButton} instance that is used for removing an ingredient from the query.
         */
        private final ImageButton mRemoveButton;

        /**
         * Queried ingredient view holder constructor.
         * @param itemView {@link View} instance.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /// init views
            mNameTextView = itemView.findViewById(R.id.BannedIngredientCardNameTextView);
            mIncludeButton = itemView.findViewById(R.id.BannedIngredientCardIncludeButton);
            mRemoveButton = itemView.findViewById(R.id.BannedIngredientCardRemoveButton);
        }

        /**
         * Binds ingredient data to the view.
         * @param ingredientId ID of the database ingredient.
         * @param ingredient {@link Ingredient} instance that holds the data.
         * @param listener {@link OnButtonsClickListener} implementation, which will determine the
         * code the {@link ViewHolder} will execute when the buttons are clicked.
         */
        @SuppressLint("DiscouragedApi")
        public void bind(
                int ingredientId,
                @NonNull Ingredient ingredient,
                @NonNull OnButtonsClickListener listener
        ) {
            mNameTextView.setText(itemView.getResources().getIdentifier(
                    "ingredient_" + ingredient.fullName,
                    "string",
                    itemView.getContext().getPackageName()
            ));

            mIncludeButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onIncludeButtonClick(ingredientId, ingredient);
            });

            mRemoveButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onRemoveButtonClick(ingredientId, ingredient);
            });
        }
    }

    /**
     * Interface that determines what the buttons of a {@link ViewHolder} do when clicked.
     * @author Daniel N.
     */
    public interface OnButtonsClickListener {
        /**
         * Function that will be called when the include button is clicked.
         * @param ingredientId ID of the ingredient in the database.
         * @param ingredient {@link Ingredient} instance.
         */
        void onIncludeButtonClick(int ingredientId, @NonNull Ingredient ingredient);

        /**
         * Function that will be called when the remove button is clicked.
         * @param ingredientId ID of the ingredient in the database.
         * @param ingredient {@link Ingredient} instance.
         */
        void onRemoveButtonClick(int ingredientId, @NonNull Ingredient ingredient);
    }
}
