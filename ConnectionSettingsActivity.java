package com.aura.ai;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionSettingsActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;
    private static final int PANEL = Color.rgb(15, 29, 49);

    private EditText ipInput;
    private EditText portInput;
    private EditText cameraInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String value) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(CYAN);
        text.setTextSize(15);
        text.setPadding(0, dp(12), 0, dp(5));
        return text;
    }

    private EditText field(String hint, String value) {
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setHint(hint);
        edit.setText(value);
        edit.setTextColor(WHITE);
        edit.setHintTextColor(Color.LTGRAY);
        edit.setPadding(dp(12), dp(8), dp(12), dp(8));
        edit.setBackgroundColor(PANEL);
        return edit;
    }

    private Button button(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(BG);
        button.setAllCaps(false);
        button.setBackgroundColor(CYAN);
        return button;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(24));
        scroll.addView(root);

        TextView title = label("AURA CONNECTION SETTINGS");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        root.addView(label("Raspberry Pi IPv4 address"));
        ipInput = field("Example: 192.168.1.9",
                ConnectionConfig.getPiIp(this));
        root.addView(ipInput);

        root.addView(label("API port"));
        portInput = field("8000",
                String.valueOf(ConnectionConfig.getPiPort(this)));
        portInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER);
        root.addView(portInput);

        root.addView(label("Camera stream URL (optional)"));
        cameraInput = field("http://192.168.1.9:8000/camera/stream",
                CameraConfig.getStreamUrl(this));
        root.addView(cameraInput);

        Button save = button("Save Settings");
        save.setOnClickListener(v -> saveSettings());
        root.addView(save, buttonParams());

        Button test = button("Test Raspberry Pi Connection");
        test.setOnClickListener(v -> testConnection());
        root.addView(test, buttonParams());

        Button back = button("← Back to Home");
        back.setOnClickListener(v -> finish());
        root.addView(back, buttonParams());

        setContentView(scroll);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(8), 0, dp(4));
        return params;
    }

    private boolean saveSettings() {
        String ip = ipInput.getText().toString().trim();
        String portText = portInput.getText().toString().trim();
        String cameraUrl = cameraInput.getText().toString().trim();

        if (ip.isEmpty()) {
            Toast.makeText(this, "Enter the Raspberry Pi IP address.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        int port;

        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid API port.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (!ConnectionConfig.savePiIp(this, ip)) {
            Toast.makeText(this, "Invalid IPv4 address.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (!ConnectionConfig.savePiPort(this, port)) {
            Toast.makeText(this, "Port must be between 1 and 65535.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (!CameraConfig.saveStreamUrl(this, cameraUrl)) {
            Toast.makeText(this,
                    "Camera URL must start with http:// or https:// "
                            + "and contain no spaces.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        Toast.makeText(this, "Settings saved.",
                Toast.LENGTH_SHORT).show();

        return true;
    }

    private void testConnection() {
        if (!saveSettings()) return;

        PiApiClient.health(this, (success, code, body) -> {
            Toast.makeText(this,
                    success
                            ? "Raspberry Pi API connected."
                            : "Connection failed: " + body,
                    Toast.LENGTH_LONG).show();

            HistoryStore.addConnectionEvent(this,
                    success ? "Manual Pi connection test succeeded"
                            : "Manual Pi connection test failed: " + body);
        });
    }
}