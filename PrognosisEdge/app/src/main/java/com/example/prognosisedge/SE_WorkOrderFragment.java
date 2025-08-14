package com.example.prognosisedge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prognosisedge.models.TaskCountsResponse;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SE_WorkOrderFragment extends Fragment {

    private TextView allWorkOrdersCount, upcomingCount, inProcessCount, completedCount, overdueCount, cancelledCount;
    private Map<String, Integer> taskCounts = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.se__workorderfragment, container, false);

        // Initialize TextViews for counts
        allWorkOrdersCount = view.findViewById(R.id.all_work_orders_count);
        upcomingCount = view.findViewById(R.id.Upcoming_count);
        inProcessCount = view.findViewById(R.id.in_process_count);
        completedCount = view.findViewById(R.id.completed_count);
        overdueCount = view.findViewById(R.id.overdue_count);
        cancelledCount = view.findViewById(R.id.cancelled_count);

        // Set click listeners for each button to open WorkOrderListFragment with the respective filter
        view.findViewById(R.id.all_work_orders_button).setOnClickListener(v -> openWorkOrderList("All"));
        view.findViewById(R.id.Upcoming_button).setOnClickListener(v -> openWorkOrderList("Upcoming"));
        view.findViewById(R.id.in_process_button).setOnClickListener(v -> openWorkOrderList("In Progress"));
        view.findViewById(R.id.completed_button).setOnClickListener(v -> openWorkOrderList("Completed"));
        view.findViewById(R.id.overdue_button).setOnClickListener(v -> openWorkOrderList("Overdue"));
        view.findViewById(R.id.cancelled_button).setOnClickListener(v -> openWorkOrderList("Cancelled"));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch task counts whenever the fragment is resumed
        fetchTaskCounts();
    }

    private void fetchTaskCounts() {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        Call<TaskCountsResponse> call = apiService.fetchTaskCounts();

        call.enqueue(new Callback<TaskCountsResponse>() {
            @Override
            public void onResponse(Call<TaskCountsResponse> call, Response<TaskCountsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    TaskCountsResponse countsResponse = response.body();
                    updateTaskCounts(countsResponse);
                } else {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to fetch task counts.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TaskCountsResponse> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateTaskCounts(TaskCountsResponse countsResponse) {
        // Retrieve counts from response
        Map<String, Integer> taskCounts = countsResponse.getCounts();

        if (isAdded()) {
            // Normalize case when retrieving counts to avoid case-sensitive mismatches
            allWorkOrdersCount.setText(String.valueOf(countsResponse.getTotalCount()));
            upcomingCount.setText(String.valueOf(taskCounts.getOrDefault("upcoming", 0)));
            inProcessCount.setText(String.valueOf(taskCounts.getOrDefault("In Progress", 0)));
            completedCount.setText(String.valueOf(taskCounts.getOrDefault("Completed", 0)));  // Case-sensitive key from backend
            overdueCount.setText(String.valueOf(taskCounts.getOrDefault("Overdue", 0)));      // Add "Overdue" if backend includes it
            cancelledCount.setText(String.valueOf(taskCounts.getOrDefault("Cancelled", 0)));  // Add "Cancelled" if backend includes it
        }
    }


    private void openWorkOrderList(String status) {
        SE_WorkOrderListFragment listFragment = new SE_WorkOrderListFragment();
        Bundle args = new Bundle();
        args.putString("status", status);
        listFragment.setArguments(args);

        // Set the TaskUpdateListener
        listFragment.setTaskUpdateListener(this::fetchTaskCounts);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, listFragment)
                .addToBackStack(null)
                .commit();
    }
}
