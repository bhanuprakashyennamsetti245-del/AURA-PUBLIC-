
package com.aura.ai;

import android.content.Context;
import android.content.SharedPreferences;

public final class ConnectionStatusStore {

    private static final String PREFS = "aura_connection_status";

    private static final String KEY_WIFI_STATE = "wifi_state";
    private static final String KEY_BLUETOOTH_STATE = "bluetooth_state";
    private static final String KEY_LAST_MESSAGE = "last_message";
    private static final String KEY_UPDATED_AT = "updated_at";

    private ConnectionStatusStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );
    }

    public static synchronized void updateWifi(
            Context context,
            ConnectionState state,
            String message) {

        update(context, KEY_WIFI_STATE, state, message);
    }

    public static synchronized void updateBluetooth(
            Context context,
            ConnectionState state,
            String message) {

        update(context, KEY_BLUETOOTH_STATE, state, message);
    }

    private static void update(
            Context context,
            String key,
            ConnectionState state,
            String message) {

        if (context == null || state == null) {
            return;
        }

        prefs(context).edit()
                .putString(key, state.name())
                .putString(KEY_LAST_MESSAGE,
                        message == null ? "" : message)
                .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    public static ConnectionState getWifiState(Context context) {
        return getState(context, KEY_WIFI_STATE);
    }

    public static ConnectionState getBluetoothState(Context context) {
        return getState(context, KEY_BLUETOOTH_STATE);
    }

    private static ConnectionState getState(
            Context context,
            String key) {

        if (context == null) {
            return ConnectionState.DISCONNECTED;
        }

        String value = prefs(context).getString(
                key,
                ConnectionState.DISCONNECTED.name()
        );

        try {
            return ConnectionState.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ConnectionState.DISCONNECTED;
        }
    }

    public static String getLastMessage(Context context) {
        if (context == null) {
            return "";
        }

        return prefs(context).getString(KEY_LAST_MESSAGE, "");
    }

    public static long getUpdatedAt(Context context) {
        if (context == null) {
            return 0L;
        }

        return prefs(context).getLong(KEY_UPDATED_AT, 0L);
    }

    public static synchronized void clear(Context context) {
        if (context == null) {
            return;
        }

        prefs(context).edit().clear().apply();
    }
}
