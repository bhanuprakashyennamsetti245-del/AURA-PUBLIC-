package com.aura.ai;

import org.json.JSONObject;

public final class RobotStatus {

    private boolean connected;
    private Integer batteryPercent;
    private String motorStatus;
    private String cameraStatus;
    private String lockStatus;
    private String message;
    private long lastUpdated;

    public RobotStatus() {
        connected = false;
        batteryPercent = null;
        motorStatus = "Unknown";
        cameraStatus = "Unknown";
        lockStatus = "Unknown";
        message = "Waiting for Raspberry Pi status.";
        lastUpdated = 0L;
    }

    public synchronized void updateFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            message = "Empty status response.";
            return;
        }

        try {
            JSONObject data = new JSONObject(json);

            if (data.has("connected")) {
                connected = data.optBoolean(
                        "connected", false);
            }

            if (data.has("battery_percent")
                    && !data.isNull("battery_percent")) {

                int battery = data.optInt(
                        "battery_percent", -1);

                batteryPercent = battery >= 0 && battery <= 100
                        ? battery : null;
            }

            if (data.has("motor_status")) {
                motorStatus = safeValue(
                        data.optString("motor_status", "Unknown"));
            }

            if (data.has("camera_status")) {
                cameraStatus = safeValue(
                        data.optString("camera_status", "Unknown"));
            }

            if (data.has("lock_status")) {
                lockStatus = safeValue(
                        data.optString("lock_status", "Unknown"));
            }

            if (data.has("message")) {
                message = safeValue(
                        data.optString("message", ""));
            } else {
                message = "Status response received.";
            }

            lastUpdated = System.currentTimeMillis();

        } catch (Exception exception) {
            message = "Could not read the Pi status response.";
        }
    }

    private String safeValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Unknown";
        }

        return value.trim();
    }

    public synchronized void markDisconnected(String reason) {
        connected = false;
        message = reason == null
                ? "Raspberry Pi disconnected."
                : reason;
        lastUpdated = System.currentTimeMillis();
    }

    public synchronized void markConnected() {
        connected = true;
        message = "Raspberry Pi connected.";
        lastUpdated = System.currentTimeMillis();
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    public synchronized Integer getBatteryPercent() {
        return batteryPercent;
    }

    public synchronized String getMotorStatus() {
        return motorStatus;
    }

    public synchronized String getCameraStatus() {
        return cameraStatus;
    }

    public synchronized String getLockStatus() {
        return lockStatus;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized long getLastUpdated() {
        return lastUpdated;
    }

    public synchronized String getBatteryDisplay() {
        return batteryPercent == null
                ? "Unavailable"
                : batteryPercent + "%";
    }

    public synchronized String toDisplayText() {
        return "Connection: "
                + (connected ? "Connected" : "Disconnected")
                + "\nBattery: " + getBatteryDisplay()
                + "\nMotors: " + motorStatus
                + "\nCamera: " + cameraStatus
                + "\nLock: " + lockStatus
                + "\nDetails: " + message;
    }
}