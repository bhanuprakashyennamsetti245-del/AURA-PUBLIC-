
package com.aura.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(170, 190, 210);

    private LinearLayout content;
    private TextView wifiStatus;
    private TextView bluetoothStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        RobotNotificationManager.createChannel(this);
        requestNotificationPermissionIfNeeded();

        buildHome();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(
            int color, int radius) {

        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(radius));
        return background;
    }

    private TextView makeText(
            String value, int size, int color) {

        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setPadding(dp(5), dp(6), dp(5), dp(6));
        return text;
    }

    private void buildHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(17), dp(12), dp(17), dp(24));

        scroll.addView(root);

        TextView brand = makeText("A U R A   A I", 29, CYAN);
        brand.setGravity(Gravity.CENTER);
        brand.setTypeface(null, Typeface.BOLD);
        root.addView(brand);

        TextView subtitle = makeText(
                "YOUR SMART ROBOT COMPANION",
                11, WHITE);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        TextView tagline = makeText(
                "Intelligent assistance. Connected robotics.",
                13, MUTED);
        tagline.setGravity(Gravity.CENTER);
        root.addView(tagline);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(14), 0, 0);
        root.addView(content);

        addCard(
                "SYSTEM OVERVIEW",
                "AURA AI app is running. Connection and hardware states "
                        + "will be shown only when verified."
        );

        wifiStatus = addCard(
                "RASPBERRY PI · WI-FI",
                "Address: " + configuredAddress()
        );

        bluetoothStatus = addCard(
                "BLUETOOTH",
                "Connection state: "
                        + ConnectionStatusStore
                        .getBluetoothState(this)
                        .getDisplayName()
        );

        addCard(
                "LIVE CAMERA",
                "Open the camera screen to configure the Pi stream "
                        + "or request a Bluetooth snapshot."
        );

        addCard(
                "BATTERY",
                "Battery percentage is unavailable until a real "
                        + "monitor reports it."
        );

        addCard(
                "SECURITY",
                "Phone biometric verification is available when "
                        + "supported. Physical lock control remains disabled."
        );

        addSectionHeading("AURA FEATURES");

        addMenuButton(
                "01  AI CHAT",
                "English and Telugu · Wi-Fi or Bluetooth",
                ChatActivity.class
        );

        addMenuButton(
                "02  LIVE CAMERA",
                "Pi stream and snapshot requests",
                CameraActivity.class
        );

        addMenuButton(
                "03  CAMERA GALLERY",
                "View snapshots saved inside this app",
                CameraGalleryActivity.class
        );

        addMenuButton(
                "04  ROBOT CONTROLS",
                "Movement remains locked until safety is verified",
                RobotControlActivity.class
        );

        addMenuButton(
                "05  FINGERPRINT SECURITY",
                "Verify your phone identity",
                FingerprintLockActivity.class
        );

        addMenuButton(
                "06  SYSTEM STATUS",
                "Check the Pi API and reported status",
                SystemStatusActivity.class
        );

        addMenuButton(
                "07  CONNECTION SETTINGS",
                "Configure Pi IP, API port and camera URL",
                ConnectionSettingsActivity.class
        );

        addMenuButton(
                "08  AURA HISTORY",
                "Chat, image, connection, lock and error events",
                HistoryActivity.class
        );

        addMenuButton(
                "09  APP SETTINGS",
                "Language, voice and safety preferences",
                SettingsActivity.class
        );

        Button refresh = makePrimaryButton("Refresh Connection Status");
        refresh.setOnClickListener(v -> refreshConnectionStatus());
        root.addView(refresh, buttonParams());

        TextView footer = makeText(
                "AURA AI · Raspberry Pi 5",
                11, MUTED);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(16), 0, dp(8));
        root.addView(footer);

        setContentView(scroll);
        refreshConnectionStatus();
    }

    private TextView addCard(String heading, String description) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(13), dp(9), dp(13), dp(9));
        card.setBackground(roundedBackground(PANEL, 15));

        TextView title = makeText(heading, 14, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView detail = makeText(description, 13, WHITE);
        card.addView(detail);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(5));

        content.addView(card, params);
        return detail;
    }

    private void addSectionHeading(String heading) {
        TextView title = makeText(heading, 17, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(dp(3), dp(17), dp(3), dp(7));
        content.addView(title);
    }

    private void addMenuButton(
            String title,
            String description,
            Class<?> activityClass) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(12), dp(9), dp(12), dp(9));
        item.setBackground(roundedBackground(PANEL, 14));

        TextView heading = makeText(title, 15, CYAN);
        heading.setTypeface(null, Typeface.BOLD);
        item.addView(heading);

        item.addView(makeText(description, 12, WHITE));

        item.setClickable(true);
        item.setFocusable(true);
        item.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, activityClass));
            } catch (Exception e) {
                RobotNotificationManager.systemAlert(
                        MainActivity.this,
                        "Could not open " + title
                );
            }
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(4));

        content.addView(item, params);
    }

    private Button makePrimaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(BG);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackground(roundedBackground(CYAN, 14));
        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(10), 0, dp(5));
        return params;
    }

    private String configuredAddress() {
        String ip = ConnectionConfig.getPiIp(this);

        if (ip == null || ip.trim().isEmpty()) {
            return "Not configured";
        }

        return ip + ":" + ConnectionConfig.getPiPort(this);
    }

    private void refreshConnectionStatus() {
        if (wifiStatus != null) {
            wifiStatus.setText("Address: " + configuredAddress()
                    + "\nChecking Raspberry Pi API...");
        }

        PiApiClient.health(this, (success, code, body) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            ConnectionStatusStore.updateWifi(
                    this,
                    success
                            ? ConnectionState.CONNECTED
                            : ConnectionState.ERROR,
                    success
                            ? "Raspberry Pi API reachable"
                            : "Raspberry Pi API unavailable"
            );

            if (wifiStatus != null) {
                wifiStatus.setText(
                        "Address: " + configuredAddress()
                                + "\nAPI: "
                                + (success ? "Reachable" : "Not reachable")
                );
            }

            if (bluetoothStatus != null) {
                bluetoothStatus.setText(
                        "Last known state: "
                                + ConnectionStatusStore
                                .getBluetoothState(this)
                                .getDisplayName()
                                + "\nBluetooth is checked separately."
                );
            }

            if (!success) {
                HistoryStore.addConnectionEvent(
                        this,
                        "Home screen Pi API check failed: " + body
                );
            }
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            RobotNotificationManager.requestPermission(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (content != null) {
            refreshConnectionStatus();
        }
    }
}
