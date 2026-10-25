
package com.aura.ai;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class RobotNotificationManager {

    private static final String CHANNEL_ID = "aura_robot_events";
    private static final String CHANNEL_NAME = "AURA Robot Events";

    private static final int NOTIFICATION_ID = 7201;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 7202;

    private RobotNotificationManager() {
    }

    public static void createChannel(Context context) {
        if (context == null
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);

        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        channel.setDescription(
                "Connection, battery, and robot system notifications."
        );

        manager.createNotificationChannel(channel);
    }

    public static boolean hasPermission(Context context) {
        if (context == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT < 33) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        if (activity == null
                || Build.VERSION.SDK_INT < 33
                || hasPermission(activity)) {
            return;
        }

        activity.requestPermissions(
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_NOTIFICATION_PERMISSION
        );
    }

    public static void show(
            Context context,
            String title,
            String message) {

        if (context == null) {
            return;
        }

        createChannel(context);

        if (!hasPermission(context)) {
            return;
        }

        String safeTitle = title == null ? "AURA AI" : title;
        String safeMessage = message == null ? "" : message;

        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(safeTitle)
                        .setContentText(safeMessage)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(safeMessage)
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build();

        try {
            NotificationManagerCompat.from(context).notify(
                    NOTIFICATION_ID,
                    notification
            );
        } catch (SecurityException ignored) {
            // Permission may have changed while the notification was built.
        }
    }

    public static void robotConnected(Context context, String deviceName) {
        String name = deviceName == null || deviceName.trim().isEmpty()
                ? "Raspberry Pi"
                : deviceName.trim();

        show(context, "AURA AI Connected", name + " is connected.");
    }

    public static void robotDisconnected(Context context) {
        show(context, "AURA AI Disconnected",
                "The robot connection was interrupted.");
    }

    public static void batteryUpdate(Context context, int percentage) {
        if (percentage < 0 || percentage > 100) {
            return;
        }

        show(context, "AURA Battery",
                "Reported battery level: " + percentage + "%");
    }

    public static void systemAlert(Context context, String message) {
        show(context, "AURA System Alert", message);
    }
}
