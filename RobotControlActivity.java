package com.aura.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class RobotControlActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;

    private TextView status;
    private Button forward;
    private Button backward;
    private Button left;
    private Button right;
    private Button stop;

    private boolean safeSessionConfirmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        setControlsEnabled(false);
        refreshStatus();
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
        text.setPadding(dp(6), dp(8), dp(6), dp(8));
        return text;
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(BG);
        button.setTextSize(16);
        button.setTypeface(null, Typeface.BOLD);
        button.setAllCaps(false);
        button.setBackgroundColor(CYAN);
        return button;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(20));
        root.setBackgroundColor(BG);

        TextView title = makeText("AURA ROBOT CONTROL", 23, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        status = makeText("Checking Raspberry Pi...", 14, WHITE);
        status.setBackgroundColor(PANEL);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        TextView warning = makeText(
                "Safety mode: movement stays disabled until a verified "
                        + "Raspberry Pi control endpoint is implemented.",
                14, WHITE);
        root.addView(warning);

        forward = makeButton("▲  FORWARD");
        backward = makeButton("▼  BACKWARD");
        left = makeButton("◀  LEFT");
        right = makeButton("RIGHT  ▶");
        stop = makeButton("■  STOP");
        stop.setBackgroundColor(Color.rgb(255, 100, 100));

        root.addView(forward, buttonParams());
        
        LinearLayout middle = new LinearLayout(this);
        middle.setGravity(Gravity.CENTER);
        middle.setOrientation(LinearLayout.HORIZONTAL);
        middle.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        middle.addView(right, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(middle);

        root.addView(backward, buttonParams());
        root.addView(stop, buttonParams());

        forward.setOnClickListener(v -> sendCommand("forward"));
        backward.setOnClickListener(v -> sendCommand("backward"));
        left.setOnClickListener(v -> sendCommand("left"));
        right.setOnClickListener(v -> sendCommand("right"));
        stop.setOnClickListener(v -> sendCommand("stop"));

        Button back = makeButton("← Back to Home");
        back.setOnClickListener(v -> finish());
        root.addView(back, buttonParams());

        setContentView(root);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(5), 0, dp(5));
        return params;
    }

    private void setControlsEnabled(boolean enabled) {
        if (forward == null) return;

        forward.setEnabled(enabled);
        backward.setEnabled(enabled);
        left.setEnabled(enabled);
        right.setEnabled(enabled);
        stop.setEnabled(enabled);

        forward.setAlpha(enabled ? 1f : 0.45f);
        backward.setAlpha(enabled ? 1f : 0.45f);
        left.setAlpha(enabled ? 1f : 0.45f);
        right.setAlpha(enabled ? 1f : 0.45f);
        stop.setAlpha(enabled ? 1f : 0.45f);
    }

    private void refreshStatus() {
        PiApiClient.health(this, (success, code, body) -> {
            if (success) {
                status.setText(
                        "Raspberry Pi reachable. "
                                + "Motor control remains disabled "
                                + "until hardware safety is verified.");
            } else {
                status.setText(
                        "Raspberry Pi not connected. "
                                + "Motor controls are disabled.");
            }

            // Health alone is not proof that motor control is safe.
            safeSessionConfirmed = false;
            setControlsEnabled(false);
        });
    }

    private void sendCommand(String command) {
        if (!safeSessionConfirmed) {
            Toast.makeText(this,
                    "Motor control is locked for safety.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        HistoryStore.addMotorCommand(this, command);

        PiApiClient.motorCommand(this, command,
                (success, code, body) -> {
                    if (!success) {
                        safeSessionConfirmed = false;
                        setControlsEnabled(false);
                        status.setText(
                                "Control request failed. Controls disabled.");
                        HistoryStore.addError(this,
                                "Motor request failed: " + body);
                        return;
                    }

                    status.setText("Pi response: " + body);
                });
    }
}