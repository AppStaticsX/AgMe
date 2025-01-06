package com.appstaticsx.app.agmeapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/**
 * Utility class for checking internet availability.
 */
public class NetworkUtil {
    public static boolean isInternetAvailable(Context context) {
        // Get the connectivity manager to check network status
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            // Get the currently active network
            Network network = connectivityManager.getActiveNetwork();

            // Get network capabilities (e.g., internet access)
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

            // Return true if the network has internet capability
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }

        // Return false if no network is active or available
        return false;
    }
}
