package com.aura.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CameraImageStore {

    private static final String DIRECTORY = "camera_snapshots";

    private CameraImageStore() {
    }

    public static File save(
            Context context,
            Bitmap bitmap) throws IOException {

        if (context == null || bitmap == null) {
            throw new IllegalArgumentException(
                    "Context and image are required.");
        }

        File directory = new File(
                context.getFilesDir(), DIRECTORY);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException(
                    "Could not create snapshot directory.");
        }

        String timestamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss_SSS",
                Locale.US).format(new Date());

        File destination = new File(
                directory, "AURA_" + timestamp + ".jpg");

        try (FileOutputStream output =
                     new FileOutputStream(destination)) {

            boolean compressed = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    92,
                    output);

            output.flush();

            if (!compressed) {
                destination.delete();
                throw new IOException(
                        "Could not encode the snapshot.");
            }

        } catch (IOException exception) {
            destination.delete();
            throw exception;
        }

        HistoryStore.addImage(
                context,
                "Snapshot saved privately: " + destination.getName());

        return destination;
    }

    public static File[] list(Context context) {
        if (context == null) {
            return new File[0];
        }

        File directory = new File(
                context.getFilesDir(), DIRECTORY);

        File[] files = directory.listFiles(
                file -> file.isFile()
                        && file.getName().toLowerCase(Locale.ROOT)
                        .endsWith(".jpg"));

        return files == null ? new File[0] : files;
    }

    public static Bitmap load(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    public static boolean delete(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        return file.delete();
    }
}