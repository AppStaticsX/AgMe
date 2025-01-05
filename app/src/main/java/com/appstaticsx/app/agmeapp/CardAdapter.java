package com.appstaticsx.app.agmeapp;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * CardAdapter is a RecyclerView.Adapter responsible for displaying a list of crop data.
 * It binds data from CardItem objects to the views defined in the card_item layout.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private static final String PREFS_NAME = "UserPreferences"; // Shared preferences name
    private static final String KEY_USER_EMAIL = "userEmail"; // Key for storing user email

    // List to hold CardItem objects
    private final List<CardItem> cardItemList;

    /**
     * Constructor to initialize the adapter with a list of CardItem objects.
     *
     * @param cardItemList List of CardItem objects to display
     */
    public CardAdapter(List<CardItem> cardItemList) {
        this.cardItemList = cardItemList;
    }

    /**
     * Creates and returns a ViewHolder object for RecyclerView items.
     *
     * @param parent   The parent ViewGroup into which the new view will be added
     * @param viewType The view type of the new view (not used here)
     * @return A new ViewHolder instance containing the inflated view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the card_item layout to use for each RecyclerView item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data from a CardItem object to the views in the ViewHolder.
     *
     * @param holder   The ViewHolder containing the views
     * @param position The position of the item in the list
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current CardItem object based on position
        CardItem cardItem = cardItemList.get(position);

        // Bind data to the views
        holder.cropName.setText(cardItem.getName());
        holder.fieldSize.setText("Field Size: " + cardItem.getFieldSize() + " Perch");
        holder.plantationDate.setText("Plantation Date: " + cardItem.getPlantationDate());

        // Set click listener for the edit button
        holder.editButton.setOnClickListener(view -> toggleExpandableSection(holder.expandableLayout));

        // Set click listener for the date picker button
        holder.datePickerBtn.setOnClickListener(v -> showDatePicker(holder));

        // Set click listener for the update button
        holder.updateDetails.setOnClickListener(v -> saveCropDetails(holder)); // Pass ViewHolder as an argument
    }

    /**
     * Toggles the visibility of the expandable section (details area).
     *
     * @param section The expandable section (linear layout)
     */
    @SuppressLint("SetTextI18n")
    private void toggleExpandableSection(View section) {
        if (section.getVisibility() == View.VISIBLE) {
            collapseExpandableSection(section);
        } else {
            expandExpandableSection(section);
        }
    }

    /**
     * Expands the given section with an animation.
     *
     * @param section The section to expand
     */
    private void expandExpandableSection(View section) {
        section.setVisibility(View.VISIBLE);

        // Measure height to expand to
        section.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int targetHeight = section.getMeasuredHeight();

        // Set initial height to 0 and animate to target height
        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            section.getLayoutParams().height = (int) animation.getAnimatedValue();
            section.requestLayout();
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300); // Default animation duration
        animator.start();
    }

    /**
     * Collapses the given section with an animation.
     *
     * @param section The section to collapse
     */
    private void collapseExpandableSection(View section) {
        // Get current height
        final int initialHeight = section.getMeasuredHeight();

        // Animate from current height to 0
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            int height = (int) animation.getAnimatedValue();
            section.getLayoutParams().height = height;
            section.requestLayout();

            if (height == 0) {
                section.setVisibility(View.GONE);
            }
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300); // Default animation duration
        animator.start();
    }

    /**
     * Shows a date picker dialog for the user to select a harvest date.
     *
     * @param holder The ViewHolder containing the views
     */
    private void showDatePicker(ViewHolder holder) {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show a DatePickerDialog
        Context context = holder.itemView.getContext();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date and set it to the cropHarvestDate field
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    holder.cropHarvestDate.setText(dateFormat.format(calendar.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    /**
     * Saves the crop details to Firebase Database.
     *
     * @param holder The ViewHolder containing the updated crop details
     */
    private void saveCropDetails(ViewHolder holder) {
        // Get user email from SharedPreferences
        Context context = holder.itemView.getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        // Check if the user is logged in
        if (userEmail == null) {
            CustomToast customToast = new CustomToast(context);
            customToast.show("User not logged in. Please log in to save crop details.", R.drawable.user_cross_svgrepo_com);
            return;
        }

        // Validate and get the harvest date
        String harvestedDateValue = Objects.requireNonNull(holder.cropHarvestDate.getText()).toString().trim();
        if (harvestedDateValue.isEmpty()) {
            CustomToast customToast = new CustomToast(context);
            customToast.show("Please fill in the harvest date.", R.drawable.warning_svgrepo_com);
            return;
        }

        // Sanitize email for Firebase keys
        String sanitizedEmail = userEmail.replace(".", ",");

        // Validate and get the crop name
        String cropName = holder.cropName.getText().toString().trim();
        if (cropName.isEmpty()) {
            CustomToast customToast = new CustomToast(context);
            customToast.show("Crop name is missing or invalid.", R.drawable.warning_svgrepo_com);
            return;
        }

        // Create a reference for the crop under the user's "crops" node within "users"
        DatabaseReference userCropsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail)
                .child("crops")
                .child(cropName);

        // Set the crop details
        userCropsRef.child("harvestedDate").setValue(harvestedDateValue)
                .addOnSuccessListener(aVoid -> {
                    CustomToast customToast = new CustomToast(context);
                    customToast.show("Crop details saved successfully!", R.drawable.icons8_correct);
                })
                .addOnFailureListener(e -> {
                    CustomToast customToast = new CustomToast(context);
                    customToast.show("Failed to save crop details. Please try again.", R.drawable.icons8_wrong);
                });
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The size of cardItemList
     */
    @Override
    public int getItemCount() {
        return cardItemList.size();
    }

    /**
     * ViewHolder class that holds references to the views for each RecyclerView item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cropName, fieldSize, plantationDate;
        ImageButton editButton;
        LinearLayout expandableLayout;
        AppCompatImageButton datePickerBtn;
        TextInputEditText cropHarvestDate;
        MaterialButton updateDetails;

        /**
         * Constructor to initialize the views in the ViewHolder.
         *
         * @param view The root view of the card_item layout
         */
        public ViewHolder(View view) {
            super(view);
            // Initialize views using their IDs
            cropName = view.findViewById(R.id.cropNameTV);
            fieldSize = view.findViewById(R.id.cropFieldSizeTV);
            plantationDate = view.findViewById(R.id.cropPlantDateTV);
            editButton = view.findViewById(R.id.cropEditIB);
            expandableLayout = view.findViewById(R.id.cropEditExpandablell);
            datePickerBtn = view.findViewById(R.id.datePickerBtn);
            cropHarvestDate = view.findViewById(R.id.dateInputHarvest);
            updateDetails = view.findViewById(R.id.update_crop_details_btn);
        }
    }
}
