package com.example.prognosisedge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prognosisedge.models.AddMachineResponse;
import com.example.prognosisedge.models.Machine;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SS_MachinesFragment extends Fragment {

    private RecyclerView machineRecyclerView;
    private SS_MachineAdapter machineAdapter;
    private List<Machine> machineList;

    private SwipeRefreshLayout swipeRefreshLayout; // Pull to refresh
    private Button openFilterButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ss__machinesfragment, container, false);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> fetchAllMachines()); // Pull to refresh logic

        // RecyclerView setup
        machineRecyclerView = rootView.findViewById(R.id.machine_recycler_view);
        machineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        machineList = new ArrayList<>();
        machineAdapter = new SS_MachineAdapter(getContext(), machineList);
        machineRecyclerView.setAdapter(machineAdapter);

        // Add button setup
        Button addButton = rootView.findViewById(R.id.add_machine_button);
        addButton.setOnClickListener(v -> {
            SS_MachineDialog.showAddMachineDialog(getContext(), newMachine -> {
                // Add the new machine to the list and update RecyclerView
                machineList.add(newMachine);
                machineAdapter.notifyItemInserted(machineList.size() - 1);
            });
        });

        // Fetch machines from API
        fetchAllMachines();

        return rootView;
    }

    private void openFilterBottomSheet() {
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("Status", Arrays.asList("Operating", "Decommissioned", "Off"));
        filters.put("Machine Type", Arrays.asList("Tablet Coating Machine", "Ampoule Washing Machine"));


        // Open FilterBottomSheet
        FilterBottomSheet bottomSheet = new FilterBottomSheet(filters);
        bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
    }


    private void fetchAllMachines() {
        swipeRefreshLayout.setRefreshing(true); // Show refresh indicator

        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        Call<AddMachineResponse> call = apiService.getAllMachines();

        call.enqueue(new Callback<AddMachineResponse>() {
            @Override
            public void onResponse(Call<AddMachineResponse> call, Response<AddMachineResponse> response) {
                swipeRefreshLayout.setRefreshing(false); // Hide refresh indicator

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Machine> machines = response.body().getMachines(); // Access "data"

                    if (machines != null && !machines.isEmpty()) {
                        machineList.clear();
                        machineList.addAll(machines);
                        machineAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No machines available.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to fetch machines. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AddMachineResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false); // Hide refresh indicator
                Toast.makeText(getContext(), "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}

