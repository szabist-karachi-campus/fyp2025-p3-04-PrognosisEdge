package com.example.prognosisedge;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prognosisedge.models.DeleteMachineRequest;
import com.example.prognosisedge.models.DeleteMachineResponse;
import com.example.prognosisedge.models.Machine;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SS_MachineAdapter extends RecyclerView.Adapter<SS_MachineAdapter.MachineViewHolder> {

    private final List<Machine> machineList;
    private final Context context;

    public SS_MachineAdapter(Context context, List<Machine> machineList) {
        this.context = context;
        this.machineList = machineList;
    }


    @NonNull
    @Override
    public MachineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.machine_card, parent, false);
        return new MachineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MachineViewHolder holder, int position) {
        Machine machine = machineList.get(position);

        holder.machineName.setText("Name: " + machine.getName());
        holder.machineType.setText("Type: " + machine.getType());
        holder.machineSerial.setText("Serial: " + machine.getSerialNumber());
        holder.machineLocation.setText("Location: " + machine.getLocation());
        holder.machineStatus.setText("Status: " + machine.getStatus());

        // Alternate background colors
        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.drawable.cards);
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.green));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.cards);
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.pink));
        }

        // Edit button logic
        holder.editButton.setOnClickListener(v -> {
            SS_MachineDialog.showEditMachineDialog(context, machine, updatedMachine -> {
                machineList.set(position, updatedMachine);
                notifyItemChanged(position);
                Toast.makeText(context, "Machine updated successfully!", Toast.LENGTH_SHORT).show();
            });
        });

        // Delete button logic
        holder.deleteButton.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Delete Machine")
                    .setMessage("Are you sure you want to delete this machine?")
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        // Call API to delete machine
                        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);

                        apiService.deleteMachine(machine.getSerialNumber()).enqueue(new Callback<DeleteMachineResponse>() {
                            @Override
                            public void onResponse(Call<DeleteMachineResponse> call, Response<DeleteMachineResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    machineList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Machine deleted successfully!", Toast.LENGTH_SHORT).show();
                                } else if (response.body() != null) {
                                    Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Failed to delete machine. Please try again.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<DeleteMachineResponse> call, Throwable t) {
                                Toast.makeText(context, "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .create();

            // Set the custom background
            dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_backgroud));

            // Show the dialog
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return machineList.size();
    }

    public static class MachineViewHolder extends RecyclerView.ViewHolder {
        TextView machineName, machineType, machineSerial, machineLocation, machineStatus; // Add machineStatus
        ImageView editButton, deleteButton;

        public MachineViewHolder(@NonNull View itemView) {
            super(itemView);
            machineName = itemView.findViewById(R.id.machine_name);
            machineType = itemView.findViewById(R.id.machine_type);
            machineSerial = itemView.findViewById(R.id.machine_serial);
            machineLocation = itemView.findViewById(R.id.machine_location);
            machineStatus = itemView.findViewById(R.id.machine_status); // Initialize machineStatus
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
