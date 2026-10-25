package com.aura.ai;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public final class CameraSnapshotHandler {

    public interface Callback {
        void onSaved(File imageFile);
        void onError(String message);
    }

    private CameraSnapshotHandler() {
    }

    public static void handle(
            Context context,
            String rawJson,
            Callback callback) {

        if (context == null || rawJson == null
                || rawJson.trim().isEmpty()) {
            reportError(callback, "Snapshot response is empty.");
            return;
        }

        final JSONObject response;

        try {
            response = new JSONObject(rawJson);
        } catch (Exception exception) {
            reportError(callback,
                    "The Pi returned an invalid JSON response.");
            return;
        }

        String type = response.optString("type", "");

        if (!"camera_snapshot".equals(type)) {
            reportError(callback,
                    response.optString(
                            "message",
                            "This response is not a camera snapshot."));
            return;
        }

        String encodedImage = response.optString(
                "image_base64", "");

        if (encodedImage.isEmpty()) {
            reportError(callback,
                    response.optString(
                            "error",
                            "The Pi response contains no image data."));
            return;
        }

        Bitmap bitmap =
                CameraImageDecoder.decodeBase64(encodedImage);

        if (bitmap == null) {
            reportError(callback,
                    "Could not decode the received snapshot.");
            return;
        }

        try {
            File saved = CameraImageStore.save(context, bitmap);
            reportSaved(callback, saved);
        } catch (IOException | IllegalArgumentException exception) {
            reportError(callback,
                    "Could not save snapshot: "
                            + (exception.getMessage() == null
                            ? "storage error"
                            : exception.getMessage()));
        } finally {
            bitmap.recycle();
        }
    }

    private static void reportSaved(
            Callback callback, File file) {
        if (callback != null) {
            callback.onSaved(file);
        }
    }

    private static void reportError(
            Callback callback, String message) {
        if (callback != null) {
            callback.onError(message);
        }
    }
}