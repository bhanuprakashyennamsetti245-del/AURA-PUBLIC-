package com.aura.ai;

import org.json.JSONException;
import org.json.JSONObject;

public final class LockCommand {

    public enum Action {
        LOCK,
        UNLOCK
    }

    private final Action action;
    private final long createdAt;
    private final boolean biometricVerified;

    private LockCommand(
            Action action,
            boolean biometricVerified) {
        this.action = action;
        this.biometricVerified = biometricVerified;
        this.createdAt = System.currentTimeMillis();
    }

    public static LockCommand create(
            String requestedAction,
            boolean biometricVerified) {

        if (!biometricVerified) {
            throw new SecurityException(
                    "Biometric verification is required.");
        }

        if (requestedAction == null) {
            throw new IllegalArgumentException(
                    "Lock action cannot be empty.");
        }

        Action action;

        switch (requestedAction.trim().toLowerCase()) {
            case "lock":
                action = Action.LOCK;
                break;

            case "unlock":
                action = Action.UNLOCK;
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported lock action.");
        }

        return new LockCommand(action, true);
    }

    public Action getAction() {
        return action;
    }

    public String getValue() {
        return action.name().toLowerCase();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isBiometricVerified() {
        return biometricVerified;
    }

    public JSONObject toJson() throws JSONException {
        if (!biometricVerified) {
            throw new SecurityException(
                    "Biometric verification is required.");
        }

        JSONObject json = new JSONObject();
        json.put("action", getValue());
        json.put("timestamp", createdAt);
        json.put("biometric_verified", true);

        return json;
    }
}