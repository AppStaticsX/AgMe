package com.appstaticsx.app.agmeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class SigninActivity extends AppCompatActivity {

    MaterialButton google_sign_in_btn; // Button for Google Sign-In
    FirebaseAuth firebaseAuth; // Firebase authentication instance
    FirebaseDatabase firebaseDatabase; // Firebase database instance
    GoogleSignInOptions googleSignInOptions; // Google Sign-In options
    GoogleSignInClient googleSignInClient; // Google Sign-In client
    ImageView logo_img; // App logo image

    TextInputEditText emailInput, passwordInput; // Email and password input fields
    MaterialButton signIn_btn; // Button for email/password sign-in
    TextView redirectSignUp, resetPasswordTv; // Links for sign-up and reset password

    private FirebaseAuth mAuth; // Firebase authentication

    int RC_SIGN_IN = 20; // Request code for Google Sign-In

    private boolean isBackPressedOnce = false; // Track back button presses
    private final Handler handler = new Handler(); // Handler for delayed actions
    private static final int DOUBLE_TAP_DELAY = 500; // Delay for double-tap exit

    private static final String PREFS_NAME = "UserPreferences"; // SharedPreferences file name
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn"; // Key for login status

    String[] texts = {"Checking Data...", "Almost there...", "Please wait..."}; // Loading messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge UI
        setContentView(R.layout.activity_signin);

        // Initialize UI elements
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signIn_btn = findViewById(R.id.sign_in_btn);
        redirectSignUp = findViewById(R.id.signUpTV);
        resetPasswordTv = findViewById(R.id.resetPasswordTV);
        logo_img = findViewById(R.id.logo_img);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable night mode

        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        // Check for internet connectivity
        if (!NetworkUtil.isInternetAvailable(this)) {
            CustomDialog customDialog = new CustomDialog(this);
            customDialog.setMessage(getResources().getString(R.string.no_network));
            customDialog.setAnimation("no_network_anim.json"); // No network animation
            customDialog.show();
        }

        // Initialize Firebase and Google Sign-In
        google_sign_in_btn = findViewById(R.id.google_sign_in_btn);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(SigninActivity.this, googleSignInOptions);

        // Reset password textView click handler
        resetPasswordTv.setOnClickListener(view -> {
            Intent intent = new Intent(SigninActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            finish(); // End the current activity
        });

        // Google Sign-In button click handler
        google_sign_in_btn.setOnClickListener(view -> {
            if (NetworkUtil.isInternetAvailable(this)) {
                signIn(); // Start Google Sign-In process
            } else {
                CustomDialog customDialog = new CustomDialog(this);
                customDialog.setMessage(getResources().getString(R.string.no_network));
                customDialog.setAnimation("no_network_anim.json"); // No network animation
                customDialog.show();
            }
        });

        // Email/password Sign-In button click handler
        signIn_btn.setOnClickListener(view -> {
            String userEmail = Objects.requireNonNull(emailInput.getText()).toString().trim();
            String userPassword = Objects.requireNonNull(passwordInput.getText()).toString().trim();

            ProcessingCustomDialog processingcustomDialog = new ProcessingCustomDialog(this);
            processingcustomDialog.setMessage(texts);
            processingcustomDialog.setAnimation("loading_anim.json");
            processingcustomDialog.show();

            if (!validateEmail() | !validatePassword()) {
                processingcustomDialog.dismiss();
            } else {
                if (NetworkUtil.isInternetAvailable(this)) {
                    // Firebase Email/Password authentication
                    mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Save user login info in SharedPreferences
                                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                    editor.putString("userEmail", userEmail);
                                    editor.apply();

                                    onLoginSuccess(); // Navigate to dashboard
                                    updateUI(user); // Update UI for logged-in user
                                } else {
                                    processingcustomDialog.dismiss();
                                    try {
                                        throw Objects.requireNonNull(task.getException());
                                    } catch (FirebaseAuthException e) {
                                        CustomToast customToast = new CustomToast(this);
                                        customToast.show("Please enter valid details", R.drawable.icons8_wrong);
                                    } catch (Exception e) {
                                        CustomToast customToast = new CustomToast(this);
                                        customToast.show(e.getMessage(), R.drawable.icons8_wrong);
                                    }
                                    updateUI(null);
                                }
                            });
                } else {
                    CustomDialog customDialog = new CustomDialog(this);
                    customDialog.setMessage(getResources().getString(R.string.no_network));
                    customDialog.setAnimation("no_network_anim.json"); // No network animation
                    customDialog.show();
                }
            }
        });

        // Redirect to Sign-Up page
        redirectSignUp.setOnClickListener(view -> {
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, logo_img, "imgShared");
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent, activityOptionsCompat.toBundle());
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Slide animation
            finish(); // End current activity
        });
    }

    private void updateUI(FirebaseUser ignoredUser) {
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload(); // Reload user data if logged in
        }
    }

    private void reload() { }

    // Validate Email Input
    public boolean validateEmail() {
        String val = Objects.requireNonNull(emailInput.getText()).toString();
        if (val.isEmpty()) {
            emailInput.setError("Email cannot be empty");
            return false;
        } else {
            emailInput.setError(null);
            return true;
        }
    }

    // Validate Password Input
    public boolean validatePassword() {
        String val = Objects.requireNonNull(passwordInput.getText()).toString();
        if (val.isEmpty()) {
            passwordInput.setError("Password cannot be empty");
            return false;
        } else {
            passwordInput.setError(null);
            return true;
        }
    }

    // Google Sign-In Handler
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken()); // Authenticate with Firebase
            } catch (Exception e) {
                CustomToast customToast = new CustomToast(this);
                customToast.show(e.getMessage(), R.drawable.icons8_wrong);
            }
        }
    }

    // Firebase Authentication for Google Sign-In
    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        // Save user info to Firebase Database
                        HashMap<String, Object> map = new HashMap<>();
                        assert user != null;
                        String emailKey = Objects.requireNonNull(user.getEmail()).replace(".", ",");
                        map.put("email", user.getEmail());
                        map.put("name", user.getDisplayName());
                        map.put("profile", Objects.requireNonNull(user.getPhotoUrl()).toString());
                        firebaseDatabase.getReference().child("users").child(emailKey).setValue(map);

                        // Save user login info in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                        editor.putString("userName", user.getDisplayName());
                        editor.putString("userEmail", user.getEmail());
                        editor.apply();

                        onLoginSuccess(); // Navigate to dashboard
                    } else {
                        CustomToast customToast = new CustomToast(SigninActivity.this);
                        customToast.show("Something went wrong!", R.drawable.icons8_wrong);
                    }
                });
    }

    private void onLoginSuccess() {
        Intent intent = new Intent(SigninActivity.this, DashBoardActivity.class);
        startActivity(intent); // Navigate to dashboard
        finish(); // End current activity
    }

    // Handle back button presses
    public void onBackPressed() {
        if (isBackPressedOnce) {
            super.onBackPressed(); // Exit the app
            return;
        }

        this.isBackPressedOnce = true;
        CustomToast customToast = new CustomToast(this);
        customToast.show("Press again to exit", R.drawable.logout_2_svgrepo_com);

        // Reset flag after delay
        handler.postDelayed(() -> isBackPressedOnce = false, DOUBLE_TAP_DELAY);
    }
}