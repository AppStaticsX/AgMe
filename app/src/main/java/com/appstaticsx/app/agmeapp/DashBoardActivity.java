package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashBoardActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addCropFab;


    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        frameLayout = findViewById(R.id.frame_layout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        replaceFragment(new HomeFragment());

        bottomNavigationView.setBackground(null);

        addCropFab = findViewById(R.id.add_crop_fab);


        showTapTargetOnce();

        // Fab behaviour
        addCropFab.setOnClickListener(view -> {
            // Trigger the click on the add_details menu item
            bottomNavigationView.setSelectedItemId(R.id.add_details);
            // Replace the fragment as needed
            replaceFragment(new AddCropFragment());
        });


        //NavigationMenu item behaviour
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.shorts) {
                replaceFragment(new TipsFragment());
            } else if (itemId == R.id.subscriptions) {
                replaceFragment(new AnalyzeFragment());
            } else if (itemId == R.id.library) {
                replaceFragment(new AccountFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void showTapTargetOnce() {
        // Initialize SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean isTapTargetShown = sharedPreferences.getBoolean("TapTargetShown", false);

        // Check if TapTarget has been shown before
        if (!isTapTargetShown) {

            Typeface customTypeface = ResourcesCompat.getFont(this, R.font.avenirnextltpro_bold);

            List<TapTarget> targets = new ArrayList<>();

            targets.add(com.appstaticsx.app.agmeapp.TapTargetSequenceHelper.createTapTarget(
                    addCropFab, "Manage your crops", "Tap to ADD your crop details!",
                    R.color.my_light_primary, R.color.white, R.color.white, R.color.black, customTypeface, 60));

            // Show the TapTarget sequence
            com.appstaticsx.app.agmeapp.TapTargetSequenceHelper.showTapTargetSequence(
                    this,
                    targets,
                    new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            replaceFragment(new AddCropFragment());
                            bottomNavigationView.setSelectedItemId(R.id.add_details);

                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                        }
                    });

            // Update the flag to indicate TapTarget has been shown
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("TapTargetShown", true);
            editor.apply();
        }
    }
}
