package com.appstaticsx.app.agmeapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AddCropFragment extends Fragment {

    // Constants for SharedPreferences keys
    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_USER_EMAIL = "userEmail";

    // UI Elements
    private TextInputEditText cropFieldSize, cropPlantationDate;
    private MaterialAutoCompleteTextView cropName, district;
    private CardAdapter cardAdapter;
    private final List<CardItem> cardItemList = new ArrayList<>();
    private TextView addCropSubTitle;
    private MaterialCardView viewCropCard;

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_crop, container, false);

        // Initialize UI elements
        MaterialCardView addCropCardView = view.findViewById(R.id.addCropCard);
        LinearLayout expandable_addCrop_card = view.findViewById(R.id.expandable_addCrop_card);
        LinearLayout expandable_crop_card = view.findViewById(R.id.collapsibleLayout);
        cropName = view.findViewById(R.id.cropNameInput);
        district = view.findViewById(R.id.disrictInput);
        cropFieldSize = view.findViewById(R.id.fieldSizeInput);
        cropPlantationDate = view.findViewById(R.id.dateInput);
        MaterialButton saveCrop = view.findViewById(R.id.save_crop_details_btn);
        AppCompatImageButton datePicker = view.findViewById(R.id.datePickerBtn);
        addCropSubTitle = view.findViewById(R.id.add_crop_subtitle);
        TextView header = view.findViewById(R.id.headerTextView);
        LinearLayout viewCropHeader = view.findViewById(R.id.viewCropHeader);
        viewCropCard = view.findViewById(R.id.viewCropCard);

        // Setup dropdown for crop names
        List<DropdownItem> cropItems = Arrays.asList(
                new DropdownItem(getString(R.string.vegi1), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi2), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi3), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi4), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi5), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi6), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi7), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi8), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi9), R.drawable.vegetables_salad_svgrepo_com),
                new DropdownItem(getString(R.string.vegi10), R.drawable.vegetables_salad_svgrepo_com)
        );
        CustomDropdownAdapter cropAdapter = new CustomDropdownAdapter(requireContext(), cropItems);
        cropName.setAdapter(cropAdapter);

        // Prevent keyboard from opening automatically
        cropName.setOnTouchListener((v, event) -> {
            cropName.showDropDown();
            return true;
        });

        // Setup dropdown for districts
        List<DropdownItem> districts = Arrays.asList(
                new DropdownItem(getString(R.string.dist1), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist2), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist3), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist4), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist5), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist6), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist7), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist8), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist9), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist10), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist11), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist12), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist13), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist14), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist15), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist16), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist17), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist18), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist19), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist20), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist21), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist22), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist23), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist24), R.drawable.country_land_svgrepo_com),
                new DropdownItem(getString(R.string.dist25), R.drawable.country_land_svgrepo_com)
        );
        CustomDropdownAdapter districtAdapter = new CustomDropdownAdapter(requireContext(), districts);
        district.setAdapter(districtAdapter);

        // Prevent keyboard from opening automatically
        district.setOnTouchListener((v, event) -> {
            district.showDropDown();
            return true;
        });

        // Set up RecyclerView for displaying saved crops
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cardAdapter = new CardAdapter(cardItemList);
        recyclerView.setAdapter(cardAdapter);

        // Load crop data from Firebase on startup
        loadCropsFromFirebase();

        // Add crop card expand/collapse functionality
        addCropCardView.setOnClickListener(view1 -> toggleExpandableSection(expandable_addCrop_card, addCropSubTitle));
        viewCropHeader.setOnClickListener(view1 -> toggleExpandableSection(expandable_crop_card, header));

        // Set listener for the save button
        saveCrop.setOnClickListener(view1 -> saveCropDetails());

        // Date picker functionality
        datePicker.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireActivity(),
                    (view2, year1, monthOfYear, dayOfMonth) -> cropPlantationDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                    year, month, day
            );
            datePickerDialog.show();
        });

        return view;
    }

    // Toggle visibility for expandable sections
    @SuppressLint("SetTextI18n")
    private void toggleExpandableSection(View section, TextView subtitle) {
        if (section.getVisibility() == View.VISIBLE) {
            collapseExpandableSection(section);
            subtitle.setText("Tap to Add Details");
        } else {
            expandExpandableSection(section);
            subtitle.setText("Tap to Collapse");
        }
    }

    // Store the global layout listener as a class member to remove it correctly after use
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;

    // Expands the section with an animation
    private void expandExpandableSection(View section) {
        section.setVisibility(View.VISIBLE);
        section.measure(View.MeasureSpec.makeMeasureSpec(section.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
        final int contentHeight = section.getMeasuredHeight();
        final int extraGap = (int) (0 * section.getContext().getResources().getDisplayMetrics().density);
        final int targetHeight = contentHeight + extraGap;

        section.getLayoutParams().height = 0;
        section.requestLayout();

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            section.getLayoutParams().height = (int) animation.getAnimatedValue();
            section.requestLayout();
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                enableAutoResize(section, extraGap); // Enable auto-resizing after animation completes
            }
        });
        animator.start();
    }

    // Collapses the section with an animation
    private void collapseExpandableSection(View section) {
        final int initialHeight = section.getHeight();
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            int animatedHeight = (int) animation.getAnimatedValue();
            section.getLayoutParams().height = animatedHeight;
            section.requestLayout();

            // If height reaches zero, set visibility to GONE to prevent layout updates
            if (animatedHeight == 0) {
                section.setVisibility(View.GONE);
            }
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Remove the global layout listener after collapsing
                if (mGlobalLayoutListener != null) {
                    section.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                }
            }
        });
        animator.start();
    }

    // Enables auto-resizing of the section after the animation
    private void enableAutoResize(View section, int extraGap) {
        // Create and store the global layout listener
        mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Check if the section is still visible, resize accordingly
                if (section.getVisibility() == View.VISIBLE) {
                    section.measure(View.MeasureSpec.makeMeasureSpec(section.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
                    section.getLayoutParams().height = section.getMeasuredHeight() + extraGap;
                    section.requestLayout();
                } else {
                    // Remove the listener once the section is no longer visible
                    section.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        };

        // Add the global layout listener to the view's tree observer
        section.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }




    // Save crop details to Firebase
    private void saveCropDetails() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        if (userEmail == null) {
            CustomToast customToast = new CustomToast(requireContext());
            customToast.show("User not logged-in", R.drawable.user_cross_svgrepo_com);
            return;
        }

        String cropNameValue = Objects.requireNonNull(cropName.getText()).toString().trim();
        String fieldSizeValue = Objects.requireNonNull(cropFieldSize.getText()).toString().trim();
        String plantationDateValue = Objects.requireNonNull(cropPlantationDate.getText()).toString().trim();

        if (cropNameValue.isEmpty() || fieldSizeValue.isEmpty() || plantationDateValue.isEmpty()) {
            CustomToast customToast = new CustomToast(requireContext());
            customToast.show("Please fill all fields!", R.drawable.warning_svgrepo_com);
            return;
        }

        String sanitizedEmail = userEmail.replace(".", ",");
        DatabaseReference userCropsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail)
                .child("crops")
                .child(cropNameValue);

        userCropsRef.child("name").setValue(cropNameValue);
        userCropsRef.child("fieldSize").setValue(fieldSizeValue);
        userCropsRef.child("plantationDate").setValue(plantationDateValue);

        CustomToast customToast = new CustomToast(requireContext());
        customToast.show("Crop details saved!", R.drawable.icons8_correct);
    }

    // Load crops data from Firebase
    private void loadCropsFromFirebase() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        if (userEmail == null) {
            CustomToast customToast = new CustomToast(requireContext());
            customToast.show("User not logged-in", R.drawable.user_cross_svgrepo_com);
            return;
        }

        String sanitizedEmail = userEmail.replace(".", ",");
        DatabaseReference cropsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail)
                .child("crops");

        cropsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cardItemList.clear();
                for (DataSnapshot cropSnapshot : snapshot.getChildren()) {
                    String name = cropSnapshot.child("name").getValue(String.class);
                    String fieldSize = cropSnapshot.child("fieldSize").getValue(String.class);
                    String plantationDate = cropSnapshot.child("plantationDate").getValue(String.class);
                    String harvestedDate = cropSnapshot.child("harvestDate").getValue(String.class);

                    if (name != null && fieldSize != null && plantationDate != null) {
                        cardItemList.add(new CardItem(name, fieldSize, plantationDate, harvestedDate));
                    }
                }
                cardAdapter.notifyDataSetChanged();

                // Check if there are crops, and show/hide the viewCropCard accordingly
                if (cardItemList.isEmpty()) {
                    viewCropCard.setVisibility(View.GONE);
                } else {
                    viewCropCard.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast customToast = new CustomToast(requireContext());
                customToast.show("Failed to load details!", R.drawable.icons8_wrong);
            }
        });
    }
}