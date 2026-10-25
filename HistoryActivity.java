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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class HistoryActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(170, 190, 210);

    private LinearLayout eventsContainer;
    private String selectedCategory = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        loadHistory();
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
        text.setPadding(dp(8), dp(6), dp(8), dp(6));
        return text;
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(BG);
        button.setBackgroundColor(CYAN);
        return button;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), dp(12));
        root.setBackgroundColor(BG);

        TextView title = makeText("AURA HISTORY", 23, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        ScrollView filterScroll = new ScrollView(this);
        filterScroll.setFillViewport(false);

        LinearLayout filters = new LinearLayout(this);
        filters.setOrientation(LinearLayout.HORIZONTAL);

        String[][] options = {
                {"All", "all"},
                {"Chat", "chat"},
                {"Images", "image"},
                {"Motors", "motor"},
                {"Lock", "lock"},
                {"Connections", "connection"},
                {"Errors", "error"}
        };

        for (String[] option : options) {
            Button filter = makeButton(option[0]);
            filter.setOnClickListener(v -> {
                selectedCategory = option[1];
                loadHistory();
            });

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-2, -2);
            params.setMargins(dp(3), dp(4), dp(3), dp(4));
            filters.addView(filter, params);
        }

        filterScroll.addView(filters);
        root.addView(filterScroll,
                new LinearLayout.LayoutParams(-1, -2));

        ScrollView eventScroll = new ScrollView(this);
        eventScroll.setFillViewport(false);

        eventsContainer = new LinearLayout(this);
        eventsContainer.setOrientation(LinearLayout.VERTICAL);

        eventScroll.addView(eventsContainer);

        root.addView(eventScroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        Button clear = makeButton("Clear All History");
        clear.setOnClickListener(v -> clearHistory());
        root.addView(clear);

        Button back = makeButton("← Back to Home");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(root);
    }

    private void loadHistory() {
        if (eventsContainer == null) return;

        eventsContainer.removeAllViews();

        JSONArray all = HistoryStore.getAll(this);
        int shown = 0;

        for (int i = 0; i < all.length(); i++) {
            JSONObject event = all.optJSONObject(i);
            if (event == null) continue;

            String category = event.optString("category", "unknown");

            if (!"all".equals(selectedCategory)
                    && !selectedCategory.equals(category)) {
                continue;
            }

            String time = event.optString("time", "");
            String description = event.optString("description", "");

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(10), dp(8), dp(10), dp(8));
            card.setBackgroundColor(PANEL);

            TextView heading = makeText(
                    category.toUpperCase() + "  ·  " + time,
                    12, CYAN);
            heading.setTypeface(null, Typeface.BOLD);

            TextView body = makeText(description, 15, WHITE);
            TextView note = makeText(
                    "Saved locally on this phone",
                    10, MUTED);

            card.addView(heading);
            card.addView(body);
            card.addView(note);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(0, dp(4), 0, dp(4));

            eventsContainer.addView(card, params);
            shown++;
        }

        if (shown == 0) {
            TextView empty = makeText(
                    "No history entries in this category yet.",
                    15, WHITE);
            eventsContainer.addView(empty);
        }
    }

    private void clearHistory() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Clear AURA history?")
                .setMessage(
                        "This permanently clears locally saved history "
                                + "from this app.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Clear", (dialog, which) -> {
                    HistoryStore.clear(this);
                    loadHistory();

                    Toast.makeText(this, "History cleared.",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }
}