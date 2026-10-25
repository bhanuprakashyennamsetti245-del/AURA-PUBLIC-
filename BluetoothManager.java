
package com.aura.ai;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BluetoothManager {

    public interface Listener {
        void onConnected(String deviceName);
        void onDisconnected(String reason);
        void onMessage(String message);
        void onError(String error);
    }

    private static final UUID SPP_UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB"
    );

    private final BluetoothAdapter adapter;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    // Separate executors prevent the blocking reader from blocking writes.
    private final ExecutorService connectionExecutor =
            Executors.newSingleThreadExecutor();

    private final ExecutorService writeExecutor =
            Executors.newSingleThreadExecutor();

    private volatile BluetoothSocket socket;
    private volatile BufferedReader reader;
    private volatile BufferedWriter writer;
    private volatile boolean running;
    private volatile long connectionGeneration;

    private volatile Listener listener;

    public BluetoothManager(Context context) {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void postError(String message) {
        mainHandler.post(() -> {
            Listener current = listener;
            if (current != null) {
                current.onError(message);
            }
        });
    }

    private void postDisconnected(String message) {
        mainHandler.post(() -> {
            Listener current = listener;
            if (current != null) {
                current.onDisconnected(message);
            }
        });
    }

    private void postConnected(String name) {
        mainHandler.post(() -> {
            Listener current = listener;
            if (current != null) {
                current.onConnected(name);
            }
        });
    }

    private void postMessage(String message) {
        mainHandler.post(() -> {
            Listener current = listener;
            if (current != null) {
                current.onMessage(message);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void connectToPairedDevice(String nameOrAddress) {
        if (adapter == null) {
            postError("This phone does not support Bluetooth.");
            return;
        }

        if (nameOrAddress == null || nameOrAddress.trim().isEmpty()) {
            postError("Enter the paired Raspberry Pi name or address.");
            return;
        }

        try {
            if (!adapter.isEnabled()) {
                postError("Enable Bluetooth on your phone first.");
                return;
            }
        } catch (SecurityException e) {
            postError("Allow Nearby Devices permission in Android settings.");
            return;
        }

        final String targetValue = nameOrAddress.trim();
        final long generation = ++connectionGeneration;

        closeConnection(false);

        connectionExecutor.execute(() -> {
            BluetoothSocket newSocket = null;

            try {
                BluetoothDevice target = null;
                Set<BluetoothDevice> paired = adapter.getBondedDevices();

                for (BluetoothDevice device : paired) {
                    String name = device.getName();
                    String address = device.getAddress();

                    if (targetValue.equalsIgnoreCase(address)
                            || (name != null
                            && targetValue.equalsIgnoreCase(name))) {
                        target = device;
                        break;
                    }
                }

                if (target == null) {
                    postError(
                            "Device not found. Pair the Raspberry Pi "
                                    + "in Android Bluetooth settings first."
                    );
                    return;
                }

                newSocket =
                        target.createRfcommSocketToServiceRecord(SPP_UUID);

                newSocket.connect();

                if (generation != connectionGeneration) {
                    newSocket.close();
                    return;
                }

                BufferedReader newReader = new BufferedReader(
                        new InputStreamReader(
                                newSocket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                BufferedWriter newWriter = new BufferedWriter(
                        new OutputStreamWriter(
                                newSocket.getOutputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                socket = newSocket;
                reader = newReader;
                writer = newWriter;
                running = true;

                String connectedName = target.getName() == null
                        ? target.getAddress()
                        : target.getName();

                postConnected(connectedName);

                readLoop(generation, newReader);

            } catch (SecurityException e) {
                closeSocketQuietly(newSocket);
                if (generation == connectionGeneration) {
                    postError(
                            "Bluetooth permission missing. "
                                    + "Allow Nearby Devices permission."
                    );
                }
            } catch (IOException | IllegalArgumentException e) {
                closeSocketQuietly(newSocket);

                if (generation == connectionGeneration) {
                    postError(
                            "Bluetooth connection failed. Check pairing "
                                    + "and the Raspberry Pi SPP server."
                    );
                }
            } finally {
                if (generation == connectionGeneration) {
                    closeConnection(false);
                }
            }
        });
    }

    private void readLoop(long generation, BufferedReader activeReader) {
        try {
            String line;

            while (running
                    && generation == connectionGeneration
                    && (line = activeReader.readLine()) != null) {
                postMessage(line);
            }

            if (generation == connectionGeneration && running) {
                postDisconnected("Raspberry Pi closed the Bluetooth connection.");
            }

        } catch (IOException e) {
            if (generation == connectionGeneration && running) {
                postDisconnected("Bluetooth connection was interrupted.");
            }
        }
    }

    public void sendJson(JSONObject packet) {
        if (packet == null) {
            postError("Cannot send an empty Bluetooth message.");
            return;
        }

        final long generation = connectionGeneration;
        final String payload = packet.toString();

        writeExecutor.execute(() -> {
            BufferedWriter activeWriter = writer;

            if (!running
                    || activeWriter == null
                    || generation != connectionGeneration) {
                postError("Bluetooth is not connected.");
                return;
            }

            try {
                activeWriter.write(payload);
                activeWriter.newLine();
                activeWriter.flush();

            } catch (IOException e) {
                if (generation == connectionGeneration) {
                    postError("Bluetooth send failed.");
                    postDisconnected("Bluetooth connection was interrupted.");
                    closeConnection(true);
                }
            }
        });
    }

    public void sendChat(String text, String language) {
        if (text == null || text.trim().isEmpty()) {
            postError("Enter a message before sending.");
            return;
        }

        try {
            JSONObject packet = new JSONObject();
            packet.put("type", "chat");
            packet.put("text", text);
            packet.put("language", "te".equals(language) ? "te" : "en");
            sendJson(packet);

        } catch (Exception e) {
            postError("Could not prepare the chat message.");
        }
    }

    public void sendCommand(String command) {
        if (!"forward".equals(command)
                && !"backward".equals(command)
                && !"left".equals(command)
                && !"right".equals(command)
                && !"stop".equals(command)) {
            postError("Invalid robot command.");
            return;
        }

        try {
            JSONObject packet = new JSONObject();
            packet.put("type", "command");
            packet.put("command", command);
            sendJson(packet);

        } catch (Exception e) {
            postError("Could not prepare the robot command.");
        }
    }

    public void requestCameraSnapshot() {
        try {
            JSONObject packet = new JSONObject();
            packet.put("type", "camera_snapshot");
            sendJson(packet);

        } catch (Exception e) {
            postError("Could not prepare the camera request.");
        }
    }

    public boolean isConnected() {
        BluetoothSocket current = socket;

        try {
            return running
                    && current != null
                    && current.isConnected();
        } catch (SecurityException e) {
            return false;
        }
    }

    public void disconnect() {
        connectionGeneration++;
        closeConnection(true);
    }

    private synchronized void closeConnection(boolean closeSocket) {
        running = false;

        BufferedReader oldReader = reader;
        BufferedWriter oldWriter = writer;
        BluetoothSocket oldSocket = socket;

        reader = null;
        writer = null;
        socket = null;

        try {
            if (oldReader != null) {
                oldReader.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (oldWriter != null) {
                oldWriter.close();
            }
        } catch (IOException ignored) {
        }

        if (closeSocket) {
            closeSocketQuietly(oldSocket);
        } else if (oldSocket != null) {
            closeSocketQuietly(oldSocket);
        }
    }

    private static void closeSocketQuietly(BluetoothSocket target) {
        if (target == null) {
            return;
        }

        try {
            target.close();
        } catch (IOException ignored) {
        }
    }
}
