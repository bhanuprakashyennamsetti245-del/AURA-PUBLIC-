package com.aura.ai;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;

    private Switch voiceSwitch;
    private Switch motorConfirmSwitch;
    private Switch lockConfirmSwitch;
    private Button languageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        loadSettings();
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

    private Switch makeSwitch(String label) {
        Switch control = new Switch(this);
        control.setText(label);
        control.setTextSize(15);
        control.setTextColor(WHITE);
        control.setPadding(dp(8), dp(10), dp(8), dp(10));
        return control;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(20));
        scroll.addView(root);

        TextView title = makeText("AURA APP SETTINGS", 23, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView languageLabel = makeText(
                "Assistant language", 16, WHITE);
        root.addView(languageLabel);

        languageButton = makeButton("Language");
        languageButton.setOnClickListener(v -> toggleLanguage());
        root.addView(languageButton, buttonParams());

        voiceSwitch = makeSwitch("Speak replies aloud");
        root.addView(voiceSwitch);

        motorConfirmSwitch = makeSwitch(
                "Confirm motor commands");
        root.addView(motorConfirmSwitch);

        lockConfirmSwitch = makeSwitch(
                "Confirm lock actions");
        root.addView(lockConfirmSwitch);

        TextView note = makeText(
                "Safety note: these preferences do not enable motor or "
                        + "solenoid control. The Raspberry Pi must implement "
                        + "and verify its own safe command handling.",
                13, WHITE);
        note.setBackgroundColor(PANEL);
        root.addView(note, buttonParams());

        Button save = makeButton("Save Settings");
        save.setOnClickListener(v -> saveSettings());
        root.addView(save, buttonParams());

        Button back = makeButton("← Back to Home");
        back.setOnClickListener(v -> finish());
        root.addView(back, buttonParams());

        setContentView(scroll);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(6), 0, dp(6));
        return params;
    }

    private void loadSettings() {
        voiceSwitch.setChecked(
                AppSettings.isVoiceEnabled(this));

        motorConfirmSwitch.setChecked(
                AppSettings.shouldConfirmMotorCommands(this));

        lockConfirmSwitch.setChecked(
                AppSettings.shouldConfirmLockActions(this));

        updateLanguageLabel();
    }

    private void toggleLanguage() {
        String current = AppSettings.getLanguage(this);
        AppSettings.setLanguage(
                this, "en".equals(current) ? "te" : "en");
        updateLanguageLabel();
    }

    private void updateLanguageLabel() {
        languageButton.setText(
                "en".equals(AppSettings.getLanguage(this))
                        ? "English · Tap to switch to Telugu"
                        : "తెలుగు · Tap to switch to English");
    }

    private void saveSettings() {
        AppSettings.setVoiceEnabled(
                this, voiceSwitch.isChecked());

        AppSettings.setConfirmMotorCommands(
                this, motorConfirmSwitch.isChecked());

        AppSettings.setConfirmLockActions(
                this, lockConfirmSwitch.isChecked());

        Toast.makeText(
                this,
                "AURA settings saved.",
                Toast.LENGTH_SHORT).show();

        HistoryStore.addConnectionEvent(
                this, "Application settings updated.");
    }
}