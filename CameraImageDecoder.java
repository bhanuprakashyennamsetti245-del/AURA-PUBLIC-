package com.aura.ai;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public final class CameraImageDecoder {

    private static final int MAX_ENCODED_LENGTH = 15 * 1024 * 1024;

    private CameraImageDecoder() {
    }

    public static Bitmap decodeBase64(String encodedImage) {
        if (encodedImage == null || encodedImage.trim().isEmpty()) {
            return null;
        }

        String value = encodedImage.trim();

        if (value.startsWith("data:")) {
            int separator = value.indexOf(',');

            if (separator < 0) {
                return null;
            }

            value = value.substring(separator + 1);
        }

        if (value.length() > MAX_ENCODED_LENGTH) {
            return null;
        }

        try {
            byte[] imageBytes = Base64.decode(
                    value,
                    Base64.DEFAULT);

            if (imageBytes.length == 0) {
                return null;
            }

            BitmapFactory.Options options =
                    new BitmapFactory.Options();

            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.length,
                    options);

        } catch (IllegalArgumentException exception) {
            return null;
        } catch (OutOfMemoryError error) {
            return null;
        }
    }
}