package com.aura.ai;

import android.content.Context;
import android.content.SharedPreferences;

public final class CameraConfig {

    private static final String PREFS = "aura_camera_settings";
    private static final String KEY_STREAM_URL = "stream_url";

    private CameraConfig() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);
    }

    public static String getStreamUrl(Context context) {
        return prefs(context).getString(KEY_STREAM_URL, "");
    }

    public static boolean saveStreamUrl(
            Context context, String streamUrl) {

        if (context == null || streamUrl == null) {
            return false;
        }

        String value = streamUrl.trim();

        if (value.isEmpty()) {
            prefs(context).edit()
                    .remove(KEY_STREAM_URL)
                    .apply();
            return true;
        }

        if (!(value.startsWith("http://")
                || value.startsWith("https://"))) {
            return false;
        }

        if (value.contains(" ")) {
            return false;
        }

        prefs(context).edit()
                .putString(KEY_STREAM_URL, value)
                .apply();

        return true;
    }

    public static String buildPiStreamUrl(Context context) {
        String ip = ConnectionConfig.getPiIp(context);
        int port = ConnectionConfig.getPiPort(context);

        if (ip == null || ip.trim().isEmpty()) {
            return "";
        }

        return "http://" + ip.trim() + ":" + port + "/camera/stream";
    }

    public static String buildPiSnapshotUrl(Context context) {
        String ip = ConnectionConfig.getPiIp(context);
        int port = ConnectionConfig.getPiPort(context);

        if (ip == null || ip.trim().isEmpty()) {
            return "";
        }

        return "http://" + ip.trim() + ":" + port + "/camera/snapshot";
    }
}