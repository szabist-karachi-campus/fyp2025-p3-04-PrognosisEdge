package com.example.prognosisedge;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prognosisedge.models.EngineersResponse;
import com.example.prognosisedge.models.MaintenanceHistoryResponse;
import com.example.prognosisedge.models.MaintenanceRecord;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;
import com.example.prognosisedge.models.PredictionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SE_MaintenanceHistory extends Fragment {

    private RecyclerView recyclerView;
    private MaintenanceHistoryAdapter adapter;
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();
    private Button openFilterButton;
    private Map<String, List<String>> filters = new HashMap<>();
    private boolean engineersFetched = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.se__maintenancehistoryfragment, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.maintenance_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Filter Button
        openFilterButton = rootView.findViewById(R.id.open_filter_button);
        openFilterButton.setOnClickListener(v -> openFilterBottomSheet());

        // Set up static filters
        filters.put("Status", Arrays.asList("Completed", "Cancelled"));
        filters.put("Machine Type", Arrays.asList("Tablet Coating Machine", "Sterilization Autoclave", "Ampoule Washing Machine", "Computer Numerical Control"));

        // Fetch engineers for filters
        fetchEngineersForFilters();

        // Fetch initial maintenance records
        fetchMaintenanceRecords(null, null, null);

        return rootView;
    }

    private void fetchMaintenanceRecords(String machine, String status, String engineer) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        apiService.fetchMaintenanceHistory(machine, status, engineer).enqueue(new Callback<MaintenanceHistoryResponse>() {
            @Override
            public void onResponse(Call<MaintenanceHistoryResponse> call, Response<MaintenanceHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    maintenanceRecords = response.body().getData();
                    if (maintenanceRecords.isEmpty()) {
                        Toast.makeText(getContext(), "No maintenance history available.", Toast.LENGTH_SHORT).show();
                    }
                    adapter = new MaintenanceHistoryAdapter(maintenanceRecords, record -> showMaintenanceRecordDetailsDialog(record));
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Failed to fetch maintenance records.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MaintenanceHistoryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Failed to fetch engineers.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EngineersResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFilterBottomSheet() {
        if (!engineersFetched) {
            Toast.makeText(getContext(), "Fetching engineers, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create FilterBottomSheet
        FilterBottomSheet bottomSheet = new FilterBottomSheet(filters);
        bottomSheet.setFilterListener(selectedFilters -> {
            Log.d("SelectedFilters", selectedFilters.toString());
            fetchMaintenanceRecords(
                    selectedFilters.get("Machine Type"),
                    selectedFilters.get("Status"),
                    selectedFilters.get("Engineers")
            );
        });
        bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
    }

    private void showMaintenanceRecordDetailsDialog(MaintenanceRecord record) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.se__detailed_maintenance_record, null);

        // Populate dialog views
        ((TextView) dialogView.findViewById(R.id.machine_name)).setText(record.getMachineName());
        ((TextView) dialogView.findViewById(R.id.status)).setText(record.getStatus());
        ((TextView) dialogView.findViewById(R.id.mh_engineer)).setText(record.getAssignedEngineer());
        ((TextView) dialogView.findViewById(R.id.work_order_assigned_date)).setText(formatDate(record.getScheduledAt()));
        ((TextView) dialogView.findViewById(R.id.work_order_started_date)).setText(formatDate(record.getStartedAt()));
        ((TextView) dialogView.findViewById(R.id.work_order_ended_date)).setText(formatDate(record.getEndedAt()));
        ((TextView) dialogView.findViewById(R.id.mh_notes)).setText(record.getNotes());

        TextView commentsView = dialogView.findViewById(R.id.mh_comments);
        commentsView.setText(record.getComments());
        commentsView.setFocusable(false);
        commentsView.setClickable(false);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_backgroud);
        }
        dialog.show();
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}
