package com.aura.ai;

import org.json.JSONException;
import org.json.JSONObject;

public final class RobotCommand {

    public enum Type {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        STOP
    }

    private final Type type;
    private final long createdAt;

    private RobotCommand(Type type) {
        this.type = type;
        this.createdAt = System.currentTimeMillis();
    }

    public static RobotCommand create(String command) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "Robot command cannot be empty.");
        }

        switch (command.trim().toLowerCase()) {
            case "forward":
                return new RobotCommand(Type.FORWARD);

            case "backward":
                return new RobotCommand(Type.BACKWARD);

            case "left":
                return new RobotCommand(Type.LEFT);

            case "right":
                return new RobotCommand(Type.RIGHT);

            case "stop":
                return new RobotCommand(Type.STOP);

            default:
                throw new IllegalArgumentException(
                        "Unsupported robot command: " + command);
        }
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return type.name().toLowerCase();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("command", getValue());
        json.put("transport", "wifi");
        json.put("timestamp", createdAt);
        return json;
    }

    public boolean isEmergencyStop() {
        return type == Type.STOP;
    }

    @Override
    public String toString() {
        return getValue();
    }
}