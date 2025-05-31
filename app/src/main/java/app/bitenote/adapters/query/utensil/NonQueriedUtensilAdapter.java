package app.bitenote.adapters.query.utensil;

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
 * Adapter for displaying non-queried {@link Utensil} data in a {@link RecyclerView} with cards
 * in {@link app.bitenote.activities.query.UtensilQueryActivity}.
 * @see ViewHolder
 * @author Daniel N.
 */
public final class NonQueriedUtensilAdapter extends
        RecyclerView.Adapter<NonQueriedUtensilAdapter.ViewHolder>
{
    /**
     * Array of utensils in the adapter. The first element of the pair represents the
     * integer ID of the utensil in the database, and the second element represents the data of
     * that utensil, wrapped in an {@link Utensil} instance.
     */
    private List<Pair<Integer, Utensil>> utensils;

    /**
     * {@link OnButtonsClickListener} implementation, which will determine
     * the code the {@link ViewHolder} will execute when the buttons are clicked.
     */
    private final OnButtonsClickListener listener;

    /**
     * Non-queried utensils adapter constructor.
     * @param utensils Array of {@link Pair}s, where the first element of a pair is the integer
     * ID of the utensil in the database, and the second element is an instance of {@link Utensil}
     * where the utensil's data is wrapped.
     * See: {@link BiteNoteSQLiteHelper#getAllUtensils()},
     * {@link BiteNoteSQLiteHelper#getAllUtensilsExcept(Set)}
     * @param listener {@link OnButtonsClickListener} implementation, which
     * will determine the code the {@link ViewHolder} will execute when a card is clicked.
     */
    public NonQueriedUtensilAdapter(
            @NonNull List<Pair<Integer, Utensil>> utensils,
            @NonNull OnButtonsClickListener listener
    ) {
        this.utensils = utensils;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.non_queried_utensil_card, parent, false);

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

    @SuppressLint("NotifyDataSetChanged")
    public void setUtensils(@NonNull List<Pair<Integer, Utensil>> utensils) {
        this.utensils = utensils;

        notifyDataSetChanged();
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

    /**
     * View holder for non-queried utensils.
     * @author Daniel N.
     */
    public static final class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * {@link TextView} instance that displays the translated name of the utensil in the card.
         * @see Utensil#name
         */
        private final TextView nameTextView;

        /**
         * {@link ImageButton} instance that is used for including an utensil in the query.
         */
        private final ImageButton includeButton;

        /**
         * {@link ImageButton} instance that is used for banning an utensil in the query.
         */
        private final ImageButton banButton;

        /**
         * Non-queried utensil view holder constructor.
         * @param itemView {@link View} instance.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.NonQueriedUtensilCardNameTextView);
            includeButton = itemView.findViewById(R.id.NonQueriedUtensilCardIncludeButton);
            banButton = itemView.findViewById(R.id.NonQueriedUtensilCardBanButton);
        }

        /**
         * Binds utensil data to the view.
         * @param utensilId ID of the database utensil.
         * @param utensil {@link Utensil} instance that holds the new data.
         * @param listener {@link OnButtonsClickListener} implementation, which
         * will determine the code the {@link ViewHolder} will execute when the buttons are clicked.
         */
        @SuppressLint("DiscouragedApi")
        public void bind(
                int utensilId,
                @NonNull Utensil utensil,
                @NonNull OnButtonsClickListener listener
        ) {
            nameTextView.setText(itemView.getResources().getIdentifier(
                    "utensil_" + utensil.name,
                    "string",
                    itemView.getContext().getPackageName()
            ));

            includeButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onIncludeButtonClick(utensilId, utensil);
            });

            banButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onBanButtonClick(utensilId, utensil);
            });
        }
    }

    /**
     * Interface that determines what the buttons of a {@link ViewHolder} do when clicked.
     * @author Daniel N.
     */
    public interface OnButtonsClickListener {
        /**
         * Function called when the include button is clicked.
         * @param utensilId ID of the utensil in the database.
         * @param utensil {@link Utensil} instance.
         */
        void onIncludeButtonClick(int utensilId, @NonNull Utensil utensil);

        /**
         * Function called when the ban button is clicked.
         * @param utensilId ID of the utensil in the database.
         * @param utensil {@link Utensil} instance.
         */
        void onBanButtonClick(int utensilId, @NonNull Utensil utensil);
    }
}
