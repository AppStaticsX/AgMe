package com.appstaticsx.app.agmeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * CustomDropdownAdapter is a custom ArrayAdapter that provides a dropdown list with both text
 * and icons. It supports displaying the selected item in the input field as well as dropdown items.
 */
public class CustomDropdownAdapter extends ArrayAdapter<DropdownItem> {
    // LayoutInflater to inflate the dropdown layout
    private final LayoutInflater inflater;

    /**
     * Constructor to initialize the adapter with a context and list of dropdown items.
     *
     * @param context The context for the adapter
     * @param items   The list of DropdownItem objects
     */
    public CustomDropdownAdapter(Context context, List<DropdownItem> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    /**
     * Returns the view for the selected item displayed in the input field.
     *
     * @param position    The position of the item within the list
     * @param convertView The recycled view to populate (if available)
     * @param parent      The parent ViewGroup that this view will attach to
     * @return A view displaying the selected dropdown item
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Reuse or inflate the view for the selected item
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dropdown_item_with_icon_layout, parent, false);
        }

        // Bind data to the views
        bindView(position, convertView);

        return convertView;
    }

    /**
     * Returns the view for dropdown list items.
     *
     * @param position    The position of the item within the list
     * @param convertView The recycled view to populate (if available)
     * @param parent      The parent ViewGroup that this view will attach to
     * @return A view displaying a dropdown list item
     */
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        // Reuse or inflate the view for dropdown items
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dropdown_item_with_icon_layout, parent, false);
        }

        // Bind data to the views
        bindView(position, convertView);

        return convertView;
    }

    /**
     * Binds data from the DropdownItem to the corresponding views in the layout.
     *
     * @param position    The position of the item within the list
     * @param convertView The view to bind data to
     */
    private void bindView(int position, View convertView) {
        DropdownItem item = getItem(position);
        if (item != null) {
            TextView textView = convertView.findViewById(R.id.dropdown_item);
            ImageView imageView = convertView.findViewById(R.id.icon);

            textView.setText(item.getText());
            imageView.setImageResource(item.getIconResId());
        }
    }
}