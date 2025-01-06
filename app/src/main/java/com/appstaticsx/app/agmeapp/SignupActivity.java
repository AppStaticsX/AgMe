package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private boolean isBackPressedOnce = false;
    private final Handler handler = new Handler();
    private static final int DOUBLE_TAP_DELAY = 500; // milliseconds

    ImageView logo_img;

    FirebaseDatabase fbDatabase;
    DatabaseReference dbReference;
    private FirebaseAuth mAuth;


    String[] texts = {"Creating Account...", "Almost there...", "Please wait..."};

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mAuth = FirebaseAuth.getInstance();


        // Initialize elements
        TextView signInTv = findViewById(R.id.signInTV);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput_signup);
        TextInputEditText emailInput = findViewById(R.id.emailInput_signup);
        MaterialButton signUpBtn = findViewById(R.id.sign_up_btn);
        TextInputLayout jobInput_layout = findViewById(R.id.jobInput_layout);

        MaterialAutoCompleteTextView jobInput = findViewById(R.id.jobInput);

        PasswordStrengthChecker.setPasswordStrengthWatcher(passwordInput, this);

        List<DropdownItem> items = Arrays.asList(
                new DropdownItem("FARMER", R.drawable.farmer_human_svgrepo_com),
                new DropdownItem("AGRI-OFFICER", R.drawable.human_man_3_svgrepo_com),
                new DropdownItem("RESEARCH-PERSON", R.drawable.human_occupation_4_svgrepo_com)
        );

        CustomDropdownAdapter adapter = new CustomDropdownAdapter(this, items);
        jobInput.setAdapter(adapter);

        String job = jobInput.getText().toString();

        switch (job) {
            case "FARMER":
                jobInput_layout.setStartIconDrawable(R.drawable.farmer_human_svgrepo_com);
                break;
            case "AGRI-OFFICER":
                jobInput_layout.setStartIconDrawable(R.drawable.human_man_3_svgrepo_com);
                break;
            case "RESEARCH-PERSON":
                jobInput_layout.setStartIconDrawable(R.drawable.human_occupation_4_svgrepo_com);
                break;
        }


        // Prevent keyboard from opening
        jobInput.setOnTouchListener((v, event) -> {
            jobInput.showDropDown();
            return true;
        });

        logo_img = findViewById(R.id.logo_img);

        // Handle sign-in redirect
        signInTv.setOnClickListener(view -> {

            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, logo_img, "imgShared");

            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent, activityOptionsCompat.toBundle());
            finish();
        });


        // Handle sign-up button
        signUpBtn.setOnClickListener(view -> {
            String email = Objects.requireNonNull(emailInput.getText()).toString();
            String password = Objects.requireNonNull(passwordInput.getText()).toString();
            String profession = Objects.requireNonNull(jobInput.getText()).toString();

            ProcessingCustomDialog processingcustomDialog = new ProcessingCustomDialog(this);
            processingcustomDialog.setMessage(texts);
            processingcustomDialog.setAnimation("loading_anim.json"); // Name of the animation file in res/raw

            try {
                boolean isEmailEmpty = email.isEmpty();
                boolean isMobileNumberEmpty = profession.isEmpty();
                boolean isPasswordEmpty = password.isEmpty();
                boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

                String encryptedPassword = EncryptionUtil.encrypt(this, password);

                if (isEmailEmpty) {
                    Snackbar.make(findViewById(android.R.id.content), "Email can't be empty", Snackbar.LENGTH_LONG).show();
                } else if (isMobileNumberEmpty) {
                    Snackbar.make(findViewById(android.R.id.content), "Mobile number can't be empty", Snackbar.LENGTH_LONG).show();
                } else if (!isEmailValid) {
                    Snackbar.make(findViewById(android.R.id.content), "Please enter a valid email address", Snackbar.LENGTH_LONG).show();
                } else if (isPasswordEmpty) {
                    Snackbar.make(findViewById(android.R.id.content), "Password can't be empty", Snackbar.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Snackbar.make(findViewById(android.R.id.content), "Password must have at least 6 characters", Snackbar.LENGTH_LONG).show();
                } else {

                    // Check device is connected to the internet & continue sign-up
                    if (NetworkUtil.isInternetAvailable(this)) {

                        fbDatabase = FirebaseDatabase.getInstance();
                        dbReference = fbDatabase.getReference("users");

                        processingcustomDialog.show();

                    } else {
                        CustomDialog customDialog = new CustomDialog(this);
                        customDialog.setMessage(getResources().getString(R.string.no_network));
                        customDialog.setAnimation("no_network_anim.json"); // Name of the animation file in res/raw
                        customDialog.show();
                    }

                    String emailKey = email.replace(".", ",");  // Replacing '.' with ',' for Firebase key

                    // Check if the email already exists in the database
                    dbReference.child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                // Email already exists
                                Snackbar.make(findViewById(android.R.id.content), "Email already registered", Snackbar.LENGTH_LONG).show();

                                processingcustomDialog.dismiss();

                            } else {

                                processingcustomDialog.show();
                                // Email does not exist, proceed with signup
                                Helper helperClass = new Helper(email, encryptedPassword, profession);
                                dbReference.child(emailKey).setValue(helperClass)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {

                                                Snackbar.make(findViewById(android.R.id.content), "You have signed up successfully!", Snackbar.LENGTH_LONG).show();

                                                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                                finish();
                                            } else {
                                                Snackbar.make(findViewById(android.R.id.content), "Signup failed, try again.", Snackbar.LENGTH_LONG).show();
                                            }
                                            processingcustomDialog.dismiss();
                                        });


                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                updateUI(user);
                                            } else {
                                                updateUI(null);
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(findViewById(android.R.id.content), "An error occurred: " + error.getMessage(), Snackbar.LENGTH_LONG).show();

                            processingcustomDialog.dismiss();
                        }
                    });
                }
            } catch (Exception e) {
                Snackbar.make(findViewById(android.R.id.content), "An error occurred: " + e.getMessage(), Snackbar.LENGTH_LONG).show();

                processingcustomDialog.dismiss();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
    }



    // Handle back button
        public void onBackPressed() {
        if (isBackPressedOnce) {
            super.onBackPressed(); // exit the app
            return;
        }

        this.isBackPressedOnce = true;
        CustomToast customToast = new CustomToast(this);
        customToast.show( "Press again to exit  ", R.drawable.logout_2_svgrepo_com);

        // Reset the flag after the delay
        handler.postDelayed(() -> isBackPressedOnce = false, DOUBLE_TAP_DELAY);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }

    private void reload() {
    }
}