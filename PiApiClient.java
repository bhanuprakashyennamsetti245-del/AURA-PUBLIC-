
package com.aura.ai;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PiApiClient {

    public interface Callback {
        void onResult(boolean success, int statusCode, String body);
    }

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(2);

    private PiApiClient() {}

    private static String baseUrl(Context context) {
        String ip = ConnectionConfig.getPiIp(context);
        int port = ConnectionConfig.getPiPort(context);

        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Configure the Raspberry Pi IP address first.");
        }

        return "http://" + ip.trim() + ":" + port;
    }

    public static void health(Context context, Callback callback) {
        request(context, "/api/health", null, callback);
    }

    public static void chat(
            Context context, String message,
            String language, Callback callback) {

        try {
            JSONObject body = new JSONObject();
            body.put("text", message);
            body.put("language", language);
            request(context, "/api/chat", body, callback);
        } catch (Exception e) {
            callback.onResult(false, -1, e.getMessage());
        }
    }

    public static void motorCommand(
            Context context, String command,
            Callback callback) {

        if (!"forward".equals(command)
                && !"backward".equals(command)
                && !"left".equals(command)
                && !"right".equals(command)
                && !"stop".equals(command)) {
            callback.onResult(false, -1, "Invalid motor command");
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("command", command);
            body.put("transport", "wifi");
            request(context, "/api/motor", body, callback);
        } catch (Exception e) {
            callback.onResult(false, -1, e.getMessage());
        }
    }

    private static void request(
            Context context, String path,
            JSONObject json, Callback callback) {

        final String address;

        try {
            address = baseUrl(context) + path;
        } catch (Exception e) {
            callback.onResult(false, -1, e.getMessage());
            return;
        }

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(address).openConnection();

                connection.setConnectTimeout(4000);
                connection.setReadTimeout(6000);
                connection.setRequestMethod(
                        json == null ? "GET" : "POST");
                connection.setRequestProperty(
                        "Accept", "application/json");

                if (json != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty(
                            "Content-Type", "application/json; charset=UTF-8");

                    byte[] data = json.toString().getBytes(
                            StandardCharsets.UTF_8);

                    try (OutputStream out =
                                 connection.getOutputStream()) {
                        out.write(data);
                    }
                }

                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 400
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder response = new StringBuilder();

                if (stream != null) {
                    try (BufferedReader reader =
                                 new BufferedReader(
                                     new InputStreamReader(
                                         stream,
                                         StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                boolean success = code >= 200 && code < 300;
                String result = response.toString();

                android.os.Handler main =
                        new android.os.Handler(
                                android.os.Looper.getMainLooper());

                main.post(() ->
                        callback.onResult(success, code, result));

            } catch (Exception e) {
                String error = e.getMessage() == null
                        ? "Connection failed" : e.getMessage();

                new android.os.Handler(
                        android.os.Looper.getMainLooper())
                        .post(() ->
                                callback.onResult(false, -1, error));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
