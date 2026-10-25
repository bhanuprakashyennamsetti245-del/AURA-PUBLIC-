package com.aura.ai;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class NotificationPermissionHelper {

    public static final int REQUEST_NOTIFICATIONS = 7103;

    private NotificationPermissionHelper() {
    }

    public static boolean isGranted(Activity activity) {
        if (activity == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void request(Activity activity) {
        if (activity == null || isGranted(activity)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    REQUEST_NOTIFICATIONS);
        }
    }
}