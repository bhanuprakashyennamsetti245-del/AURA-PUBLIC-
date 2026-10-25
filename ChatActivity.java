package com.aura.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;

public class ChatActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;

    private LinearLayout messages;
    private EditText input;
    private Button languageButton;
    private String language = "en";
    private BluetoothManager bluetooth;
    private boolean waitingForReply = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        language = ConnectionConfig.getLanguage(this);
        bluetooth = new BluetoothManager(this);

        bluetooth.setListener(new BluetoothManager.Listener() {
            @Override
            public void onConnected(String name) {
                addMessage("Bluetooth connected: " + name, false);
            }

            @Override
            public void onDisconnected(String reason) {
                addMessage(reason, false);
            }

            @Override
            public void onMessage(String message) {
                handleBluetoothMessage(message);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, error,
                        Toast.LENGTH_LONG).show();
            }
        });

        buildUi();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private TextView makeText(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(dp(4), dp(5), dp(4), dp(5));
        return view;
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(BG);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setBackgroundColor(CYAN);
        return button;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(12), dp(8), dp(12), dp(10));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = makeText("AURA AI CHAT", 21, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(
                0, -2, 1));

        languageButton = makeButton(
                "en".equals(language) ? "English" : "తెలుగు");
        languageButton.setOnClickListener(v -> toggleLanguage());
        header.addView(languageButton);

        root.addView(header);

        TextView subtitle = makeText(
                "Talk to AURA · Raspberry Pi connection",
                12, WHITE);
        root.addView(subtitle);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, dp(8), 0, dp(8));

        scroll.addView(messages);

        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button wifiButton = makeButton("Wi-Fi");
        wifiButton.setOnClickListener(v -> testWifi());
        actions.addView(wifiButton, new LinearLayout.LayoutParams(
                0, -2, 1));

        Button bluetoothButton = makeButton("Bluetooth");
        bluetoothButton.setOnClickListener(v -> connectBluetooth());
        actions.addView(bluetoothButton,
                new LinearLayout.LayoutParams(0, -2, 1));

        root.addView(actions);

        LinearLayout composer = new LinearLayout(this);
        composer.setOrientation(LinearLayout.HORIZONTAL);
        composer.setGravity(Gravity.CENTER_VERTICAL);

        input = new EditText(this);
        input.setSingleLine(false);
        input.setMinLines(1);
        input.setMaxLines(4);
        input.setHint("Type your message...");
        input.setTextColor(WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setBackgroundColor(PANEL);
        input.setPadding(dp(10), dp(8), dp(10), dp(8));
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);

        composer.addView(input, new LinearLayout.LayoutParams(
                0, -2, 1));

        Button send = makeButton("Send");
        send.setOnClickListener(v -> sendMessage());
        composer.addView(send);

        root.addView(composer);

        Button back = makeButton("← Back to Home");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(root);

        addMessage("AURA AI is ready. Connect to your Raspberry Pi to chat.",
                false);

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void toggleLanguage() {
        language = "en".equals(language) ? "te" : "en";
        ConnectionConfig.saveLanguage(this, language);

        languageButton.setText(
                "en".equals(language) ? "English" : "తెలుగు");

        input.setHint("en".equals(language)
                ? "Type your message..." : "మీ సందేశాన్ని టైప్ చేయండి...");

        addMessage("Language changed to "
                + ("en".equals(language) ? "English" : "Telugu"), false);
    }

    private void addMessage(String message, boolean fromUser) {
        TextView bubble = makeText(
                (fromUser ? "YOU\n" : "AURA AI\n") + message,
                15, WHITE);

        bubble.setBackgroundColor(fromUser
                ? Color.rgb(12, 91, 112) : PANEL);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1, -2);

        params.setMargins(0, dp(4), 0, dp(4));

        messages.addView(bubble, params);

        HistoryStore.addChat(this, message, fromUser);
    }

    private void sendMessage() {
        String message = input.getText().toString().trim();

        if (message.isEmpty()) return;

        if (waitingForReply) {
            Toast.makeText(this, "Please wait for the reply.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        addMessage(message, true);
        input.setText("");

        ((InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(input.getWindowToken(), 0);

        waitingForReply = true;

        if (bluetooth.isConnected()) {
            bluetooth.sendChat(message, language);
        } else {
            PiApiClient.chat(this, message, language,
                    (success, status, body) -> {
                        waitingForReply = false;

                        if (!success) {
                            addMessage("Could not reach Raspberry Pi. "
                                    + "Check the Pi IP, port and Wi-Fi connection. "
                                    + body, false);
                            return;
                        }

                        try {
                            JSONObject response = new JSONObject(body);
                            String reply = response.optString(
                                    "reply",
                                    response.optString(
                                            "response", body));
                            addMessage(reply, false);
                        } catch (Exception e) {
                            addMessage(body, false);
                        }
                    });
        }
    }

    private void handleBluetoothMessage(String raw) {
        waitingForReply = false;

        try {
            JSONObject response = new JSONObject(raw);
            String reply = response.optString(
                    "reply",
                    response.optString(
                            "response",
                            response.optString("message", raw)));
            addMessage(reply, false);
        } catch (Exception e) {
            addMessage(raw, false);
        }
    }

    private void testWifi() {
        PiApiClient.health(this, (success, status, body) -> {
            addMessage(success
                    ? "Raspberry Pi Wi-Fi API is reachable: " + body
                    : "Wi-Fi connection failed: " + body, false);
        });
    }

    private void connectBluetooth() {
        String address = getIntent().getStringExtra(
                "bluetooth_device_address");

        if (address == null || address.trim().isEmpty()) {
            addMessage("Pair your Raspberry Pi in Android Bluetooth settings, "
                    + "then launch chat with its paired device name or address.",
                    false);
            return;
        }

        bluetooth.connectToPairedDevice(address);
    }

    @Override
    protected void onDestroy() {
        if (bluetooth != null) {
            bluetooth.disconnect();
        }
        super.onDestroy();
    }
}