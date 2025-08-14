package com.example.prognosisedge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 1-second delay
    private boolean wifiChecked = false;
    private boolean wifiDialogShown = false;

    private static final String PREF_NAME = "PrognosisEdgePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        ImageView splashLogo = findViewById(R.id.splash_logo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        splashLogo.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            if (!isWifiActive()) {
                showWifiAlertDialog();
            } else {
                navigateToNextActivity();
            }
        }, SPLASH_DELAY);
    }

    private boolean isWifiActive() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    private void showWifiAlertDialog() {
        if (wifiDialogShown) return;
        wifiDialogShown = true;

        new AlertDialog.Builder(this)
                .setTitle("No WiFi Connection")
                .setMessage("WiFi is not active. Please enable WiFi to use the app.")
                .setCancelable(false)
                .setPositiveButton("Settings", (dialog, which) -> {
                    wifiDialogShown = false;
                    startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    wifiDialogShown = false;
                    finish();
                })
                .show();
    }

    private void navigateToNextActivity() {
        if (!wifiChecked) {
            wifiChecked = true;

            Intent serviceIntent = new Intent(this, PredictionBroadcastReceiver.class);
            ContextCompat.startForegroundService(this, serviceIntent);

            // Check login state
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

            Intent intent;
            if (isLoggedIn) {
                // Get the user role
                String userRole = sharedPreferences.getString("userRole", "");

                // Navigate to the appropriate dashboard based on role
                switch (userRole.toLowerCase()) {
                    case "system supervisor":
                        intent = new Intent(SplashScreenActivity.this, SystemSupervisorActivity.class);
                        break;
                    case "user administrator":
                        intent = new Intent(SplashScreenActivity.this, UserAdministratorActivity.class);
                        break;
                    case "service engineer":
                        intent = new Intent(SplashScreenActivity.this, ServiceEngineerActivity.class);
                        break;
                    default:
                        Toast.makeText(this, "Invalid role. Contact admin.", Toast.LENGTH_SHORT).show();
                        intent = new Intent(SplashScreenActivity.this, LoginActivity.class); // Redirect to login
                        break;
                }
            } else {
                // Navigate to Login if not logged in
                intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wifiChecked) return;

        new Handler().postDelayed(() -> {
            if (isWifiActive()) {
                navigateToNextActivity();
            } else if (!wifiDialogShown) {
                showWifiAlertDialog();
            }
        }, 500);
    }
}
