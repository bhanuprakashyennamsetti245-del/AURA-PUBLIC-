package com.aura.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

public class SystemStatusActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;

    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        refresh();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private TextView makeText(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setPadding(dp(8), dp(8), dp(8), dp(8));
        return text;
    }

    private void addStatus(String heading, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(8), dp(12), dp(8));
        card.setBackgroundColor(PANEL);

        TextView title = makeText(heading, 16, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);
        card.addView(makeText(value, 14, WHITE));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(4));
        content.addView(card, params);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(20));
        scroll.addView(root);

        TextView title = makeText("AURA SYSTEM STATUS", 23, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content);

        Button refresh = new Button(this);
        refresh.setText("Refresh Status");
        refresh.setAllCaps(false);
        refresh.setOnClickListener(v -> refresh());
        root.addView(refresh);

        Button back = new Button(this);
        back.setText("← Back to Home");
        back.setAllCaps(false);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(scroll);
    }

    private void refresh() {
        content.removeAllViews();

        String ip = ConnectionConfig.getPiIp(this);
        int port = ConnectionConfig.getPiPort(this);

        addStatus("Raspberry Pi Address",
                (ip == null || ip.isEmpty())
                        ? "Not configured"
                        : ip + ":" + port);

        addStatus("Bluetooth",
                "Connection status is not currently shared with this screen.");

        addStatus("Camera",
                "Stream: " + (CameraConfig.getStreamUrl(this).isEmpty()
                        ? "Not configured"
                        : CameraConfig.getStreamUrl(this)));

        addStatus("Battery",
                "No live battery monitor data available.");

        addStatus("Physical Lock",
                "Unknown. No lock command has been sent.");

        addStatus("Motor Safety",
                "Motor control is not enabled by this status screen.");

        addStatus("Raspberry Pi API", "Checking connection...");

        PiApiClient.health(this, (success, code, body) -> {
            if (isFinishing() || isDestroyed()) return;

            addStatus("API Result",
                    success ? "Connected — " + body
                            : "Not reachable — " + body);

            HistoryStore.addConnectionEvent(this,
                    success ? "Pi API health check succeeded"
                            : "Pi API health check failed");
        });
    }
}