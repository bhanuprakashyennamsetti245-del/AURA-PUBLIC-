package com.aura.ai;

import android.util.Log;

public final class AuraLogger {

    private static final String TAG = "AURA_AI";

    private AuraLogger() {
    }

    public static void debug(String message) {
        Log.d(TAG, safe(message));
    }

    public static void info(String message) {
        Log.i(TAG, safe(message));
    }

    public static void warning(String message) {
        Log.w(TAG, safe(message));
    }

    public static void error(String message) {
        Log.e(TAG, safe(message));
    }

    public static void error(String message, Throwable throwable) {
        if (throwable == null) {
            Log.e(TAG, safe(message));
        } else {
            Log.e(TAG, safe(message), throwable);
        }
    }

    private static String safe(String message) {
        return message == null ? "" : message;
    }
}