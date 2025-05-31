package app.bitenote.adapters.recipe.ingredient;

import android.annotation.SuppressLint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import app.bitenote.R;
import app.bitenote.database.BiteNoteSQLiteHelper;
import app.bitenote.instances.Ingredient;
import app.bitenote.instances.Ingredient.InRecipeProperties;
import app.bitenote.instances.MeasurementType;
import app.bitenote.instances.Recipe;

/**
 * Adapter for displaying {@link Ingredient}s that have been added to a recipe in a
 * {@link RecyclerView} with cards in
 * {@link app.bitenote.activities.text.editing.EditRecipeIngredientsActivity}.
 * @see ViewHolder
 * @author Daniel N.
 */
public final class AddedRecipeIngredientAdapter
        extends RecyclerView.Adapter<AddedRecipeIngredientAdapter.ViewHolder>
{
    /**
     * List of ingredients in the adapter. The first element of the pair is an inner pair in which
     * the first element represents integer ID of the ingredient in the database, and the second
     * element is an instance of {@link Ingredient} that contains the data of the ingredient. The
     * second element of the pair is an {@link InRecipeProperties} instance that contains the data
     * of the recipe ingredient.
     */
    private List<Pair<Pair<Integer, Ingredient>, InRecipeProperties>> ingredients;

    /**
     * {@link OnButtonClickListener} implementation, which will determine
     * the code the {@link ViewHolder} will execute when the buttons are clicked.
     */
    private final OnButtonClickListener listener;

    /**
     * Added recipe ingredient adapter constructor.
     * @param ingredients List of ingredients in the adapter. The first element of the pair is an
     * inner pair in which the first element represents integer ID of the ingredient in the
     * database, and the second element is an instance of {@link Ingredient} that contains the
     * data of the ingredient. The second element of the pair is an {@link InRecipeProperties}
     * instance that contains the data of the recipe ingredient.
     * See: {@link BiteNoteSQLiteHelper#getAllIngredients()},
     * {@link BiteNoteSQLiteHelper#getAllIngredientsExcept(Set)}
     * @param listener {@link OnButtonClickListener} implementation, which will determine the code
     * the {@link ViewHolder} will execute when a card is clicked.
     */
    public AddedRecipeIngredientAdapter(
            @NonNull List<Pair<Pair<Integer, Ingredient>, InRecipeProperties>> ingredients,
            @NonNull OnButtonClickListener listener
    ) {
        this.ingredients = ingredients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.added_ingredient_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int id = ingredients.get(position).first.first;
        final Ingredient ingredient = ingredients.get(position).first.second;
        final InRecipeProperties properties = ingredients.get(position).second;

        /// the ingredient list is bound because buttons in the view holders mutate it
        holder.bind(this, id, ingredient, properties, listener);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Gets all the ingredients in the adapter.
     * @return Unmodifiable list of ingredients in the adapter. The first element of the pair is
     * an inner pair in which the first element represents integer ID of the ingredient in the
     * database, and the second element is an instance of {@link Ingredient} that contains the data
     * of the ingredient. The second element of the pair is an {@link InRecipeProperties} instance
     * that contains the data of the recipe ingredient.
     */
    public List<Pair<Pair<Integer, Ingredient>, InRecipeProperties>> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /**
     * Sets the ingredients of the adapter.
     * @param ingredients List of {@link Pair}s, where the first element of the pair represents the
     * integer ID of the ingredient in the database, and the second element is another pair whose
     * first element is an instance of {@link Ingredient} that contains the data of the ingredient,
     * and the second element is an {@link InRecipeProperties} instance that contains the data
     * of the recipe ingredient.
     * @see BiteNoteSQLiteHelper#getRecipeIngredientsWithProperties(Recipe)
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setIngredients(
            @NonNull List<Pair<Pair<Integer, Ingredient>,  InRecipeProperties>> ingredients
    ) {
        this.ingredients = ingredients;

        notifyDataSetChanged();
    }

    public void removeIngredient(
            int ingredientId,
            @NonNull Ingredient ingredient,
            @NonNull InRecipeProperties properties
    ) {
        final Pair<Pair<Integer, Ingredient>, InRecipeProperties> pairToRemove =
                Pair.create(Pair.create(ingredientId, ingredient), properties);
        removeIngredient(pairToRemove);
    }

    public void removeIngredient(
            @NonNull Pair<Pair<Integer, Ingredient>, InRecipeProperties> pair
    ) {
        if (!ingredients.contains(pair)) return;

        final int position = ingredients.indexOf(pair);
        ingredients.remove(pair);

        notifyItemRemoved(position);
    }

    public void addIngredient(int ingredientId, @NonNull Ingredient ingredient) {
        /// when adding an ingredient, recipe properties start with default values

        final InRecipeProperties properties = new InRecipeProperties();
        addIngredient(Pair.create(Pair.create(ingredientId, ingredient), properties));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addIngredient(@NonNull Pair<Pair<Integer, Ingredient>, InRecipeProperties> pair) {
        ingredients.add(pair);

        /// sort elements again
        ingredients.sort(Comparator.comparing(p -> p.first.first));

        notifyDataSetChanged();
    }

    private void setAmountAtIndex(int i, int amount) {
        ingredients.set(
                i,
                Pair.create(
                        ingredients.get(i).first,
                        new InRecipeProperties(
                                ingredients.get(i).first.second,
                                amount,
                                ingredients.get(i).second.isMeasuredInUnits
                        )
                )
        );
    }

    private void setIsMeasuredInUnitsAtIndex(int i, boolean isMeasuredInUnits) {
        ingredients.set(
                i,
                Pair.create(
                        ingredients.get(i).first,
                        new InRecipeProperties(
                                ingredients.get(i).first.second,
                                ingredients.get(i).second.amount,
                                isMeasuredInUnits
                        )
                )
        );
    }

    /**
     * View holder for a single added ingredient.
     * @author Daniel N.
     */
    public static final class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * {@link TextView} instance that displays the translated name of the ingredient in the
         * card.
         * @see Ingredient#fullName
         */
        private final TextView nameTextView;

        /**
         * {@link EditText} instance that displays and allows the user to edit the amount
         */
        private final EditText amountEditText;

        /**
         * {@link ToggleButton} instance that is used for toggling between measurements if the
         * ingredient can be measured in units.
         * @see Ingredient#canBeMeasuredInUnits
         */
        private final ToggleButton measurementToggleButton;

        /**
         * {@link ImageButton} instance that is used for removing the ingredient from the recipe.
         */
        private final ImageButton removeButton;

        /**
         * Added ingredient view holder constructor.
         * @param itemView {@link View} instance.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.AddedIngredientCardNameTextView);
            amountEditText = itemView.findViewById(R.id.AddedIngredientAmountEditText);
            measurementToggleButton =
                    itemView.findViewById(R.id.AddedIngredientMeasurementToggleButton);
            removeButton = itemView.findViewById(R.id.AddedIngredientCardRemoveButton);
        }

        /**
         * Binds ingredient data to the view.
         * @param adapter Reference to the adapter, so that the view holder can mutate it.
         * @param ingredientId ID of the database ingredient.
         * @param ingredient {@link Ingredient} instance that contains the ingredient data.
         * @param properties {@link InRecipeProperties} instance that contains the properties
         * of the ingredient in the recipe.
         * @param listener {@link OnButtonClickListener} implementation, which will determine the
         * code the {@link ViewHolder} will execute when the buttons are clicked.
         */
        @SuppressLint("DiscouragedApi")
        public void bind(
                @NonNull AddedRecipeIngredientAdapter adapter,
                int ingredientId,
                @NonNull Ingredient ingredient,
                @NonNull InRecipeProperties properties,
                @NonNull OnButtonClickListener listener
        ) {
            nameTextView.setText(itemView.getResources().getIdentifier(
                    "ingredient_" + ingredient.fullName,
                    "string",
                    itemView.getContext().getPackageName()
            ));

            amountEditText.setText(String.valueOf(properties.amount));
            amountEditText.setOnKeyListener((view, i, keyEvent) -> {
                adapter.setAmountAtIndex(
                        getAdapterPosition(),
                        Integer.parseInt(amountEditText.getText().toString())
                );
                return false;
            });

            final String measurementText;
            switch (ingredient.measurementTypeId) {
                case MeasurementType.WEIGHT_ID:
                    measurementText = itemView.getContext().getString(R.string.weight_measurement);
                    break;
                case MeasurementType.VOLUME_ID:
                    measurementText = itemView.getContext().getString(R.string.volume_measurement);
                    break;
                default: // should be unreachable
                    measurementText = "";
                    break;
            }

            measurementToggleButton.setTextOff(measurementText);

            measurementToggleButton.setChecked(
                    ingredient.canBeMeasuredInUnits && properties.isMeasuredInUnits
            );
            measurementToggleButton.setEnabled(ingredient.canBeMeasuredInUnits);
            measurementToggleButton.setOnClickListener(view ->
                    adapter.setIsMeasuredInUnitsAtIndex(
                            getAdapterPosition(),
                            measurementToggleButton.isChecked()
                    )
            );

            removeButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onRemoveButtonClick(ingredientId, ingredient, properties);
            });
        }
    }

    /**
     * Interface that determines what the buttons of a {@link ViewHolder} do when clicked.
     * @author Daniel N.
     */
    public interface OnButtonClickListener {
        /**
         * Function that will be called when the remove button is clicked.
         * @param ingredientId ID of the ingredient in the database.
         * @param ingredient {@link Ingredient} instance.
         * @param properties Properties of the ingredient while being on the recipe.
         */
        void onRemoveButtonClick(
                int ingredientId,
                @NonNull Ingredient ingredient,
                @NonNull InRecipeProperties properties
        );
    }
}
