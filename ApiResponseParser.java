package com.aura.ai;

import org.json.JSONException;
import org.json.JSONObject;

public final class ApiResponseParser {

    private ApiResponseParser() {
    }

    public static JSONObject parseObject(String raw)
            throws JSONException {

        if (raw == null || raw.trim().isEmpty()) {
            throw new JSONException("Response is empty.");
        }

        return new JSONObject(raw);
    }

    public static String getChatReply(String raw) {
        try {
            JSONObject json = parseObject(raw);

            String[] candidates = {
                    "reply",
                    "response",
                    "answer",
                    "message"
            };

            for (String key : candidates) {
                String value = json.optString(key, "");

                if (!value.trim().isEmpty()
                        && !"null".equalsIgnoreCase(value)) {
                    return value.trim();
                }
            }

            return raw == null ? "" : raw;

        } catch (JSONException exception) {
            return raw == null ? "" : raw;
        }
    }

    public static boolean indicatesSuccess(String raw) {
        try {
            JSONObject json = parseObject(raw);

            if (json.has("ok")) {
                return json.optBoolean("ok", false);
            }

            if (json.has("success")) {
                return json.optBoolean("success", false);
            }

            return false;

        } catch (JSONException exception) {
            return false;
        }
    }

    public static String getError(String raw) {
        try {
            JSONObject json = parseObject(raw);

            String error = json.optString("error", "");

            if (error.trim().isEmpty()) {
                error = json.optString("detail", "");
            }

            if (error.trim().isEmpty()) {
                error = json.optString("message", "");
            }

            return error.trim().isEmpty()
                    ? "The Raspberry Pi returned an unknown error."
                    : error.trim();

        } catch (JSONException exception) {
            return raw == null || raw.trim().isEmpty()
                    ? "The Raspberry Pi returned an empty response."
                    : raw.trim();
        }
    }

    public static String getString(
            JSONObject json,
            String key,
            String fallback) {

        if (json == null || key == null) {
            return fallback;
        }

        String value = json.optString(key, fallback);

        return value == null || value.trim().isEmpty()
                ? fallback
                : value.trim();
    }
}