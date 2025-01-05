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

    FirebaseAuth fbAuth;
    String strEmail;
    ImageView imageView;
    MaterialButton resetBtn;

    private boolean isBackPressedOnce = false;
    private final Handler handler = new Handler();
    private static final int DOUBLE_TAP_DELAY = 500;

    String[] texts = {"Sending email...", "Please wait..."};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resetpassword);


        // Initialization
        resetBtn = findViewById(R.id.reset_password_btn);
        TextInputEditText userEmail = findViewById(R.id.emailInputRP);
        imageView = findViewById(R.id.logo_img);

        fbAuth = FirebaseAuth.getInstance();


        // Handle rest password button
        resetBtn.setOnClickListener(view -> {

            strEmail = Objects.requireNonNull(userEmail.getText()).toString().trim();
            if (!TextUtils.isEmpty(strEmail)) {
                if (Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {

                    try {
                        if (NetworkUtil.isInternetAvailable(this)) {
                            resetPassword();

                        } else {
                            CustomDialog customDialog = new CustomDialog(this);
                            customDialog.setMessage(getResources().getString(R.string.no_network));
                            customDialog.setAnimation("no_network_anim.json"); // Name of the animation file in res/raw
                            customDialog.show();
                        }
                    } catch (Resources.NotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    userEmail.setError("Please enter a valid email address!");
                }
            } else {
                userEmail.setError("Email cannot be empty!");
            }
        });
    }

    // Handle reset password email send
    @SuppressLint("SetTextI18n")
    private void resetPassword() {

        ProcessingCustomDialog processingcustomDialog = new ProcessingCustomDialog(this);
        processingcustomDialog.setMessage(texts);
        processingcustomDialog.setAnimation("loading_anim.json");
        processingcustomDialog.show();

        fbAuth.sendPasswordResetEmail(strEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        processingcustomDialog.dismiss();

                        CustomToast customToast = new CustomToast(this);
                        customToast.show("Email Sent!", R.drawable.icons8_correct);

                    }
                });
    }

    // Back button handling
    public void onBackPressed() {
        if (isBackPressedOnce) {
            super.onBackPressed(); // exit the app
            return;
        }

        this.isBackPressedOnce = true;
        Intent intent = new Intent(ResetPasswordActivity.this, SigninActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();

        // Reset the flag after the delay
        handler.postDelayed(() -> isBackPressedOnce = false, DOUBLE_TAP_DELAY);
    }
}
