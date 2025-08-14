package com.example.prognosisedge;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prognosisedge.models.TasksResponse;
import com.example.prognosisedge.models.WorkOrder;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SE_WorkOrderListFragment extends Fragment {

    private RecyclerView recyclerView;
    private SE_WorkOrderAdapter adapter;
    private String status;
    private ImageView backButton;
    private TextView title;
    private TaskUpdateListener taskUpdateListener; // Interface listener
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.se__workorderlistfragment, container, false);

        initializeViews(rootView);

        if (getArguments() != null) {
            status = getArguments().getString("status", "All"); // Default to "All" if not provided
        }

        title.setText(status + " Work Orders");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch tasks based on status
        fetchWorkOrders(status);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return rootView;
    }

    private void initializeViews(View rootView) {
        backButton = rootView.findViewById(R.id.back_button);
        title = rootView.findViewById(R.id.work_order_list_title);
        recyclerView = rootView.findViewById(R.id.work_order_recycler_view);
    }

    private void fetchWorkOrders(String status) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);

        apiService.fetchTasks(status).enqueue(new Callback<TasksResponse>() {
            @Override
            public void onResponse(Call<TasksResponse> call, Response<TasksResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<WorkOrder> workOrders = response.body().getTasks();
                    if (workOrders != null && !workOrders.isEmpty()) {
                        setupRecyclerView(workOrders);
                    } else {
                        Toast.makeText(getContext(), "No work orders found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to fetch tasks. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TasksResponse> call, Throwable t) {
                Toast.makeText(getContext(), "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setupRecyclerView(List<WorkOrder> workOrders) {
        if (workOrders == null || workOrders.isEmpty()) {
            Toast.makeText(getContext(), "No tasks found for this category.", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new SE_WorkOrderAdapter(workOrders, this::openWorkOrderDetail);
        recyclerView.setAdapter(adapter);
    }


    private void openWorkOrderDetail(WorkOrder workOrder) {
        SE_WorkOrderDetailFragment detailFragment = new SE_WorkOrderDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("workOrder", workOrder);
        detailFragment.setArguments(args);

        // Pass the TaskUpdateListener to notify SE_WorkOrderFragment
        detailFragment.setTaskUpdateListener(() -> {
            fetchWorkOrders(status); // Refresh the list
            if (taskUpdateListener != null) {
                taskUpdateListener.onTaskUpdated(); // Notify parent fragment
            }
        });

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }


    public void setTaskUpdateListener(TaskUpdateListener listener) {
        this.taskUpdateListener = listener;
    }
}
