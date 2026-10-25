package com.aura.ai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class HistoryStore {

    private static final String PREFS = "aura_history";
    private static final String KEY_EVENTS = "events";
    private static final int MAX_EVENTS = 1000;

    private HistoryStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);
    }

    private static String timestamp() {
        return new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());
    }

    public static synchronized void add(
            Context context,
            String category,
            String description) {

        if (context == null || category == null
                || description == null) {
            return;
        }

        SharedPreferences preferences = prefs(context);
        JSONArray oldEvents;

        try {
            oldEvents = new JSONArray(
                    preferences.getString(KEY_EVENTS, "[]"));
        } catch (JSONException e) {
            oldEvents = new JSONArray();
        }

        JSONArray updated = new JSONArray();

        JSONObject event = new JSONObject();

        try {
            event.put("time", timestamp());
            event.put("category", category);
            event.put("description", description);
        } catch (JSONException e) {
            return;
        }

        updated.put(event);

        int start = Math.max(0, oldEvents.length() - MAX_EVENTS + 1);

        for (int i = start; i < oldEvents.length(); i++) {
            JSONObject previous = oldEvents.optJSONObject(i);

            if (previous != null) {
                updated.put(previous);
            }
        }

        preferences.edit()
                .putString(KEY_EVENTS, updated.toString())
                .apply();
    }

    public static void addChat(
            Context context, String message, boolean fromUser) {
        add(context, "chat",
                (fromUser ? "You: " : "AURA: ") + message);
    }

    public static void addImage(
            Context context, String imageDescription) {
        add(context, "image", imageDescription);
    }

    public static void addMotorCommand(
            Context context, String command) {
        add(context, "motor", "Command requested: " + command);
    }

    public static void addLockEvent(
            Context context, String eventDescription) {
        add(context, "lock", eventDescription);
    }

    public static void addConnectionEvent(
            Context context, String eventDescription) {
        add(context, "connection", eventDescription);
    }

    public static void addError(
            Context context, String errorDescription) {
        add(context, "error", errorDescription);
    }

    public static synchronized JSONArray getAll(Context context) {
        try {
            return new JSONArray(
                    prefs(context).getString(KEY_EVENTS, "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static synchronized JSONArray getByCategory(
            Context context, String category) {

        JSONArray all = getAll(context);
        JSONArray filtered = new JSONArray();

        for (int i = 0; i < all.length(); i++) {
            JSONObject event = all.optJSONObject(i);

            if (event != null
                    && category.equals(event.optString("category"))) {
                filtered.put(event);
            }
        }

        return filtered;
    }

    public static synchronized void clear(Context context) {
        prefs(context).edit().remove(KEY_EVENTS).apply();
    }
}