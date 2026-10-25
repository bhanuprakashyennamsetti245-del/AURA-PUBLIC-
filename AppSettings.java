package com.aura.ai;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppSettings {

    private static final String PREFS = "aura_app_settings";

    private static final String KEY_VOICE_ENABLED = "voice_enabled";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_CONFIRM_MOTOR = "confirm_motor";
    private static final String KEY_CONFIRM_LOCK = "confirm_lock";

    private AppSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE);
    }

    public static boolean isVoiceEnabled(Context context) {
        return prefs(context).getBoolean(
                KEY_VOICE_ENABLED, true);
    }

    public static void setVoiceEnabled(
            Context context, boolean enabled) {
        prefs(context).edit()
                .putBoolean(KEY_VOICE_ENABLED, enabled)
                .apply();
    }

    public static String getLanguage(Context context) {
        return prefs(context).getString(KEY_LANGUAGE, "en");
    }

    public static void setLanguage(
            Context context, String language) {

        if (!"en".equals(language) && !"te".equals(language)) {
            return;
        }

        prefs(context).edit()
                .putString(KEY_LANGUAGE, language)
                .apply();

        ConnectionConfig.saveLanguage(context, language);
    }

    public static boolean shouldConfirmMotorCommands(Context context) {
        return prefs(context).getBoolean(
                KEY_CONFIRM_MOTOR, true);
    }

    public static void setConfirmMotorCommands(
            Context context, boolean confirm) {
        prefs(context).edit()
                .putBoolean(KEY_CONFIRM_MOTOR, confirm)
                .apply();
    }

    public static boolean shouldConfirmLockActions(Context context) {
        return prefs(context).getBoolean(
                KEY_CONFIRM_LOCK, true);
    }

    public static void setConfirmLockActions(
            Context context, boolean confirm) {
        prefs(context).edit()
                .putBoolean(KEY_CONFIRM_LOCK, confirm)
                .apply();
    }
}