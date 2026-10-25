package com.aura.ai;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraGalleryActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int PANEL = Color.rgb(15, 29, 49);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(170, 190, 210);

    private LinearLayout gallery;
    private TextView count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        loadGallery();
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
        text.setPadding(dp(6), dp(6), dp(6), dp(6));
        return text;
    }

    private Button makeButton(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(BG);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setBackgroundColor(CYAN);
        return button;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(10), dp(14), dp(14));
        root.setBackgroundColor(BG);

        TextView title = makeText("AURA SNAPSHOT GALLERY", 22, CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        count = makeText("Saved snapshots: 0", 13, MUTED);
        root.addView(count);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);

        gallery = new LinearLayout(this);
        gallery.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(gallery);

        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1));

        Button refresh = makeButton("Refresh Gallery");
        refresh.setOnClickListener(v -> loadGallery());
        root.addView(refresh);

        Button back = makeButton("← Back");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(root);
    }

    private void loadGallery() {
        if (gallery == null) return;

        gallery.removeAllViews();

        File[] files = CameraImageStore.list(this);
        count.setText("Saved snapshots: " + files.length);

        if (files.length == 0) {
            gallery.addView(makeText(
                    "No snapshots saved yet. Capture an image from "
                            + "the camera screen to see it here.",
                    15, WHITE));
            return;
        }

        for (int i = files.length - 1; i >= 0; i--) {
            addImageCard(files[i]);
        }
    }

    private void addImageCard(File file) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackgroundColor(PANEL);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, dp(5), 0, dp(5));

        TextView heading = makeText(
                file.getName(), 14, CYAN);
        heading.setTypeface(null, Typeface.BOLD);
        card.addView(heading);

        TextView timestamp = makeText(
                new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm:ss a",
                        Locale.getDefault())
                        .format(new Date(file.lastModified())),
                12, MUTED);
        card.addView(timestamp);

        Bitmap bitmap = CameraImageStore.load(file);

        if (bitmap != null) {
            ImageView image = new ImageView(this);
            image.setImageBitmap(bitmap);
            image.setAdjustViewBounds(true);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);

            LinearLayout.LayoutParams imageParams =
                    new LinearLayout.LayoutParams(-1, dp(240));
            imageParams.setMargins(0, dp(6), 0, dp(6));
            card.addView(image, imageParams);

            image.setOnClickListener(v -> showFullImage(file));
        } else {
            card.addView(makeText(
                    "This image could not be decoded.",
                    13, WHITE));
        }

        Button delete = makeButton("Delete Snapshot");
        delete.setOnClickListener(v -> confirmDelete(file));
        card.addView(delete);

        gallery.addView(card, cardParams);
    }

    private void showFullImage(File file) {
        Bitmap bitmap = CameraImageStore.load(file);

        if (bitmap == null) {
            Toast.makeText(this, "Could not open this image.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.Dialog dialog = new android.app.Dialog(this);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(10), dp(10), dp(10), dp(10));
        container.setBackgroundColor(BG);

        ImageView image = new ImageView(this);
        image.setImageBitmap(bitmap);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);

        container.addView(image, new LinearLayout.LayoutParams(
                -1, -1, 1));

        Button close = makeButton("Close");
        close.setOnClickListener(v -> dialog.dismiss());
        container.addView(close);

        dialog.setContentView(container);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    -1, -1);
        }
    }

    private void confirmDelete(File file) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete snapshot?")
                .setMessage(
                        "This will permanently delete "
                                + file.getName() + ".")
                .setNegativeButton("Cancel",
                        (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete",
                        (dialog, which) -> {
                            if (CameraImageStore.delete(file)) {
                                HistoryStore.addImage(
                                        this,
                                        "Snapshot deleted: "
                                                + file.getName());

                                Toast.makeText(
                                        this,
                                        "Snapshot deleted.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(
                                        this,
                                        "Could not delete snapshot.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            loadGallery();
                        })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGallery();
    }
}