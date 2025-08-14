package com.example.prognosisedge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class PredictionBroadcastReceiver extends Service {
    private static final String CHANNEL_ID = "prediction_updates";  // Same as WebSocketManager

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PredictionService", "Service created");

        // Ensure notification channel exists before using it
        createNotificationChannel();

        // Create a notification immediately
        Notification notification = new NotificationCompat.Builder(this, "prediction_updates")
                .setContentTitle("Listening for updates")
                .setContentText("Monitoring machine predictions...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification); // REQUIRED for Android 14+

        WebSocketManager.getInstance(getApplicationContext()); // Start listening

        // Optional confirmation
        Toast.makeText(this, "Prediction service started", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "prediction_updates",
                    "Prediction Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts when new machine predictions are made");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Restart if killed
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
