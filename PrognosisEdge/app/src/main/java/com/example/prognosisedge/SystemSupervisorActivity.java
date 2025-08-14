package com.example.prognosisedge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SystemSupervisorActivity extends AppCompatActivity {

    private static final String TAG = "SystemSupervisorActivity";
    private BroadcastReceiver globalPredictionReceiver;

    public static List<AppNotification> ssNotificationList = new ArrayList<>();
    public static NotificationAdapter ssAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.systemsupervisor);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Set default fragment to Home and set Home as selected
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SS_DashboardFragment()).commit();

        // Start WebSocket service (must be done once)
        Intent serviceIntent = new Intent(this, PredictionBroadcastReceiver.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        // Register broadcast receiver
        registerGlobalPredictionReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (globalPredictionReceiver != null) {
            unregisterReceiver(globalPredictionReceiver);
        }
    }

    public void addInAppNotification(AppNotification notification) {
        ssNotificationList.add(0, notification); // Add at top
        if (ssAdapter != null) {
            ssAdapter.notifyDataSetChanged();
        }
    }

    private void registerGlobalPredictionReceiver() {
        globalPredictionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Broadcast", "Received broadcast from webscocket");

                if ("com.example.prognosisedge.NEW_PREDICTION".equals(intent.getAction())) {
                    String title = intent.getStringExtra("title");
                    String message = intent.getStringExtra("message");
                    String machineId = intent.getStringExtra("machine_id");
                    String machineName = intent.getStringExtra("machine_name");
                    String failureType = intent.getStringExtra("failure_type");
                    boolean machineFailure = intent.getBooleanExtra("machine_failure", false);

                    // SIMPLE FIX - Same logic as ServiceEngineer
                    String category;
                    String notificationTitle;
                    String notificationDescription;

                    if (!machineFailure || "No Failure".equals(failureType)) {
                        // No failure case - positive message
                        category = "System Status";
                        notificationTitle = "Machine Operating Normally";
                        notificationDescription = (machineName != null ? machineName : "Machine " + machineId) +
                                " is functioning properly with no issues detected." +
                                "\nMachine ID: " + machineId;
                    } else {
                        // Actual failure case - alert message
                        category = "Machine Alert";
                        notificationTitle = "Issue Detected";
                        notificationDescription = (machineName != null ? machineName : "Machine " + machineId) +
                                " requires attention: " + failureType +
                                "\nMachine ID: " + machineId;
                    }

                    addInAppNotification(new AppNotification(category, notificationTitle, notificationDescription));

                    if (SS_NotificationFragment.currentInstance != null) {
                        SS_NotificationFragment.currentInstance.notifyDataChanged();
                    }

                    // System notification
                    showPredictionNotification(title, message);

                    // Refresh dashboard safely if it's active
                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (current instanceof SS_DashboardFragment) {
                        ((SS_DashboardFragment) current).refreshDataFromBroadcast();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.example.prognosisedge.NEW_PREDICTION");
        registerReceiver(globalPredictionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void showPredictionNotification(String title, String message) {
        String channelId = "prediction_channel";
        String channelName = "Prediction Alerts";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies when new machine failure predictions arrive");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, SystemSupervisorActivity.class);
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

        notificationManager.notify(1001, builder.build());
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
    new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_machines) {
                selectedFragment = new SS_MachinesFragment();
                Log.d(TAG, "Maintenance selected");
            } else if (item.getItemId() == R.id.nav_maintenance_logs) {
                selectedFragment = new SS_MaintenanceLogsFragment();
                Log.d(TAG, "Maintenance Logs Selected");
            } else if (item.getItemId() == R.id.nav_dashboard) {
                selectedFragment = new SS_DashboardFragment();
                Log.d(TAG, "Home selected");
            } else if (item.getItemId() == R.id.nav_reports) {
                selectedFragment = new SS_ReportsFragment();
                Log.d(TAG, "Reports selected");
            } else if (item.getItemId() == R.id.nav_notifications) {
                selectedFragment = new SS_NotificationFragment();
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
