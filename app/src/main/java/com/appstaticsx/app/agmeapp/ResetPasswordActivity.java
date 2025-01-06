package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    FirebaseAuth fbAuth; // Firebase authentication instance
    String strEmail; // Stores the email entered by the user
    ImageView imageView; // Logo image view
    MaterialButton resetBtn; // Reset password button

    private boolean isBackPressedOnce = false; // Tracks if back button was pressed
    private final Handler handler = new Handler(); // Handler for delayed reset of back press flag
    private static final int DOUBLE_TAP_DELAY = 500; // Delay for double-tap detection in milliseconds

    String[] texts = {"Sending email...", "Please wait..."}; // Loading messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge mode for better UI
        setContentView(R.layout.activity_resetpassword);

        // Initialize UI elements
        resetBtn = findViewById(R.id.reset_password_btn);
        TextInputEditText userEmail = findViewById(R.id.emailInputRP);
        imageView = findViewById(R.id.logo_img);

        fbAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        // Set click listener for the reset password button
        resetBtn.setOnClickListener(view -> {

            // Get the entered email address
            strEmail = Objects.requireNonNull(userEmail.getText()).toString().trim();

            // Validate email input
            if (!TextUtils.isEmpty(strEmail)) {
                if (Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
                    try {
                        // Check if the device has an internet connection
                        if (NetworkUtil.isInternetAvailable(this)) {
                            resetPassword(); // Send reset password email
                        } else {
                            // Show a custom dialog if no network is available
                            CustomDialog customDialog = new CustomDialog(this);
                            customDialog.setMessage(getResources().getString(R.string.no_network));
                            customDialog.setAnimation("no_network_anim.json"); // Animation for no network
                            customDialog.show();
                        }
                    } catch (Resources.NotFoundException e) {
                        throw new RuntimeException(e); // Handle missing resources
                    }
                } else {
                    userEmail.setError("Please enter a valid email address!"); // Invalid email error
                }
            } else {
                userEmail.setError("Email cannot be empty!"); // Empty email error
            }
        });
    }

    // Method to send reset password email
    @SuppressLint("SetTextI18n")
    private void resetPassword() {
        // Show a custom loading dialog
        ProcessingCustomDialog processingcustomDialog = new ProcessingCustomDialog(this);
        processingcustomDialog.setMessage(texts);
        processingcustomDialog.setAnimation("loading_anim.json");
        processingcustomDialog.show();

        // Send password reset email using Firebase
        fbAuth.sendPasswordResetEmail(strEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Dismiss the loading dialog
                        processingcustomDialog.dismiss();

                        // Show a success message with custom toast
                        CustomToast customToast = new CustomToast(this);
                        customToast.show("Email Sent!", R.drawable.icons8_correct);
                    }
                });
    }

    // Override the back button behavior
    public void onBackPressed() {
        if (isBackPressedOnce) {
            super.onBackPressed(); // Exit the app
            return;
        }

        this.isBackPressedOnce = true; // Set flag to indicate back button was pressed

        // Navigate back to the SigninActivity
        Intent intent = new Intent(ResetPasswordActivity.this, SigninActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Add transition animation
        finish();

        // Reset the flag after a delay
        handler.postDelayed(() -> isBackPressedOnce = false, DOUBLE_TAP_DELAY);
    }
}
