package com.aura.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

public final class ConnectionMonitor {

    public interface Listener {
        void onConnectionChanged(
                boolean networkAvailable,
                boolean wifiConnected,
                String description);
    }

    private final Context context;
    private final Listener listener;

    private boolean registered = false;

    private final BroadcastReceiver receiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(
                        Context receivedContext,
                        Intent intent) {
                    notifyCurrentState();
                }
            };

    public ConnectionMonitor(
            Context context,
            Listener listener) {

        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public synchronized void start() {
        if (registered) {
            notifyCurrentState();
            return;
        }

        IntentFilter filter =
                new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                        receiver,
                        filter,
                        Context.RECEIVER_NOT_EXPORTED);
            } else {
                context.registerReceiver(receiver, filter);
            }

            registered = true;

        } catch (Exception exception) {
            registered = false;
        }

        notifyCurrentState();
    }

    public synchronized void stop() {
        if (!registered) {
            return;
        }

        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        } finally {
            registered = false;
        }
    }

    public void notifyCurrentState() {
        if (listener == null) {
            return;
        }

        boolean network =
                NetworkUtils.hasNetworkConnection(context);

        boolean wifi =
                NetworkUtils.isWifiConnected(context);

        String description =
                NetworkUtils.getConnectionDescription(context);

        listener.onConnectionChanged(
                network, wifi, description);
    }

    public synchronized boolean isMonitoring() {
        return registered;
    }
}