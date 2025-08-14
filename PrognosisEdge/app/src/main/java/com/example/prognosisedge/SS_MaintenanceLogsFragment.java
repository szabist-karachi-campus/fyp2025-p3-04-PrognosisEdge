package com.example.prognosisedge;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prognosisedge.models.EngineersResponse;
import com.example.prognosisedge.models.MaintenanceHistoryResponse;
import com.example.prognosisedge.models.MaintenanceRecord;
import com.example.prognosisedge.models.UpdateCommentRequest;
import com.example.prognosisedge.models.UpdateCommentResponse;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import androidx.appcompat.app.AlertDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.widget.EditText;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SS_MaintenanceLogsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MaintenanceHistoryAdapter adapter;
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();
    private Button filterButton;
    private Map<String, List<String>> filters = new HashMap<>();
    private boolean engineersFetched = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ss__maintenancelogsfragment, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Filter Button
//        filterButton = rootView.findViewById(R.id.open_filter_button);
//        filterButton.setOnClickListener(v -> openFilterBottomSheet());

        // Set up filters
        setupFilters();

        // Fetch dynamic engineers list
        fetchEngineersForFilters();

        // Fetch initial maintenance logs
        fetchMaintenanceLogs(null, null, null);

        return rootView;
    }

    private void setupFilters() {
        // All statuses
        filters.put("Status", Arrays.asList("Upcoming", "In Progress", "Completed", "Overdue", "Cancelled"));
        // All machine types
        filters.put("Machine Type", Arrays.asList("Tablet Coating Machine", "Sterilization Autoclave", "Ampoule Washing Machine", "Computer Numerical Control"));
    }

    private void fetchMaintenanceLogs(String status, String engineer, String machineType) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);

        // Call the new `/fetch_logs` endpoint
        apiService.fetchMaintenanceLogs(machineType, status, engineer).enqueue(new Callback<MaintenanceHistoryResponse>() {
            @Override
            public void onResponse(Call<MaintenanceHistoryResponse> call, Response<MaintenanceHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    maintenanceRecords = response.body().getData();
                    if (maintenanceRecords.isEmpty()) {
                        Toast.makeText(requireContext(), "No logs are available for the selected criteria.", Toast.LENGTH_SHORT).show();
                    }
                    adapter = new MaintenanceHistoryAdapter(maintenanceRecords, record -> showMaintenanceRecordDetailsDialog(record));
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch maintenance logs.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MaintenanceHistoryResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEngineersForFilters() {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        apiService.fetchEngineers().enqueue(new Callback<EngineersResponse>() {
            @Override
            public void onResponse(Call<EngineersResponse> call, Response<EngineersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filters.put("Engineers", response.body().getData());
                    engineersFetched = true;
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch engineers.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EngineersResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    private void showMaintenanceRecordDetailsDialog(MaintenanceRecord record) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.se__detailed_maintenance_record, null);

        // Populate views
        ((TextView) dialogView.findViewById(R.id.machine_name)).setText(record.getMachineName());
        ((TextView) dialogView.findViewById(R.id.status)).setText(record.getStatus());
        ((TextView) dialogView.findViewById(R.id.mh_engineer)).setText(record.getAssignedEngineer());
        ((TextView) dialogView.findViewById(R.id.work_order_assigned_date)).setText(formatDate(record.getScheduledAt()));
        ((TextView) dialogView.findViewById(R.id.work_order_started_date)).setText(formatDate(record.getStartedAt()));
        ((TextView) dialogView.findViewById(R.id.work_order_ended_date)).setText(formatDate(record.getEndedAt()));
        ((TextView) dialogView.findViewById(R.id.mh_notes)).setText(record.getNotes());

        EditText commentsEditText = dialogView.findViewById(R.id.mh_comments);
        Button saveButton = dialogView.findViewById(R.id.save_comments_button);

        // Always editable since this is supervisor-only
        commentsEditText.setText(record.getComments());
        commentsEditText.setEnabled(true);
        commentsEditText.setFocusableInTouchMode(true);
        saveButton.setVisibility(View.VISIBLE);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        saveButton.setOnClickListener(v -> {
            String updatedComment = commentsEditText.getText().toString().trim();
            if (updatedComment.isEmpty()) {
                commentsEditText.setError("Comment cannot be empty.");
                return;
            }

            ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
            apiService.updateComment(new UpdateCommentRequest(record.getTaskId(), updatedComment))
                    .enqueue(new Callback<UpdateCommentResponse>() {
                        @Override
                        public void onResponse(Call<UpdateCommentResponse> call, Response<UpdateCommentResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(requireContext(), "Comment saved!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss(); // close dialog
                                fetchMaintenanceLogs(null, null, null); //  Refresh the list
                            } else {
                                Toast.makeText(requireContext(), "Failed to save comment.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UpdateCommentResponse> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.setOnShowListener(dlg -> {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        });

        dialog.show();
    }


    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}
