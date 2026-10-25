package com.aura.ai;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class AndroidPermissions {

    public static final int REQUEST_BLUETOOTH = 7101;
    public static final int REQUEST_MICROPHONE = 7102;

    private AndroidPermissions() {
    }

    public static boolean hasBluetoothPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestBluetoothPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || hasBluetoothPermission(activity)) {
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT
                },
                REQUEST_BLUETOOTH);
    }

    public static boolean hasMicrophonePermission(Activity activity) {
        return ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestMicrophonePermission(Activity activity) {
        if (hasMicrophonePermission(activity)) {
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.RECORD_AUDIO
                },
                REQUEST_MICROPHONE);
    }
}