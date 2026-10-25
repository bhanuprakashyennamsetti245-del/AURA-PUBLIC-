
package com.aura.ai;

import android.content.Context;

import org.json.JSONObject;

public final class RobotBatteryMonitor {

    public interface Callback {
        void onBatteryResult(
                boolean available,
                int percentage,
                String message
        );
    }

    private RobotBatteryMonitor() {
    }

    public static void readFromStatusJson(
            Context context,
            String json,
            Callback callback) {

        if (callback == null) {
            return;
        }

        if (json == null || json.trim().isEmpty()) {
            callback.onBatteryResult(
                    false, -1, "No Raspberry Pi status data received."
            );
            return;
        }

        try {
            JSONObject response = new JSONObject(json);

            JSONObject data = response.optJSONObject("data");
            if (data == null) {
                data = response;
            }

            int battery = findBatteryPercentage(data);

            if (battery < 0 || battery > 100) {
                callback.onBatteryResult(
                        false, -1,
                        "Battery percentage is not available in the Pi response."
                );
                return;
            }

            if (context != null) {
                RobotStatusStore.updateBattery(context, battery);
                HistoryStore.add(
                        context,
                        "battery",
                        "Raspberry Pi reported battery: " + battery + "%"
                );
            }

            callback.onBatteryResult(
                    true, battery, "Battery: " + battery + "%"
            );

        } catch (Exception e) {
            callback.onBatteryResult(
                    false, -1, "Could not read battery status."
            );
        }
    }

    private static int findBatteryPercentage(JSONObject data) {
        String[] keys = {
                "battery_percentage",
                "battery_percent",
                "battery_level",
                "battery"
        };

        for (String key : keys) {
            if (!data.has(key) || data.isNull(key)) {
                continue;
            }

            Object value = data.opt(key);

            if (value instanceof Number) {
                return ((Number) value).intValue();
            }

            if (value instanceof String) {
                try {
                    String text = ((String) value)
                            .trim()
                            .replace("%", "");

                    return Integer.parseInt(text);
                } catch (NumberFormatException ignored) {
                    // Try the next supported field.
                }
            }
        }

        return -1;
    }
}
