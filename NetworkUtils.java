package com.aura.ai;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;

public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean hasNetworkConnection(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = manager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(network);

            return capabilities != null
                    && capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }

        android.net.NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = manager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(network);

            return capabilities != null
                    && capabilities.hasTransport(
                            NetworkCapabilities.TRANSPORT_WIFI);
        }

        android.net.NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null
                && info.isConnected()
                && info.getType()
                        == ConnectivityManager.TYPE_WIFI;
    }

    public static String getConnectionDescription(Context context) {
        if (context == null) {
            return "Connection status unavailable.";
        }

        if (isWifiConnected(context)) {
            return "Connected to Wi-Fi. Raspberry Pi reachability "
                    + "still needs to be tested.";
        }

        if (hasNetworkConnection(context)) {
            return "Connected through a non-Wi-Fi network.";
        }

        return "No internet-capable network detected. "
                + "Bluetooth may still work.";
    }
}