package com.aura.ai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public final class RobotStatusStore {

    private static final String PREFS = "aura_robot_status";
    private static final String KEY_STATUS = "status_json";
    private static final String KEY_SAVED_AT = "saved_at";

    private RobotStatusStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized void save(
            Context context, RobotStatus status) {

        if (context == null || status == null) {
            return;
        }

        JSONObject json = new JSONObject();

        try {
            json.put("connected", status.isConnected());

            Integer battery = status.getBatteryPercent();
            json.put("battery_percent",
                    battery == null ? JSONObject.NULL : battery);

            json.put("motor_status", status.getMotorStatus());
            json.put("camera_status", status.getCameraStatus());
            json.put("lock_status", status.getLockStatus());
            json.put("message", status.getMessage());
            json.put("last_updated", status.getLastUpdated());

            prefs(context).edit()
                    .putString(KEY_STATUS, json.toString())
                    .putLong(KEY_SAVED_AT, System.currentTimeMillis())
                    .apply();

        } catch (JSONException ignored) {
            // Leave the previously saved status intact.
        }
    }

    public static synchronized RobotStatus load(Context context) {
        RobotStatus status = new RobotStatus();

        if (context == null) {
            return status;
        }

        String raw = prefs(context).getString(KEY_STATUS, null);

        if (raw == null || raw.trim().isEmpty()) {
            status.markDisconnected(
                    "No previously saved robot status.");
            return status;
        }

        try {
            JSONObject json = new JSONObject(raw);
            status.updateFromJson(json.toString());

            if (!json.optBoolean("connected", false)) {
                status.markDisconnected(
                        "Last saved status was disconnected.");
            }

        } catch (JSONException exception) {
            status.markDisconnected(
                    "Saved status data could not be read.");
        }

        return status;
    }

    public static long getSavedAt(Context context) {
        if (context == null) {
            return 0L;
        }

        return prefs(context).getLong(KEY_SAVED_AT, 0L);
    }

    public static synchronized void clear(Context context) {
        if (context == null) {
            return;
        }

        prefs(context).edit()
                .remove(KEY_STATUS)
                .remove(KEY_SAVED_AT)
                .apply();
    }
}