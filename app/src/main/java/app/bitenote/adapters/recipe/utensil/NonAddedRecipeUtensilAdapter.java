package app.bitenote.adapters.recipe.utensil;

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
import app.bitenote.instances.Utensil;

/**
 * Adapter for displaying {@link Utensil}s that have not been added to a recipe in a
 * {@link RecyclerView} with cards in
 * {@link app.bitenote.activities.text.editing.EditRecipeUtensilsActivity}.
 * @see ViewHolder
 * @author Daniel N.
 */
public final class NonAddedRecipeUtensilAdapter extends
        RecyclerView.Adapter<NonAddedRecipeUtensilAdapter.ViewHolder>
{
    /**
     * List of utensils in the adapter. The first element of the pair represents the integer ID of
     * the utensil in the database, and the second element represents the data of that utensil,
     * wrapped in an {@link Utensil} instance.
     */
    private List<Pair<Integer, Utensil>> utensils;

    /**
     * {@link OnButtonClickListener} implementation, which will determine the code the
     * {@link ViewHolder} will execute when the buttons are clicked.
     */
    private final OnButtonClickListener listener;

    /**
     * Non-added recipe utensil adapter constructor.
     * @param utensils Array of {@link Pair}s, where the first element of the pair represents the
     * integer ID of the utensil in the database, and the second element represents the data of that
     * utensil, wrapped in an {@link Utensil} instance. See:
     * {@link BiteNoteSQLiteHelper#getAllUtensils()},
     * {@link BiteNoteSQLiteHelper#getAllUtensilsExcept(Set)}.
     * @param listener {@link OnButtonClickListener} implementation, which will determine the code
     * the {@link ViewHolder} will execute when the buttons are clicked.
     */
    public NonAddedRecipeUtensilAdapter(
            @NonNull List<Pair<Integer, Utensil>> utensils,
            @NonNull OnButtonClickListener listener
    ) {
        this.utensils = utensils;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.non_added_utensil_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int id = utensils.get(position).first;
        final Utensil utensil = utensils.get(position).second;

        holder.bind(id, utensil, listener);
    }

    @Override
    public int getItemCount() {
        return utensils.size();
    }

    public List<Pair<Integer, Utensil>> getUtensils() {
        return Collections.unmodifiableList(utensils);
    }

    /**
     * Sets the utensils of the adapter.
     * @param utensils Array of {@link Pair}s, where the first element of the pair represents the
     * integer ID of the utensil in the database, and the second element represents the data of that
     * utensil, wrapped in an {@link Utensil} instance.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setUtensils(@NonNull List<Pair<Integer, Utensil>> utensils) {
        this.utensils = utensils;

        notifyDataSetChanged();
    }

    public void removeUtensil(int utensilId, @NonNull Utensil utensil) {
        final Pair<Integer, Utensil> pairToRemove = Pair.create(utensilId, utensil);
        removeUtensil(pairToRemove);
    }

    public void removeUtensil(@NonNull Pair<Integer, Utensil> pair) {
        if (!utensils.contains(pair)) return;

        final int position = utensils.indexOf(pair);
        utensils.remove(pair);

        notifyItemRemoved(position);
    }

    public void addUtensil(int utensilId, @NonNull Utensil utensil) {
        final Pair<Integer, Utensil> pairToAdd = Pair.create(utensilId, utensil);
        addUtensil(pairToAdd);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addUtensil(@NonNull Pair<Integer, Utensil> pair) {
        utensils.add(pair);

        utensils.sort(Comparator.comparing(pairA -> pairA.first)); // sort elements again

        notifyDataSetChanged();
    }

    /**
     * View holder for a single utensil that hasn't been added yet to the recipe.
     */
    public static final class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * {@link TextView} instance that displays the name of the utensil.
         */
        private final TextView nameTextView;

        /**
         * {@link ImageButton} that is used to add the utensil to the recipe.
         */
        private final ImageButton addButton;

        /**
         * Non-added utensil view holder constructor.
         * @param itemView {@link View} instance.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.NonAddedUtensilCardNameTextView);
            addButton = itemView.findViewById(R.id.NonAddedUtensilCardAddButton);
        }

        /**
         * Binds utensil data to the view.
         * @param utensilId ID of the database ingredient.
         * @param utensil {@link Utensil} instance that holds the data.
         * @param listener {@link OnButtonClickListener} implementation, which will determine the
         * code the {@link ViewHolder} will execute when the buttons are clicked.
         */
        @SuppressLint("DiscouragedApi")
        public void bind(
                int utensilId,
                @NonNull Utensil utensil,
                @NonNull OnButtonClickListener listener
        ) {
            nameTextView.setText(utensil.name); // todo: translate

            addButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onAddButtonClick(utensilId, utensil);
            });
        }
    }

    /**
     * Interface that determines what the buttons of a {@link ViewHolder} do when clicked.
     * @author Daniel N.
     */
    public interface OnButtonClickListener {
        /**
         * Function that will be called when the add button is clicked.
         * @param utensilId ID of the utensil in the database.
         * @param utensil {@link Utensil} instance.
         */
        void onAddButtonClick(int utensilId, @NonNull Utensil utensil);
    }
}
