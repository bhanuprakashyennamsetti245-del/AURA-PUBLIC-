
package com.aura.ai;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CameraStreamActivity extends Activity {

    private static final int BG = Color.rgb(5, 12, 26);
    private static final int CYAN = Color.rgb(25, 221, 247);
    private static final int WHITE = Color.WHITE;

    private WebView webView;
    private TextView status;
    private String streamUrl = "";

    private int mainFrameErrors = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        streamUrl = getIntent().getStringExtra("stream_url");

        if (streamUrl == null || streamUrl.trim().isEmpty()) {
            streamUrl = CameraConfig.getStreamUrl(this);
        }

        buildUi();

        String validation =
                CameraStreamValidator.validateOrError(streamUrl);

        if (validation != null) {
            status.setText(validation);
            return;
        }

        openStream();
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

    @SuppressLint("SetJavaScriptEnabled")
    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10), dp(8), dp(10), dp(10));
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("AURA · LIVE CAMERA");
        title.setTextColor(CYAN);
        title.setTextSize(21);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        status = new TextView(this);
        status.setTextColor(WHITE);
        status.setTextSize(13);
        status.setText("Preparing camera stream...");
        status.setPadding(dp(4), dp(8), dp(4), dp(8));
        root.addView(status);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);

        // JavaScript is disabled because a basic camera stream
        // should not require running arbitrary web-page scripts.
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setDomStorageEnabled(false);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view, String url, android.graphics.Bitmap favicon) {

                if (url != null
                        && CameraStreamValidator.isValidHttpUrl(url)) {
                    status.setText("Connecting to camera stream...");
                }
            }

            @Override
            public void onPageFinished(
                    WebView view, String url) {

                if (url != null && url.equals(streamUrl)) {
                    status.setText(
                            "Stream page loaded. "
                                    + "Video rendering depends on the "
                                    + "Pi's stream format."
                    );
                }
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {

                if (request.isForMainFrame()) {
                    mainFrameErrors++;
                    status.setText(
                            "Camera page failed to load. Check the Pi "
                                    + "address, endpoint and network."
                    );
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                // Keep navigation inside the configured stream only.
                String requested = request.getUrl().toString();
                return !requested.equals(streamUrl);
            }
        });

        root.addView(webView,
                new LinearLayout.LayoutParams(-1, 0, 1));

        Button reload = makeButton("Reload Stream");
        reload.setOnClickListener(v -> openStream());
        root.addView(reload);

        Button back = makeButton("← Back to Camera");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(root);
    }

    private void openStream() {
        if (webView == null) {
            return;
        }

        String validation =
                CameraStreamValidator.validateOrError(streamUrl);

        if (validation != null) {
            status.setText(validation);
            return;
        }

        mainFrameErrors = 0;
        status.setText("Opening: " + streamUrl);
        webView.loadUrl(streamUrl);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
