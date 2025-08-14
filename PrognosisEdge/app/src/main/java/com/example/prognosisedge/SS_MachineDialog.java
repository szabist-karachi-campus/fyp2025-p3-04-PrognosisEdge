package com.example.prognosisedge;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.prognosisedge.models.AddMachineRequest;
import com.example.prognosisedge.models.AddMachineResponse;
import com.example.prognosisedge.models.EditMachineRequest;
import com.example.prognosisedge.models.Machine;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SS_MachineDialog {

    public interface MachineDialogCallback {
        void onMachineUpdated(Machine machine);
    }

    public static void showAddMachineDialog(Context context, MachineDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Machine");

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.ss__machinedialog, null);
        builder.setView(dialogView);

        // Initialize input fields
        EditText serialEditText = dialogView.findViewById(R.id.machine_serial_input);
        EditText nameEditText = dialogView.findViewById(R.id.machine_name_input);
        Spinner typeSpinner = dialogView.findViewById(R.id.machine_type_input);
        EditText locationEditText = dialogView.findViewById(R.id.machine_location_input);
        Spinner statusSpinner = dialogView.findViewById(R.id.machine_status_input);

        // Initialize spinners
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                context, R.array.machine_type_options, android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                context, R.array.machine_status_options, android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Handle "Add" button in the custom layout
        Button addButton = dialogView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            String serialNumber = serialEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String type = typeSpinner.getSelectedItem() != null ? typeSpinner.getSelectedItem().toString() : "";
            String location = locationEditText.getText().toString().trim();
            String status = statusSpinner.getSelectedItem() != null ? statusSpinner.getSelectedItem().toString() : "";

            // Validate input fields with specific error messages
            if (serialNumber.isEmpty()) {
                Toast.makeText(context, "Serial Number is required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (name.isEmpty()) {
                Toast.makeText(context, "Name is required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (type.isEmpty()) {
                Toast.makeText(context, "Please select a valid machine type.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (location.isEmpty()) {
                Toast.makeText(context, "Location is required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (status.isEmpty()) {
                Toast.makeText(context, "Please select a valid status.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log the request for debugging
            Log.d("AddMachineDebug", "Serial: " + serialNumber +
                    ", Name: " + name +
                    ", Type: " + type +
                    ", Location: " + location +
                    ", Status: " + status);

            // Create API request
            AddMachineRequest request = new AddMachineRequest(serialNumber, name, type, location, status);
            ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
            apiService.addMachine(request).enqueue(new Callback<AddMachineResponse>() {
                @Override
                public void onResponse(Call<AddMachineResponse> call, Response<AddMachineResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        callback.onMachineUpdated(new Machine(serialNumber, name, type, location, status));
                        Toast.makeText(context, "Machine added successfully.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // Dismiss dialog on success
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Failed to add machine. Please try again.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AddMachineResponse> call, Throwable t) {
                    Toast.makeText(context, "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
                    Log.e("AddMachineError", "Error: " + t.getMessage(), t);
                }
            });
        });

        // Handle "Cancel" button in the custom layout
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_backgroud));
        dialog.show();
    }



    public static void showEditMachineDialog(Context context, Machine machine, MachineDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Machine");

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.ss__machinedialog, null);
        builder.setView(dialogView);

        // Initialize input fields
        EditText serialEditText = dialogView.findViewById(R.id.machine_serial_input);
        EditText nameEditText = dialogView.findViewById(R.id.machine_name_input);
        Spinner typeSpinner = dialogView.findViewById(R.id.machine_type_input);
        EditText locationEditText = dialogView.findViewById(R.id.machine_location_input);
        Spinner statusSpinner = dialogView.findViewById(R.id.machine_status_input);

        // Disable editing of the serial number
        serialEditText.setText(machine.getSerialNumber());
        serialEditText.setFocusable(false);
        serialEditText.setClickable(false);

        // Initialize spinners
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                context, R.array.machine_type_options, android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                context, R.array.machine_status_options, android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Pre-fill the dialog with the machine's current details
        serialEditText.setText(machine.getSerialNumber());
        nameEditText.setText(machine.getName());
        locationEditText.setText(machine.getLocation());

        // Select the current machine type and status
        if (machine.getType() != null) {
            int typePosition = typeAdapter.getPosition(machine.getType());
            typeSpinner.setSelection(typePosition);
        }

        if (machine.getStatus() != null) {
            int statusPosition = statusAdapter.getPosition(machine.getStatus());
            statusSpinner.setSelection(statusPosition);
        }

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Handle "Save" button in the custom layout
        Button saveButton = dialogView.findViewById(R.id.add_button);
        saveButton.setText("Save");
        saveButton.setOnClickListener(v -> {
            String serialNumber = serialEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String type = typeSpinner.getSelectedItem() != null ? typeSpinner.getSelectedItem().toString() : "";
            String location = locationEditText.getText().toString().trim();
            String status = statusSpinner.getSelectedItem() != null ? statusSpinner.getSelectedItem().toString() : "";

            // Validate input fields with specific error messages
            if (serialNumber.isEmpty()) {
                Toast.makeText(context, "Serial Number is required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (name.isEmpty()) {
                Toast.makeText(context, "Name is required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (type.isEmpty()) {
                Toast.makeText(context, "Please select a valid machine type.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (location.isEmpty()) {
                Toast.makeText(context, "Location is required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (status.isEmpty()) {
                Toast.makeText(context, "Please select a valid status.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log the request for debugging
            Log.d("EditMachineDebug", "Serial: " + serialNumber +
                    ", Name: " + name +
                    ", Type: " + type +
                    ", Location: " + location +
                    ", Status: " + status);

            // Create API request
            EditMachineRequest request = new EditMachineRequest(serialNumber, name, type, location, status);
            ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
            apiService.editMachine(request).enqueue(new Callback<AddMachineResponse>() {
                @Override
                public void onResponse(Call<AddMachineResponse> call, Response<AddMachineResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Update the machine object
                        machine.setSerialNumber(serialNumber);
                        machine.setName(name);
                        machine.setType(type);
                        machine.setLocation(location);
                        machine.setStatus(status);

                        callback.onMachineUpdated(machine);

                        Toast.makeText(context, "Machine updated successfully.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // Dismiss dialog on success
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Failed to update machine. Please try again.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AddMachineResponse> call, Throwable t) {
                    Toast.makeText(context, "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
                    Log.e("EditMachineError", "Error: " + t.getMessage(), t);
                }
            });
        });

        // Handle "Cancel" button in the custom layout
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_backgroud));
        dialog.show();
    }


}
