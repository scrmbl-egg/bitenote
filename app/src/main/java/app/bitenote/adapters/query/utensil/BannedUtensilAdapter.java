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
 * Adapter for displaying queried {@link Utensil} data in a {@link RecyclerView} with cards
 * in {@link app.bitenote.activities.query.UtensilQueryActivity}.
 * @see ViewHolder
 * @author Daniel N.
 */
public final class BannedUtensilAdapter extends
        RecyclerView.Adapter<BannedUtensilAdapter.ViewHolder>
{
    /**
     * List of utensils in the adapter. The first element of the pair represents the
     * integer ID of the utensil in the database, and the second element represents the data of
     * that utensil, wrapped in an {@link Utensil} instance.
     */
    private List<Pair<Integer, Utensil>> mUtensils;

    /**
     * {@link OnButtonsClickListener} implementation, which will determine
     * the code the {@link ViewHolder} will execute when the buttons are clicked.
     */
    private final OnButtonsClickListener mListener;

    /**
     * Banned utensils adapter constructor.
     * @param utensils List of {@link Pair}s, where the first element of a pair is the integer
     * ID of the utensil in the database, and the second element is an instance of {@link Utensil}
     * where the utensil's data is wrapped.
     * See: {@link BiteNoteSQLiteHelper#getAllUtensils()},
     * {@link BiteNoteSQLiteHelper#getAllUtensilsExcept(Set)}
     * @param listener {@link OnButtonsClickListener} implementation, which
     * will determine the code the {@link ViewHolder} will execute when a card is clicked.
     */
    public BannedUtensilAdapter(
            @NonNull List<Pair<Integer, Utensil>> utensils,
            @NonNull OnButtonsClickListener listener
    ) {
        mUtensils = utensils;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.banned_utensil_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int id = mUtensils.get(position).first;
        final Utensil utensil = mUtensils.get(position).second;

        holder.bind(id, utensil, mListener);
    }

    @Override
    public int getItemCount() {
        return mUtensils.size();
    }

    /**
     * Gets all the utensils in the adapter.
     * @return Unmodifiable list of utensils in the adapter. The first element of a pair
     * represents the integer ID of the utensil in the database, and the second element
     * represents the data of that utensil, wrapped in an {@link Utensil} instance.
     */
    public List<Pair<Integer, Utensil>> getUtensils() {
        return Collections.unmodifiableList(mUtensils);
    }

    /**
     * Sets the utensils of the adapter.
     * @param utensils List of {@link Pair}s, where the first element of a pair is the integer
     * ID of the utensil in the database, and the second element is an instance of
     * {@link Utensil} where the utensil's data is wrapped.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setUtensils(@NonNull List<Pair<Integer, Utensil>> utensils) {
        mUtensils = utensils;

        notifyDataSetChanged();
    }

    /**
     * Adds an utensil to the adapter.
     * @param utensilId ID of the utensil in the database.
     * @param utensil {@link Utensil} instance.
     */
    public void addUtensil(int utensilId, @NonNull Utensil utensil) {
        final Pair<Integer, Utensil> pairToAdd = Pair.create(utensilId, utensil);
        addUtensil(pairToAdd);
    }

    /**
     * Adds an utensil to the adapter.
     * @param pair {@link Pair} instance, where the first element of the pair represents the
     * integer ID of the utensil in the database, and the second element represents the data of that
     * utensil, wrapped in an {@link Utensil} instance.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void addUtensil(@NonNull Pair<Integer, Utensil> pair) {
        mUtensils.add(pair);
        mUtensils.sort(Comparator.comparing(pairA -> pairA.first)); // sort elements again

        notifyDataSetChanged();
    }

    /**
     * Removes an utensil from the adapter.
     * @param utensilId ID of the utensil in the database.
     * @param utensil {@link Utensil} instance.
     */
    public void removeUtensil(int utensilId, @NonNull Utensil utensil) {
        final Pair<Integer, Utensil> pairToRemove = Pair.create(utensilId, utensil);
        removeUtensil(pairToRemove);
    }

    /**
     * Removes an utensil from the adapter.
     * @param pair {@link Pair} instance, where the first element of the pair represents the
     * integer ID of the utensil in the database, and the second element represents the data of that
     * utensil, wrapped in an {@link Utensil} instance.
     */
    public void removeUtensil(@NonNull Pair<Integer, Utensil> pair) {
        if (!mUtensils.contains(pair)) return;

        final int position = mUtensils.indexOf(pair);
        mUtensils.remove(pair);

        notifyItemRemoved(position);
    }

    /**
     * View holder for banned utensils.
     * @author Daniel N.
     */
    public static final class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * {@link TextView} instance that displays the translated name of the utensil in the card.
         * @see Utensil#name
         */
        private final TextView mNameTextView;

        /**
         * {@link ImageButton} instance that is used for including an utensil in the query.
         */
        private final ImageButton mIncludeButton;

        /**
         * {@link ImageButton} instance that is used for removing an utensil from the query.
         */
        private final ImageButton mRemoveButton;

        /**
         * Banned utensil view holder constructor.
         * @param itemView {@link View} instance.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mNameTextView = itemView.findViewById(R.id.BannedUtensilCardNameTextView);
            mIncludeButton = itemView.findViewById(R.id.BannedUtensilCardIncludeButton);
            mRemoveButton = itemView.findViewById(R.id.BannedUtensilCardRemoveButton);
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
            mNameTextView.setText(itemView.getResources().getIdentifier(
                    "utensil_" + utensil.name,
                    "string",
                    itemView.getContext().getPackageName()
            ));

            mIncludeButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onIncludeButtonClick(utensilId, utensil);
            });

            mRemoveButton.setOnClickListener(view -> {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                listener.onRemoveButtonClick(utensilId, utensil);
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
         * Function called when the remove button is clicked.
         * @param utensilId ID of the utensil in the database.
         * @param utensil {@link Utensil} instance.
         */
        void onRemoveButtonClick(int utensilId, @NonNull Utensil utensil);
    }
}
