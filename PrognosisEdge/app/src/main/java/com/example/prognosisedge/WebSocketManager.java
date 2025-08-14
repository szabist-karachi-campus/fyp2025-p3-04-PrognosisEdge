package com.example.prognosisedge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.AppNotification;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;

public class WebSocketManager {
    private static WebSocketManager instance;
    private Context context;
    private Socket socket;
    private final String SOCKET_URL = "https://172.16.158.222:5000"; // Flask-SocketIO server
    private final String CHANNEL_ID = "prediction_updates";

    private WebSocketManager(Context context) {
        this.context = context.getApplicationContext();
        createNotificationChannel();
        connectSocket();
    }

    public static synchronized WebSocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketManager(context);
        }
        return instance;
    }

    private void connectSocket() {
        try {
            IO.Options options = new IO.Options();
            options.secure = true;
            options.reconnection = true;
            options.forceNew = true;

            // Inject unsafe OkHttpClient to bypass SSL cert issues
            OkHttpClient unsafeClient = ApiClient.getUnsafeOkHttpClient().build();
            options.callFactory = unsafeClient;
            options.webSocketFactory = unsafeClient;

            socket = IO.socket(SOCKET_URL, options);
            socket.connect();

            Log.d("WebSocket", "Connecting to: " + SOCKET_URL);

            socket.on(Socket.EVENT_CONNECT, args -> Log.d("SocketIO", "Connected to server"));
            socket.on("new_prediction", onPredictionReceived);
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.d("SocketIO", "Disconnected from server"));
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e("SocketIO", "Connection error", (Throwable) args[0]));

        } catch (URISyntaxException e) {
            Log.e("SocketIO", "URI Error: " + e.getMessage());
        }
    }

    //on prediction
    private final Emitter.Listener onPredictionReceived = args -> {
        Log.d("WebSocketDebug", "📡 Received: " + args[0].toString());

        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                String failureType = data.optString("failure_type", "No Failure");
                String machineId = data.optString("machine_id", "N/A");
                String machineName = data.optString("machine_name", "Machine " + machineId);
                boolean machineFailure = data.optBoolean("machine_failure", false);

                // FIXED NOTIFICATION LOGIC
                String title;
                String message;
                String category;
                String description;

                if (!machineFailure || "No Failure".equals(failureType)) {
                    // No failure case - positive message
                    category = "System Status";
                    title = "Operating Normally";
                    message = machineName + " → All systems normal";
                    description = machineName + " is functioning properly with no issues detected.";
                } else {
                    // Actual failure case - alert message
                    category = "Machine Alert";
                    title = "Issue Detected";
                    message = machineName + " → " + failureType;
                    description = machineName + " requires attention: " + failureType;
                }

                Log.d("WebSocketDebug", "Sending notification: " + title + " - " + message);

                // Create notification object with proper messaging
                AppNotification notification = new AppNotification(
                        category,
                        title,
                        description
                );

                // Add to SS notification list
                SS_NotificationFragment.notificationList.add(0, notification); // Add at top

                // Keep only last 50 notifications
                if (SS_NotificationFragment.notificationList.size() > 50) {
                    SS_NotificationFragment.notificationList.remove(SS_NotificationFragment.notificationList.size() - 1);
                }

                NotificationStorage.saveNotifications(context, SS_NotificationFragment.notificationList);

                // Add to SE notification list
                ServiceEngineerActivity.seNotificationList.add(0, notification); // Add at top

                // Keep only last 50 notifications
                if (ServiceEngineerActivity.seNotificationList.size() > 50) {
                    ServiceEngineerActivity.seNotificationList.remove(ServiceEngineerActivity.seNotificationList.size() - 1);
                }

                NotificationStorage.saveNotifications(context, ServiceEngineerActivity.seNotificationList);

                // Update SS fragment if active
                if (SS_NotificationFragment.currentInstance != null) {
                    SS_NotificationFragment.currentInstance.requireActivity().runOnUiThread(() -> {
                        SS_NotificationFragment.currentInstance.notifyDataChanged();
                    });
                }

                // Update SE fragment if active
                if (SE_NotificationFragment.currentInstance != null) {
                    SE_NotificationFragment.currentInstance.requireActivity().runOnUiThread(() -> {
                        SE_NotificationFragment.currentInstance.notifyDataChanged();
                    });
                }

                // Broadcast to both SS and SE apps with proper messaging
                Intent broadcast = new Intent("com.example.prognosisedge.NEW_PREDICTION");
                broadcast.putExtra("title", title);
                broadcast.putExtra("message", message);
                broadcast.putExtra("machine_id", machineId);
                broadcast.putExtra("failure_type", failureType);
                broadcast.putExtra("machine_failure", machineFailure);
                broadcast.putExtra("machine_name", machineName);
                context.sendBroadcast(broadcast);

                // System-level push notification with proper message
                sendSystemNotification(title, message);

            } catch (Exception e) {
                Log.e("SocketIO", "Error parsing prediction: " + e.getMessage());
            }
        }
    };

    private void sendSystemNotification(String title, String message) {
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Prediction Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts when new machine predictions are made");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}