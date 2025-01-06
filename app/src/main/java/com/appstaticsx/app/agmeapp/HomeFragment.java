package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // Constants for SharedPreferences and Firebase keys
    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";

    // Constants for Weather API
    private static final String TAG = "HomeFragment";
    private static final String API_KEY = "206a662b2d424dc607a07b82783d6809"; // Replace with your OpenWeatherMap API key
    private static final double DEFAULT_LATITUDE = 6.8449; // Homagama latitude
    private static final double DEFAULT_LONGITUDE = 80.0029; // Homagama longitude

    // UI Elements and adapters
    private CardAdapterHomeFragment cardAdapter;
    private final List<CardItem> cardItemList = new ArrayList<>();
    private TextView textView;
    private LottieAnimationView loadingAnimation;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Find and initialize views
        textView = view.findViewById(R.id.sloganTV);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCrops);
        loadingAnimation = view.findViewById(R.id.loadingAnimation);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        cardAdapter = new CardAdapterHomeFragment(cardItemList);
        recyclerView.setAdapter(cardAdapter);

        // Retrieve user data from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
        String userNameFromDB = sharedPreferences.getString(KEY_USER_NAME, null);

        // Display greeting based on user data
        if (savedEmail != null) {
            findAccountInFirebase(savedEmail);
            int atIndex = savedEmail.indexOf("@");
            if (atIndex != -1) {
                String username = savedEmail.substring(0, atIndex);
                textView.setText("Hi, " + username + "!");
            } else {
                textView.setText("Hi, " + userNameFromDB + "!");
            }
        } else if (userNameFromDB != null) {
            textView.setText("Hi, " + userNameFromDB + "!");
        } else {
            textView.setText("User email not found!");
        }

        // Load crops data and fetch weather
        checkAndLoadCropsFromFirebase();
        fetchCurrentLocationWeather();

        return view;
    }

    /**
     * Finds the user account in Firebase using the provided email.
     */
    private void findAccountInFirebase(String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query query = reference.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String emailFromDB = userSnapshot.child("email").getValue(String.class);
                        if (emailFromDB != null) {
                            int atIndex = emailFromDB.indexOf("@");
                            if (atIndex != -1) {
                                String username = emailFromDB.substring(0, atIndex);
                                textView.setText("Hi, " + username + "!");
                            }
                        }
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textView.setText("Error retrieving account.");
            }
        });
    }

    /**
     * Checks for crops data in Firebase and loads it if available.
     */
    private void checkAndLoadCropsFromFirebase() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        if (userEmail == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedEmail = userEmail.replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail).child("crops");

        // Show loading animation
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loadCropsFromFirebase();
                } else {
                    Toast.makeText(getContext(), "No crops data found", Toast.LENGTH_SHORT).show();
                    loadingAnimation.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking crops data", Toast.LENGTH_SHORT).show();
                loadingAnimation.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Loads crops data from Firebase and updates the RecyclerView.
     */
    private void loadCropsFromFirebase() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        if (userEmail == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedEmail = userEmail.replace(".", ",");
        DatabaseReference cropsRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail).child("crops");

        cropsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cardItemList.clear();

                for (DataSnapshot cropSnapshot : snapshot.getChildren()) {
                    String name = cropSnapshot.child("name").getValue(String.class);
                    String fieldSizeStr = cropSnapshot.child("fieldSize").getValue(String.class);
                    String plantationDate = cropSnapshot.child("plantationDate").getValue(String.class);
                    String harvestedDate = cropSnapshot.child("harvestedDate").getValue(String.class);

                    if (name != null && fieldSizeStr != null && plantationDate != null) {
                        cardItemList.add(new CardItem(name, fieldSizeStr, plantationDate, harvestedDate));
                    }
                }
                cardAdapter.notifyDataSetChanged();

                // Hide loading animation
                loadingAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load crops", Toast.LENGTH_SHORT).show();
                loadingAnimation.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Fetches current location weather using default latitude and longitude.
     */
    private void fetchCurrentLocationWeather() {
        getWeatherData();
    }

    /**
     * Fetches weather data from OpenWeatherMap API.
     */
    private void getWeatherData() {
        new Thread(() -> {
            try {
                @SuppressLint("DefaultLocale") String urlString = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s",
                        DEFAULT_LATITUDE, DEFAULT_LONGITUDE, API_KEY);
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                parseWeatherResponse(response.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data: ", e);
            }
        }).start();
    }

    /**
     * Parses weather data from the API response and updates the UI.
     */
    @SuppressLint("CheckResult")
    private void parseWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject main = jsonObject.getJSONObject("main");
            double temperature = main.getDouble("temp") - 273.15; // Convert from Kelvin to Celsius
            double pressure = main.getDouble("pressure");
            double humidity = main.getDouble("humidity");

            JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
            String iconCode = weather.getString("icon");
            String iconUrl = String.format("https://openweathermap.org/img/wn/%s@2x.png", iconCode);

            requireActivity().runOnUiThread(() -> {
                TextView temperatureView = requireView().findViewById(R.id.temperature);
                TextView pressureView = requireView().findViewById(R.id.windResult);
                TextView humidityView = requireView().findViewById(R.id.humidityResult);

                @SuppressLint("DefaultLocale") String tempText = String.format("%.0f°C", temperature);
                @SuppressLint("DefaultLocale") String pressureText = String.format("%.0f hPa", pressure);
                @SuppressLint("DefaultLocale") String humidityText = String.format("%.0f%%", humidity);

                temperatureView.setText(tempText);
                pressureView.setText(pressureText);
                humidityView.setText(humidityText);

                // Load weather icon
                Glide.with(requireContext())
                        .load(iconUrl)
                        .placeholder(R.drawable.cloudy_rainy_svgrepo_com)
                        .error(R.drawable.cloudy_rainy_svgrepo_com);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error parsing weather data: ", e);
        }
    }
}