package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * CardAdapterHomeFragment is an adapter for a RecyclerView that binds a list of CardItem objects
 * to the views in the card_item_home_fragment layout.
 */
public class CardAdapterHomeFragment extends RecyclerView.Adapter<CardAdapterHomeFragment.ViewHolder> {

    // List to hold card data items
    private final List<CardItem> cardItemList;

    /**
     * Constructor for initializing the adapter with a list of card items.
     *
     * @param cardItemList List of CardItem objects to be displayed
     */
    public CardAdapterHomeFragment(List<CardItem> cardItemList) {
        this.cardItemList = cardItemList;
    }

    /**
     * Inflates the item layout and creates a ViewHolder instance.
     *
     * @param parent   The ViewGroup into which the new view will be added
     * @param viewType The view type of the new view
     * @return A new ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each card item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item_home_fragment, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data from the card item to the views in the ViewHolder.
     *
     * @param holder   The ViewHolder which holds the views
     * @param position The position of the data item in the list
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the card item at the current position
        CardItem cardItem = cardItemList.get(position);

        // Set data to the corresponding views
        holder.cropName.setText(cardItem.getName());
        holder.fieldSize.setText("Field Size: " + cardItem.getFieldSize());
        holder.plantationDate.setText("Plantation Date: " + cardItem.getPlantationDate());
        holder.harvestedDate.setText("Harvested Date: " + cardItem.getHarvestedDate());
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return Size of the cardItemList
     */
    @Override
    public int getItemCount() {
        return cardItemList.size();
    }

    /**
     * ViewHolder class that holds references to the views for each card item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // TextViews to display card details
        private final TextView cropName, fieldSize, plantationDate, harvestedDate;

        /**
         * Constructor that initializes the views in the card layout.
         *
         * @param view The inflated view for the card item
         */
        public ViewHolder(@NonNull View view) {
            super(view);
            cropName = view.findViewById(R.id.cropNameTV);
            fieldSize = view.findViewById(R.id.cropFieldSizeTV);
            plantationDate = view.findViewById(R.id.cropPlantDateTV);
            harvestedDate = view.findViewById(R.id.cropHarvestDateCountTV);
        }
    }
}
