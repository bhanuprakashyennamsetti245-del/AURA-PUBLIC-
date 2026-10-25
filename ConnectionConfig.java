
package com.aura.ai;

import android.content.Context;
import android.content.SharedPreferences;

public final class ConnectionConfig {

    private static final String PREFS = "aura_connection_settings";
    private static final String KEY_PI_IP = "pi_ip";
    private static final String KEY_PI_PORT = "pi_port";
    private static final String KEY_LANGUAGE = "language";

    private ConnectionConfig() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);
    }

    public static String getPiIp(Context context) {
        return prefs(context).getString(KEY_PI_IP, "");
    }

    public static boolean savePiIp(Context context, String ip) {
        if (ip == null) return false;

        String value = ip.trim();
        if (!value.matches(
                "^(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}"
                + "(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$")) {
            return false;
        }

        prefs(context).edit().putString(KEY_PI_IP, value).apply();
        return true;
    }

    public static int getPiPort(Context context) {
        return prefs(context).getInt(KEY_PI_PORT, 8000);
    }

    public static boolean savePiPort(Context context, int port) {
        if (port < 1 || port > 65535) return false;

        prefs(context).edit().putInt(KEY_PI_PORT, port).apply();
        return true;
    }

    public static String getLanguage(Context context) {
        return prefs(context).getString(KEY_LANGUAGE, "en");
    }

    public static void saveLanguage(Context context, String language) {
        if (!"en".equals(language) && !"te".equals(language)) {
            return;
        }
        prefs(context).edit().putString(KEY_LANGUAGE, language).apply();
    }
}
