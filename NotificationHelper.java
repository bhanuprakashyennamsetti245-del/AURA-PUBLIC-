package com.aura.ai;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class NotificationHelper {

    private static final String CHANNEL_ID = "aura_robot_events";
    private static final String CHANNEL_NAME = "AURA Robot Events";

    private NotificationHelper() {
    }

    public static void createChannel(Context context) {
        if (context == null
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.setDescription(
                "Robot connection and status notifications.");

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        if (context == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void show(
            Context context,
            String title,
            String message) {

        if (context == null) {
            return;
        }

        createChannel(context);

        if (!hasNotificationPermission(context)) {
            return;
        }

        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(
                                title == null ? "AURA AI" : title)
                        .setContentText(
                                message == null ? "" : message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message == null ? "" : message))
                        .setPriority(
                                NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build();

        try {
            NotificationManagerCompat.from(context)
                    .notify(nextNotificationId(), notification);
        } catch (SecurityException ignored) {
            // Notification permission may have changed.
        }
    }

    public static void robotConnected(Context context) {
        show(context, "AURA AI", "Raspberry Pi connected.");
    }

    public static void robotDisconnected(Context context) {
        show(context, "AURA AI", "Raspberry Pi disconnected.");
    }

    public static void robotStarted(Context context) {
        show(context, "AURA AI", "Robot start status reported.");
    }

    public static void robotStopped(Context context) {
        show(context, "AURA AI", "Robot stop status reported.");
    }

    private static int nextNotificationId() {
        return (int) (System.currentTimeMillis() & 0x7fffffff);
    }
}