
package com.aura.ai;

import android.net.Uri;

import java.util.Locale;

public final class CameraStreamValidator {

    private CameraStreamValidator() {
    }

    public static boolean isValidHttpUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String input = value.trim();

        if (input.contains(" ") || input.length() > 2048) {
            return false;
        }

        try {
            Uri uri = Uri.parse(input);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null || host.trim().isEmpty()) {
                return false;
            }

            scheme = scheme.toLowerCase(Locale.ROOT);

            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                return false;
            }

            int port = uri.getPort();

            return port == -1 || (port >= 1 && port <= 65535);

        } catch (Exception e) {
            return false;
        }
    }

    public static String validateOrError(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Enter a camera stream URL.";
        }

        if (!isValidHttpUrl(value)) {
            return "Enter a valid HTTP or HTTPS URL with a hostname or IP address.";
        }

        return null;
    }
}
