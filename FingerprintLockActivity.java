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

import androidx.fragment.app.FragmentActivity;

public class FingerprintLockActivity extends FragmentActivity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;

    private TextView status;
    private Button verifyButton;
    private boolean authenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
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

    private Button makeButton(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setAllCaps(false);
        button.setTextColor(BG);
        button.setBackgroundColor(CYAN);
        return button;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundColor(BG);

        TextView title = makeText("AURA SECURE LOCK", 24, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView fingerprint = makeText("◉", 76, CYAN);
        fingerprint.setGravity(Gravity.CENTER);
        root.addView(fingerprint);

        TextView explanation = makeText(
                "Verify your identity with the fingerprint or biometric "
                        + "authentication configured on this phone.",
                15, WHITE);
        explanation.setGravity(Gravity.CENTER);
        root.addView(explanation);

        status = makeText(
                "Security status: Not authenticated",
                15, WHITE);
        status.setBackgroundColor(PANEL);
        root.addView(status);

        verifyButton = makeButton("Verify Fingerprint");
        verifyButton.setOnClickListener(v -> verifyIdentity());
        root.addView(verifyButton, buttonParams());

        Button lockRequest = makeButton("Request Lock Action");
        lockRequest.setOnClickListener(v -> requestLockAction());
        root.addView(lockRequest, buttonParams());

        Button back = makeButton("← Back to Home");
        back.setOnClickListener(v -> finish());
        root.addView(back, buttonParams());

        setContentView(root);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(8), 0, dp(8));
        return params;
    }

    private void verifyIdentity() {
        authenticated = false;
        status.setText("Waiting for biometric verification...");

        BiometricLock.authenticate(this,
                new BiometricLock.Callback() {

                    @Override
                    public void onAuthenticated() {
                        authenticated = true;
                        status.setText(
                                "Phone identity verified. "
                                        + "Physical lock is not yet connected.");
                        HistoryStore.addLockEvent(
                                FingerprintLockActivity.this,
                                "Phone biometric verification succeeded");
                    }

                    @Override
                    public void onFailed(String message) {
                        authenticated = false;
                        status.setText("Verification failed.");
                        Toast.makeText(
                                FingerprintLockActivity.this,
                                message,
                                Toast.LENGTH_LONG).show();

                        HistoryStore.addLockEvent(
                                FingerprintLockActivity.this,
                                "Biometric verification failed: " + message);
                    }

                    @Override
                    public void onCancelled() {
                        authenticated = false;
                        status.setText("Verification cancelled.");

                        HistoryStore.addLockEvent(
                                FingerprintLockActivity.this,
                                "Biometric verification cancelled");
                    }
                });
    }

    private void requestLockAction() {
        if (!authenticated) {
            Toast.makeText(this,
                    "Verify your fingerprint first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Intentionally no physical lock request yet.
        // The Raspberry Pi must implement authenticated lock control
        // before this action can safely operate the solenoid.

        status.setText(
                "Identity verified. Lock request not sent: "
                        + "secure Raspberry Pi lock endpoint is not ready.");

        HistoryStore.addLockEvent(this,
                "Lock action blocked: secure Pi endpoint not implemented");

        Toast.makeText(this,
                "Fingerprint verified; physical lock unchanged.",
                Toast.LENGTH_LONG).show();
    }
}