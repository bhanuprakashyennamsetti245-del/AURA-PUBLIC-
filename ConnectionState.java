
package com.aura.ai;

public enum ConnectionState {

    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR;

    public boolean isConnected() {
        return this == CONNECTED;
    }

    public boolean isBusy() {
        return this == CONNECTING;
    }

    public boolean isDisconnected() {
        return this == DISCONNECTED;
    }

    public boolean hasError() {
        return this == ERROR;
    }

    public String getDisplayName() {
        switch (this) {
            case CONNECTING:
                return "Connecting";

            case CONNECTED:
                return "Connected";

            case ERROR:
                return "Connection Error";

            case DISCONNECTED:
            default:
                return "Disconnected";
        }
    }
}
