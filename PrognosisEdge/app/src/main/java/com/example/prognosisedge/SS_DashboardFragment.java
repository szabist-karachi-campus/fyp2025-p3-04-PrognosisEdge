package com.example.prognosisedge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.prognosisedge.models.PredictionResponse;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SS_DashboardFragment extends Fragment {

    private Button viewAllButton;
    private ImageView logoutButton;
    private LinearLayout machineContainer;
    private ApiService apiService;
    private TextView greetingTextView;
    private BroadcastReceiver predictionReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ss__dashboard_fragment, container, false);

        greetingTextView = rootView.findViewById(R.id.greeting_text);
        updateGreeting();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PrognosisEdgePrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "User");
        if (greetingTextView != null) {
            greetingTextView.append(", " + userName);
        }

        machineContainer = rootView.findViewById(R.id.machine_container);
        apiService = ApiClient.getRetrofitClient().create(ApiService.class);

        viewAllButton = rootView.findViewById(R.id.view_ss_logs);
        viewAllButton.setOnClickListener(v -> navigateToMaintenanceLogs());

        logoutButton = rootView.findViewById(R.id.logout);
        logoutButton.setOnClickListener(v -> handleLogout(sharedPreferences));



        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGreeting();
        fetchPredictions();

        // Register broadcast listener
        predictionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Broadcast", "Received broadcast from WebSocket");
                refreshDataFromBroadcast();
            }
        };

        ContextCompat.registerReceiver(
                requireContext(),
                predictionReceiver,
                new IntentFilter("com.example.prognosisedge.NEW_PREDICTION"),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        if (predictionReceiver != null) {
            requireContext().unregisterReceiver(predictionReceiver);
            predictionReceiver = null;
        }
    }


    private void updateGreeting() {
        if (greetingTextView == null) return;
        String greeting;
        int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date()));

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }
        greetingTextView.setText(greeting);
    }

    public void fetchPredictions() {
        machineContainer.removeAllViews();
        apiService.getAllPredictions().enqueue(new Callback<List<PredictionResponse>>() {
            @Override
            public void onResponse(Call<List<PredictionResponse>> call, Response<List<PredictionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayPredictions(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<PredictionResponse>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void refreshDataFromBroadcast() {
        Log.d("Dashboard", "Triggered UI refresh from broadcast");
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> fetchPredictions());
        }
    }


    private void displayPredictions(List<PredictionResponse> predictions) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        DecimalFormat df = new DecimalFormat("#.##");

        for (PredictionResponse prediction : predictions) {
            View itemView = inflater.inflate(R.layout.machine_layout, machineContainer, false);

            TextView machineName = itemView.findViewById(R.id.machine_name);
            TextView machinePrediction = itemView.findViewById(R.id.machine_prediction);
            TextView machineLastData = itemView.findViewById(R.id.machine_last_data);
            LinearLayout machineDetails = itemView.findViewById(R.id.machine_details);
            ImageView machineArrow = itemView.findViewById(R.id.machine_arrow);

            machineName.setText(prediction.getMachineName());
            machineLastData.setText("Failure Type: " + prediction.getFailureType() + "\nLast Data: " + new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date()));

            String status = prediction.isMachineFailure() ? "Failure" : "No Failure";
            machinePrediction.setText(status);
            setPredictionColor(machinePrediction, status);

            machineDetails.removeAllViews();

            String[] labels = new String[]{
                    "Water Flow Rate",
                    "Pressure Stability",
                    "Detergent Level",
                    "Hydraulic Pressure",
                    "Temp Fluctuation",
                    "Oil Temp",
                    "Coolant Temp"
            };

            double[] values = new double[]{
                    prediction.getWaterFlowRate(),
                    prediction.getPressureStabilityIndex(),
                    prediction.getDetergentLevel(),
                    prediction.getHydraulicPressure(),
                    prediction.getTemperatureFluctuationIndex(),
                    prediction.getHydraulicOilTemperature(),
                    prediction.getCoolantTemperature()
            };

            for (int i = 0; i < labels.length; i++) {
                TextView readingLine = new TextView(getContext());
                readingLine.setText(labels[i] + ": " + df.format(values[i]));
                readingLine.setTextSize(11);
                readingLine.setLineSpacing(4f, 1f);
                readingLine.setPadding(0, 4, 0, 4);
                machineDetails.addView(readingLine);
            }

            itemView.setOnClickListener(v -> {
                if (machineDetails.getVisibility() == View.VISIBLE) {
                    machineDetails.setVisibility(View.GONE);
                    machineArrow.setImageResource(R.drawable.arrow_down);
                } else {
                    machineDetails.setVisibility(View.VISIBLE);
                    machineArrow.setImageResource(R.drawable.arrow_up);
                }
            });

            machineContainer.addView(itemView);
        }
    }

    private void setPredictionColor(TextView predictionView, String status) {
        int color = "Failure".equals(status) ?
                ContextCompat.getColor(predictionView.getContext(), R.color.priority_high) :
                ContextCompat.getColor(predictionView.getContext(), R.color.priority_on);

        LayerDrawable layerDrawable = (LayerDrawable) predictionView.getBackground();
        GradientDrawable background = (GradientDrawable) layerDrawable.findDrawableByLayerId(android.R.id.background);
        background.setColor(color);
    }

    private void navigateToMaintenanceLogs() {
        Fragment SS_MaintenanceLogsFragment = new SS_MaintenanceLogsFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SS_MaintenanceLogsFragment)
                .addToBackStack(null)
                .commit();

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_maintenance_logs);
    }

    private void handleLogout(SharedPreferences sharedPreferences) {
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
