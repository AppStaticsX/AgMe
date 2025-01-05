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

    MaterialButton google_sign_in_btn;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;
    ImageView logo_img;

    TextInputEditText emailInput, passwordInput;
    MaterialButton signIn_btn;
    TextView redirectSignUp, resetPasswordTv;

    private FirebaseAuth mAuth;

    int RC_SIGN_IN = 20;

    private boolean isBackPressedOnce = false;
    private final Handler handler = new Handler();
    private static final int DOUBLE_TAP_DELAY = 500;

    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    String[] texts = {"Checking Data...", "Almost there...", "Please wait..."};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signIn_btn = findViewById(R.id.sign_in_btn);
        redirectSignUp = findViewById(R.id.signUpTV);
        resetPasswordTv = findViewById(R.id.resetPasswordTV);
        logo_img = findViewById(R.id.logo_img);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mAuth = FirebaseAuth.getInstance();

        // Check device is connected to the internet
        if (NetworkUtil.isInternetAvailable(this)) {

        } else {
            CustomDialog customDialog = new CustomDialog(this);
            customDialog.setMessage(getResources().getString(R.string.no_network));
            customDialog.setAnimation("no_network_anim.json"); // Name of the animation file in res/raw
            customDialog.show();
        }


        // Setting-up Firebase
        google_sign_in_btn = findViewById(R.id.google_sign_in_btn);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(SigninActivity.this, googleSignInOptions);


        // reset password textView handle
        resetPasswordTv.setOnClickListener(view -> {
            Intent intent = new Intent(SigninActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            finish();
        });


        // Sign-in button logic
        google_sign_in_btn.setOnClickListener(view -> {
            if (NetworkUtil.isInternetAvailable(this)) {
                signIn();

            } else {
                CustomDialog customDialog = new CustomDialog(this);
                customDialog.setMessage(getResources().getString(R.string.no_network));
                customDialog.setAnimation("no_network_anim.json"); // Name of the animation file in res/raw
                customDialog.show();
            }
        });


        // Login button logic
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
                        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        // Save login status & email in SharedPreferences

                                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                        editor.putString("userEmail", userEmail);
                                        editor.apply();

                                        onLoginSuccess();
                                        updateUI(user);
                                    } else {
                                        // If sign in fails, check the exception
                                        processingcustomDialog.dismiss();
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        } catch (FirebaseAuthException e) {
                                            // User does not exist
                                            CustomToast customToast = new CustomToast(this);
                                            customToast.show("Please enter valid detail", R.drawable.icons8_wrong);
                                        } catch (Exception e) {
                                            // Incorrect password
                                            CustomToast customToast = new CustomToast(this);
                                            customToast.show(e.getMessage(), R.drawable.icons8_wrong);
                                        }
                                        updateUI(null);
                                    }
                                });


                    } else {
                        CustomDialog customDialog = new CustomDialog(this);
                        customDialog.setMessage(getResources().getString(R.string.no_network));
                        customDialog.setAnimation("no_network_anim.json"); // Name of the animation file in res/raw
                        customDialog.show();
                    }
                }
        });


        // Sign-up redirect button
        redirectSignUp.setOnClickListener(view -> {

            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, logo_img, "imgShared");

            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent, activityOptionsCompat.toBundle());
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void updateUI(FirebaseUser ignoredUser) {
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

    private void reload() { }


    // Validate Email
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

    // Validate Password
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

    // Handle Google sign-in
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
                firebaseAuth(account.getIdToken());
            } catch (Exception e) {
                CustomToast customToast = new CustomToast(this);
                customToast.show(e.getMessage(), R.drawable.icons8_wrong);
            }

        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        HashMap<String, Object> map = new HashMap<>();
                        assert user != null;
                        String emailKey = Objects.requireNonNull(user.getEmail()).replace(".", ",");  // Replacing '.' with ',' for Firebase key
                        map.put("email", user.getEmail());
                        map.put("name", user.getDisplayName());
                        map.put("profile", Objects.requireNonNull(user.getPhotoUrl()).toString());
                        firebaseDatabase.getReference().child("users").child(emailKey).setValue(map);

                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                        editor.putString("userName", user.getDisplayName());
                        editor.putString("userEmail", user.getEmail());
                        editor.apply();

                        onLoginSuccess();
                    }

                    else {
                        CustomToast customToast = new CustomToast(SigninActivity.this);
                        customToast.show("Something went wrong!", R.drawable.icons8_wrong);
                    }
                });
    }

    private void onLoginSuccess() {
        // Redirect to Dashboard
        Intent intent = new Intent(SigninActivity.this, DashBoardActivity.class);
        startActivity(intent);
        finish();
    }


    // Back button handling
    public void onBackPressed() {
        if (isBackPressedOnce) {
            super.onBackPressed(); // exit the app
            return;
        }

        this.isBackPressedOnce = true;
        CustomToast customToast = new CustomToast(this);
        customToast.show("Press again to exit  ", R.drawable.logout_2_svgrepo_com);

        // Reset the flag after the delay
        handler.postDelayed(() -> isBackPressedOnce = false, DOUBLE_TAP_DELAY);
    }

}