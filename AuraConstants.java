package com.aura.ai;

import java.util.UUID;

public final class AuraConstants {

    public static final String APP_NAME = "AURA AI";

    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_TELUGU = "te";

    public static final int DEFAULT_PI_API_PORT = 8000;

    public static final String API_HEALTH_PATH = "/api/health";
    public static final String API_CHAT_PATH = "/api/chat";

    public static final String BLUETOOTH_SPP_UUID =
            "00001101-0000-1000-8000-00805F9B34FB";

    public static final UUID BLUETOOTH_SPP =
            UUID.fromString(BLUETOOTH_SPP_UUID);

    public static final String PACKET_CHAT = "chat";
    public static final String PACKET_COMMAND = "command";
    public static final String PACKET_CAMERA_SNAPSHOT =
            "camera_snapshot";

    public static final int MAX_CHAT_LENGTH = 1000;

    public static final boolean MOTOR_CONTROL_DEFAULT_ENABLED = false;
    public static final boolean PHYSICAL_LOCK_DEFAULT_ENABLED = false;

    private AuraConstants() {
    }

    public static boolean isSupportedLanguage(String language) {
        return LANGUAGE_ENGLISH.equals(language)
                || LANGUAGE_TELUGU.equals(language);
    }

    public static String normaliseLanguage(String language) {
        return LANGUAGE_TELUGU.equals(language)
                ? LANGUAGE_TELUGU
                : LANGUAGE_ENGLISH;
    }

    public static boolean isValidChat(String text) {
        return text != null
                && !text.trim().isEmpty()
                && text.length() <= MAX_CHAT_LENGTH;
    }
}