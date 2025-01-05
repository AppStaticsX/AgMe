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
import android.widget.ImageView;
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

    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";

    private CardAdapterHomeFragment cardAdapter;
    private final List<CardItem> cardItemList = new ArrayList<>();

    private TextView textView;
    private LottieAnimationView loadingAnimation; // Lottie animation view

    private static final String TAG = "HomeFragment";
    private static final String API_KEY = "077fca0117280391cb4c864e32d5ab81"; // Replace with your OpenWeatherMap API key
    private static final double DEFAULT_LATITUDE = 6.8449; // Homagama latitude
    private static final double DEFAULT_LONGITUDE = 80.0029; // Homagama longitude

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Find views
        textView = view.findViewById(R.id.sloganTV);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCrops);
        loadingAnimation = view.findViewById(R.id.loadingAnimation); // Initialize Lottie view

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        cardAdapter = new CardAdapterHomeFragment(cardItemList);
        recyclerView.setAdapter(cardAdapter);

        // Retrieve saved user data
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
        String userNameFromDB = sharedPreferences.getString(KEY_USER_NAME, null);

        // Set greeting message
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

        // Load crops data
        checkAndLoadCropsFromFirebase();

        // Fetch weather for current location
        fetchCurrentLocationWeather();

        return view;
    }

    // Method to find account in Firebase
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

    // Method to check crops and load if available
    private void checkAndLoadCropsFromFirebase() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        if (userEmail == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedEmail = userEmail.replace(".", ",");

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail)
                .child("crops");

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

    // Method to load crops from Firebase
    private void loadCropsFromFirebase() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);

        if (userEmail == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
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
                    String fieldSizeStr = cropSnapshot.child("fieldSize").getValue(String.class);
                    String plantationDate = cropSnapshot.child("plantationDate").getValue(String.class);
                    String harvestedDate = cropSnapshot.child("plantationDate").getValue(String.class);

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

    private void fetchCurrentLocationWeather() {
        // Directly use the default location (Homagama)
        getWeatherData();
    }

    private void getWeatherData() {
        new Thread(() -> {
            try {
                @SuppressLint("DefaultLocale") String urlString = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s", HomeFragment.DEFAULT_LATITUDE, HomeFragment.DEFAULT_LONGITUDE, API_KEY);
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

    private void parseWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject main = jsonObject.getJSONObject("main");
            double temperature = main.getDouble("temp") - 273.15; // Convert from Kelvin to Celsius
            double feelsLike = main.getDouble("feels_like") - 273.15; // Convert from Kelvin to Celsius
            double pressure = main.getDouble("pressure");
            double humidity = main.getDouble("humidity");

            JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
            String iconCode = weather.getString("icon"); // Get the weather icon code
            String iconUrl = String.format("https://openweathermap.org/img/wn/01d@2x.png", iconCode);

            // Debugging
            Log.d(TAG, "Weather Icon Code: " + iconCode);
            Log.d(TAG, "Weather Icon URL: " + iconUrl);

            requireActivity().runOnUiThread(() -> {
                TextView temperatureView = requireView().findViewById(R.id.temperature);
                TextView feelsLikeView = requireView().findViewById(R.id.feel_result);
                TextView pressureView = requireView().findViewById(R.id.windResult);
                TextView humidityView = requireView().findViewById(R.id.humidityResult);
                ImageView weatherIconView = requireView().findViewById(R.id.weatherIconImageView);

                @SuppressLint("DefaultLocale") String tempText = String.format("%.0f°C", temperature);
                @SuppressLint("DefaultLocale") String feelsLikeText = String.format("%.1f°C", feelsLike);
                @SuppressLint("DefaultLocale") String pressureText = String.format("%.0f hPa", pressure);
                @SuppressLint("DefaultLocale") String humidityText = String.format("%.0f%%", humidity);

                temperatureView.setText(tempText);
                feelsLikeView.setText(feelsLikeText);
                pressureView.setText(pressureText);
                humidityView.setText(humidityText);

                // Load weather icon
                Glide.with(requireContext())
                        .load(iconUrl)
                        .placeholder(R.drawable.cloudy_rainy_svgrepo_com) // Optional loading placeholder
                        .error(R.drawable.cloudy_rainy_svgrepo_com) // Fallback if the image cannot be loaded
                        .into(weatherIconView);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error parsing weather data: ", e);
        }
    }



}
