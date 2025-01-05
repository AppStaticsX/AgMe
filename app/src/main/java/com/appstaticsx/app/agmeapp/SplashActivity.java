package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds delay
    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Delay for splash screen
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

            Intent intent;
            if (isLoggedIn) {
                // User is already logged in, redirect to Dashboard
                intent = new Intent(SplashActivity.this, DashBoardActivity.class);
            } else {
                // User is not logged in, redirect to Sign-in
                intent = new Intent(SplashActivity.this, SigninActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, SPLASH_DELAY);
    }
}


