package com.aura.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

public final class RobotStatusService {

    public interface Callback {
        void onStatusUpdated(RobotStatus status);
        void onError(String message);
    }

    private RobotStatusService() {
    }

    public static void checkConnection(
            Context context,
            Callback callback) {

        if (context == null) {
            if (callback != null) {
                callback.onError("Android context is unavailable.");
            }
            return;
        }

        Context appContext = context.getApplicationContext();

        PiApiClient.health(appContext, (success, code, body) -> {
            RobotStatus status = RobotStatusStore.load(appContext);

            if (success) {
                status.markConnected();

                try {
                    JSONObject response = new JSONObject(body);

                    if (response.has("battery_percent")
                            || response.has("motor_status")
                            || response.has("camera_status")
                            || response.has("lock_status")) {
                        status.updateFromJson(body);
                        status.markConnected();
                    }

                } catch (Exception ignored) {
                    // A health response may be plain or non-JSON text.
                }

                RobotStatusStore.save(appContext, status);

                if (callback != null) {
                    callback.onStatusUpdated(status);
                }

                HistoryStore.addConnectionEvent(
                        appContext,
                        "Raspberry Pi health check succeeded.");

            } else {
                status.markDisconnected(
                        "Raspberry Pi API is unreachable: " + body);

                RobotStatusStore.save(appContext, status);

                if (callback != null) {
                    callback.onError(status.getMessage());
                }

                HistoryStore.addConnectionEvent(
                        appContext,
                        "Raspberry Pi health check failed: " + body);
            }
        });
    }
}