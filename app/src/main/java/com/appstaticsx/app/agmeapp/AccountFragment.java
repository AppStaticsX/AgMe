package com.appstaticsx.app.agmeapp;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    // UI Elements
    private TextView user_email_tv, user_name_tv;
    private CircleImageView ProfPic;

    // SharedPreferences keys and constants
    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final int IMAGE_PICK_CODE = 100;
    private static final String IMAGE_KEY = "image_data";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize UI elements
        user_email_tv = view.findViewById(R.id.userEmailTV);
        user_name_tv = view.findViewById(R.id.usernameTV);
        MaterialButton logoutButton = view.findViewById(R.id.logout_btn);
        MaterialButton deleteAccButton = view.findViewById(R.id.deleteAcc_btn);
        AppCompatImageButton changeProPicBtn = view.findViewById(R.id.changeProfPic_btn);
        ProfPic = view.findViewById(R.id.ProfPic);

        // Load saved profile picture
        loadSavedImage();

        // Retrieve and display the saved email from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        if (savedEmail != null) {
            findAccountInFirebase(savedEmail);

            // Extract username from email and display it
            int atIndex = savedEmail.indexOf("@");
            if (atIndex != -1) {
                String username = savedEmail.substring(0, atIndex);
                user_name_tv.setText(username);
                user_email_tv.setText(savedEmail);
            }
        } else {
            // Fallback if no email is found
            user_name_tv.setText("Unable to find account.");
            user_email_tv.setText("Unable to find account.");
        }

        // Check for internet availability and handle appropriately
        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            CustomDialog customDialog = new CustomDialog(requireContext());
            customDialog.setMessage(getResources().getString(R.string.no_network));
            customDialog.setAnimation("no_network_anim.json"); // Name of animation file in res/raw
            customDialog.show();
        }

        // Logout button click handler
        logoutButton.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();
            logout();
        });

        // Delete account button click handler
        deleteAccButton.setOnClickListener(view2 -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        logout();
                    }
                });
            }
        });

        // Change profile picture button click handler
        changeProPicBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        return view;
    }

    // Logout method
    private void logout() {
        Context context = requireActivity();

        // Update login status in SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();

        // Clear other SharedPreferences
        SharedPreferences sharedPreferences_all = context.getSharedPreferences("AppPreferences", MODE_PRIVATE);
        sharedPreferences_all.edit().clear().apply();

        // Redirect to Sign-in activity
        Intent intent = new Intent(context, SigninActivity.class);
        startActivity(intent);

        // Close the current activity
        requireActivity().finish();
    }

    // Find account in Firebase using the saved email
    private void findAccountInFirebase(String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query query = reference.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve and display email and username
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String emailFromDB = userSnapshot.child("email").getValue(String.class);

                        if (emailFromDB != null) {
                            int atIndex = emailFromDB.indexOf("@");
                            if (atIndex != -1) {
                                String username = emailFromDB.substring(0, atIndex);
                                user_name_tv.setText(username);
                                user_email_tv.setText(emailFromDB);
                            }
                        }
                    }
                } else {
                    user_name_tv.setText("Account not found.");
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                user_name_tv.setText("Error retrieving account.");
            }
        });
    }

    // Handle the result of the image picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // Convert selected image to Bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(selectedImageUri));

                    // Display the image
                    ProfPic.setImageBitmap(bitmap);

                    // Save the image
                    saveImage(bitmap);
                } catch (Exception e) {
                    CustomToast customToast = new CustomToast(requireContext());
                    customToast.show(e.getMessage(), R.drawable.icons8_wrong);
                }
            }
        }
    }

    // Save the image to SharedPreferences
    private void saveImage(Bitmap bitmap) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Convert Bitmap to Base64 string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Save Base64 string
        editor.putString(IMAGE_KEY, encodedImage);
        editor.apply();
    }

    // Load the saved profile picture from SharedPreferences
    private void loadSavedImage() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String encodedImage = sharedPreferences.getString(IMAGE_KEY, null);

        if (encodedImage != null) {
            // Decode Base64 string to Bitmap
            byte[] byteArray = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            // Set Bitmap to ImageView
            ProfPic.setImageBitmap(bitmap);
        }
    }
}