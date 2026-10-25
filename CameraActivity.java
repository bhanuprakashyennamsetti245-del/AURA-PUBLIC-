
package com.aura.ai;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class CameraActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;
    private static final int PANEL = Color.rgb(15, 29, 49);

    private ImageView preview;
    private EditText urlInput;
    private TextView status;

    private BluetoothManager bluetooth;
    private boolean waitingForSnapshot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetooth = new BluetoothManager(this);

        bluetooth.setListener(new BluetoothManager.Listener() {
            @Override
            public void onConnected(String name) {
                updateStatus("Bluetooth connected: " + name);
                RobotNotificationManager.robotConnected(
                        CameraActivity.this, name);
            }

            @Override
            public void onDisconnected(String reason) {
                waitingForSnapshot = false;
                updateStatus(reason);
                RobotNotificationManager.robotDisconnected(
                        CameraActivity.this);
            }

            @Override
            public void onMessage(String message) {
                handleBluetoothMessage(message);
            }

            @Override
            public void onError(String error) {
                waitingForSnapshot = false;
                updateStatus(error);
                showToast(error);
            }
        });

        buildUi();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(BG);
        button.setBackgroundColor(CYAN);
        return button;
    }

    private void updateStatus(String value) {
        runOnUiThread(() -> {
            if (status != null) {
                status.setText(value);
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() ->
                Toast.makeText(
                        CameraActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), dp(12));
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("AURA LIVE CAMERA");
        title.setTextSize(22);
        title.setTextColor(CYAN);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        status = new TextView(this);
        status.setText("Camera not connected.");
        status.setTextSize(13);
        status.setTextColor(WHITE);
        status.setPadding(0, dp(5), 0, dp(5));
        root.addView(status);

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setHint("http://192.168.1.9:8000/camera/stream");
        urlInput.setTextColor(WHITE);
        urlInput.setHintTextColor(Color.LTGRAY);
        urlInput.setText(CameraConfig.getStreamUrl(this));
        urlInput.setBackgroundColor(PANEL);
        root.addView(urlInput);

        preview = new ImageView(this);
        preview.setBackgroundColor(Color.BLACK);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setAdjustViewBounds(true);

        ScrollView previewScroll = new ScrollView(this);
        previewScroll.setFillViewport(true);
        previewScroll.setBackgroundColor(Color.BLACK);
        previewScroll.addView(preview,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));

        LinearLayout.LayoutParams previewParams =
                new LinearLayout.LayoutParams(-1, 0, 1);
        previewParams.setMargins(0, dp(8), 0, dp(8));
        root.addView(previewScroll, previewParams);

        Button openStream = makeButton("Open Wi-Fi Camera Stream");
        openStream.setOnClickListener(v -> openStream());
        root.addView(openStream);

        Button snapshot = makeButton("Request Bluetooth Snapshot");
        snapshot.setOnClickListener(v -> requestBluetoothSnapshot());
        root.addView(snapshot);

        Button connectBluetooth = makeButton("Connect Bluetooth");
        connectBluetooth.setOnClickListener(v -> connectBluetooth());
        root.addView(connectBluetooth);

        Button gallery = makeButton("Open Saved Snapshots");
        gallery.setOnClickListener(v ->
                startActivity(new android.content.Intent(
                        CameraActivity.this,
                        CameraGalleryActivity.class
                )));
        root.addView(gallery);

        Button back = makeButton("← Back to Home");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(root);
    }

    private void openStream() {
        String url = urlInput.getText().toString().trim();
        String validation =
                CameraStreamValidator.validateOrError(url);

        if (validation != null) {
            showToast(validation);
            return;
        }

        if (!CameraConfig.saveStreamUrl(this, url)) {
            showToast("Could not save the camera URL.");
            return;
        }

        status.setText(
                "Opening stream. If it does not appear, check "
                        + "the Pi camera endpoint and stream format."
        );

        // A WebView is used here because common MJPEG camera streams
        // are not necessarily playable directly by ImageView.
        android.content.Intent intent =
                new android.content.Intent(this, CameraStreamActivity.class);
        intent.putExtra("stream_url", url);

        try {
            startActivity(intent);
            HistoryStore.addConnectionEvent(
                    this, "Camera stream opened: " + url);
        } catch (Exception e) {
            status.setText(
                    "Camera stream screen is not available yet.");
            HistoryStore.addError(
                    this, "Unable to open camera stream: " + e.getMessage());
        }
    }

    private void connectBluetooth() {
        String address = getIntent().getStringExtra(
                "bluetooth_device_address");

        if (address == null || address.trim().isEmpty()) {
            updateStatus(
                    "Pair the Raspberry Pi in Android Bluetooth settings. "
                            + "Then configure the paired device address "
                            + "for this screen."
            );
            return;
        }

        bluetooth.connectToPairedDevice(address);
    }

    private void requestBluetoothSnapshot() {
        if (!bluetooth.isConnected()) {
            showToast(
                    "Connect Bluetooth to the Raspberry Pi first.");
            return;
        }

        if (waitingForSnapshot) {
            showToast("A snapshot request is already in progress.");
            return;
        }

        waitingForSnapshot = true;
        status.setText("Requesting snapshot over Bluetooth...");

        bluetooth.requestCameraSnapshot();
    }

    private void handleBluetoothMessage(String raw) {
        try {
            JSONObject response = new JSONObject(raw);
            String type = response.optString("type", "");

            if ("camera_snapshot".equals(type)) {
                String imageData =
                        response.optString("image_base64", "");

                if (imageData.isEmpty()) {
                    waitingForSnapshot = false;
                    status.setText(
                            "Pi replied without image data. "
                                    + response.optString("message",
                                    "Check the Pi camera implementation.")
                    );
                    return;
                }

                Bitmap bitmap =
                        CameraImageDecoder.decode(imageData);

                if (bitmap == null) {
                    waitingForSnapshot = false;
                    status.setText(
                            "Received data was not a valid supported image.");
                    HistoryStore.addError(
                            this, "Camera snapshot image decoding failed");
                    return;
                }

                CameraImageStore.SavedImage saved =
                        CameraImageStore.save(this, bitmap);

                waitingForSnapshot = false;

                if (saved == null) {
                    status.setText(
                            "Image received but could not be saved.");
                    return;
                }

                preview.setImageBitmap(bitmap);
                status.setText(
                        "Snapshot received and saved locally: "
                                + saved.getFileName()
                );

                HistoryStore.addImage(
                        this,
                        "Bluetooth snapshot saved: "
                                + saved.getFileName()
                );

            } else {
                status.setText(
                        response.optString("message", raw));
            }

        } catch (Exception e) {
            waitingForSnapshot = false;
            status.setText("Could not process the snapshot response.");
            HistoryStore.addError(
                    this, "Snapshot response processing failed: "
                            + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        waitingForSnapshot = false;

        if (preview != null) {
            preview.setImageDrawable(null);
        }

        if (bluetooth != null) {
            bluetooth.disconnect();
        }

        super.onDestroy();
    }
}
