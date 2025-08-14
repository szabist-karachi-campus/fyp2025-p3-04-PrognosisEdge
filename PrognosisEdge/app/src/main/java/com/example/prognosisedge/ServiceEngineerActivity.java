package com.example.prognosisedge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ServiceEngineerActivity extends AppCompatActivity {

    private static final String TAG = "ServiceEngineerActivity";
    private BroadcastReceiver globalPredictionReceiver;

    // Shared notification list for SE portal
    public static List<AppNotification> seNotificationList = new ArrayList<>();
    public static NotificationAdapter seAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serviceengineer);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Set default fragment to Home and set Home as selected
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SE_DashboardFragment()).commit();

        // Initialize notification list with stored notifications
        seNotificationList.clear();
        seNotificationList.addAll(NotificationStorage.loadNotifications(this));

        // Start WebSocket service for real-time predictions
        Intent serviceIntent = new Intent(this, PredictionBroadcastReceiver.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        // Register broadcast receiver for notifications
        registerGlobalPredictionReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (globalPredictionReceiver != null) {
            unregisterReceiver(globalPredictionReceiver);
        }
    }

    // Add notification to the list and update UI
    public void addInAppNotification(AppNotification notification) {
        seNotificationList.add(0, notification); // Add at top

        // Save to SharedPreferences
        NotificationStorage.saveNotifications(this, seNotificationList);

        // Update adapter if available
        if (seAdapter != null) {
            seAdapter.notifyDataSetChanged();
        }
    }

    // Register broadcast receiver for WebSocket predictions
    private void registerGlobalPredictionReceiver() {
        globalPredictionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("SE_Broadcast", "Received broadcast from WebSocket");

                if ("com.example.prognosisedge.NEW_PREDICTION".equals(intent.getAction())) {
                    String title = intent.getStringExtra("title");
                    String message = intent.getStringExtra("message");
                    String machineId = intent.getStringExtra("machine_id");
                    String machineName = intent.getStringExtra("machine_name");
                    String failureType = intent.getStringExtra("failure_type");
                    boolean machineFailure = intent.getBooleanExtra("machine_failure", false);

                    // Create proper notification based on failure status
                    String category;
                    String notificationTitle;
                    String notificationDescription;

                    if (!machineFailure || "No Failure".equals(failureType)) {
                        // No failure - positive notification
                        category = "System Status";
                        notificationTitle = "Machine Operating Normally";
                        notificationDescription = (machineName != null ? machineName : "Machine " + machineId) +
                                "\nStatus: All systems functioning properly" +
                                "\nMachine ID: " + machineId;
                    } else {
                        // Failure detected - alert notification
                        category = "Machine Alert";
                        notificationTitle = "Maintenance Required";
                        notificationDescription = (machineName != null ? machineName : "Machine " + machineId) +
                                "\nIssue: " + failureType +
                                "\nMachine ID: " + machineId +
                                "\nAction: Service required";
                    }

                    // Add to in-app notifications
                    addInAppNotification(new AppNotification(
                            category,
                            notificationTitle,
                            notificationDescription
                    ));

                    // Update notification fragment if it's currently active
                    if (SE_NotificationFragment.currentInstance != null) {
                        SE_NotificationFragment.currentInstance.notifyDataChanged();
                    }

                    // Show system notification with appropriate message
                    showPredictionNotification(title, message);

                    // Refresh dashboard if it's currently active
                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (current instanceof SE_DashboardFragment) {
                        ((SE_DashboardFragment) current).refreshDataFromBroadcast();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.example.prognosisedge.NEW_PREDICTION");
        registerReceiver(globalPredictionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    // Show system-level push notification
    private void showPredictionNotification(String title, String message) {
        String channelId = "se_prediction_channel";
        String channelName = "SE Machine Alerts";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts for Service Engineers about machine predictions");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, ServiceEngineerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2001, builder.build()); // Different ID from SS notifications
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.nav_maintenance) {
                        selectedFragment = new SE_MaintenanceFragment();
                        Log.d(TAG, "Maintenance selected");
                    } else if (item.getItemId() == R.id.nav_work_order) {
                        selectedFragment = new SE_WorkOrderFragment();
                        Log.d(TAG, "Work Orders selected");
                    } else if (item.getItemId() == R.id.nav_home) {
                        selectedFragment = new SE_DashboardFragment();
                        Log.d(TAG, "Home selected");
                    } else if (item.getItemId() == R.id.nav_reports) {
                        selectedFragment = new SE_ReportsFragment();
                        Log.d(TAG, "Reports selected");
                    } else if (item.getItemId() == R.id.nav_notifications) {
                        selectedFragment = new SE_NotificationFragment();
                        Log.d(TAG, "Notifications selected");
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                        return true; // Indicate that the item selection is handled successfully
                    }

                    return false; // No valid item was selected, item state is not changed
                }
            };
}