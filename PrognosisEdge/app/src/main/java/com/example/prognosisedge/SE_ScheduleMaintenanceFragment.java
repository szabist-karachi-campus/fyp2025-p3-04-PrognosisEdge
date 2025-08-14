package com.example.prognosisedge;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prognosisedge.models.MachineNamesResponse;
import com.example.prognosisedge.models.ScheduleMaintenanceRequest;
import com.example.prognosisedge.models.ScheduleMaintenanceResponse;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SE_ScheduleMaintenanceFragment extends Fragment {

    private EditText titleInput, dateInput, timeInput, notesInput;
    private Button submitButton;
    private AutoCompleteTextView machineAutoComplete;
    private Spinner machineTypeSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.se__schedulemaintenancefragment, container, false);

        // Initialize form fields
        titleInput = rootView.findViewById(R.id.title_name);
        dateInput = rootView.findViewById(R.id.schedule_date);
        timeInput = rootView.findViewById(R.id.schedule_time);
        machineAutoComplete = rootView.findViewById(R.id.select_machine_autocomplete);
        machineTypeSpinner = rootView.findViewById(R.id.machine_type_input);
        notesInput = rootView.findViewById(R.id.schedule_notes_section);
        submitButton = rootView.findViewById(R.id.submit_button);

        // Set up date picker
        dateInput.setOnClickListener(v -> showDatePickerDialog());

        // Set up time picker
        timeInput.setOnClickListener(v -> showTimePickerDialog());

        // Set up machine type spinner listener
        setupMachineTypeSpinner();

        // Set up submit button click listener
        submitButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String date = dateInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            String machine = machineAutoComplete.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();

            if (validateInputs(title, date, time, machine, notes)) {
                scheduleMaintenance(title, date, time, machine, notes);
            }
        });

        return rootView;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    dateInput.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    String selectedTime = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
                    timeInput.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void setupMachineTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.machine_type_options, // Your array of machine types
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        machineTypeSpinner.setAdapter(adapter);

        machineTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = machineTypeSpinner.getSelectedItem().toString();
                fetchMachineNames(selectedType); // Fetch machines by the selected type
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private boolean validateInputs(String title, String date, String time, String machine, String notes) {
        // Check if all required fields are filled
        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            return false;
        }
        if (date.isEmpty()) {
            dateInput.setError("Date is required");
            return false;
        }
        if (time.isEmpty()) {
            timeInput.setError("Time is required");
            return false;
        }
        if (machine.isEmpty()) {
            machineAutoComplete.setError("Please select a machine");
            return false;
        }

        // Validate if the selected date and time are not in the past
        Calendar currentDateTime = Calendar.getInstance();
        Calendar selectedDateTime = Calendar.getInstance();
        String[] dateParts = date.split("/");
        String[] timeParts = time.split(":");

        try {
            // Parse the date and time input
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Months are 0-based
            int year = Integer.parseInt(dateParts[2]);
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            selectedDateTime.set(year, month, day, hour, minute);

            if (selectedDateTime.before(currentDateTime)) {
                Toast.makeText(requireContext(), "Date and time cannot be in the past.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid date or time format.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void scheduleMaintenance(String title, String date, String time, String machine, String notes) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);

        // Fetch engineer name from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PrognosisEdgePrefs", Context.MODE_PRIVATE);
        String engineerName = sharedPreferences.getString("userName", "");

        if (engineerName.isEmpty()) {
            Toast.makeText(requireContext(), "Failed to fetch engineer details. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Construct scheduled_at from date and time
        String scheduledAt = date + " " + time;

        // Handle optional notes
        if (notes.isEmpty()) {
            notes = null; // Optional: Use null or an empty string depending on backend handling
        }

        // Create the request object
        ScheduleMaintenanceRequest request = new ScheduleMaintenanceRequest(
                title, machine, engineerName, scheduledAt, notes
        );

        // Make the API call
        apiService.scheduleMaintenance(request).enqueue(new Callback<ScheduleMaintenanceResponse>() {
            @Override
            public void onResponse(Call<ScheduleMaintenanceResponse> call, Response<ScheduleMaintenanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                        clearForm();
                    } else {
                        Toast.makeText(requireContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to schedule maintenance. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ScheduleMaintenanceResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "System temporarily unavailable. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearForm() {
        if (titleInput != null) titleInput.setText("");
        if (dateInput != null) dateInput.setText("");
        if (timeInput != null) timeInput.setText("");
        if (machineAutoComplete != null) machineAutoComplete.setText(""); // Reset AutoCompleteTextView
        if (notesInput != null) notesInput.setText(""); // Optional: Reset notes if provided
    }


    private void fetchMachineNames(String machineType) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);

        apiService.getMachinesByType(machineType).enqueue(new Callback<MachineNamesResponse>() {
            @Override
            public void onResponse(Call<MachineNamesResponse> call, Response<MachineNamesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> machineNames = response.body().getMachineNames();
                    if (machineNames.isEmpty()) {
                        Toast.makeText(requireContext(), "No machines found for this type.", Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                machineNames
                        );
                        machineAutoComplete.setAdapter(adapter);
                        machineAutoComplete.setThreshold(1);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch machine names.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MachineNamesResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "System temporarily unavailable. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
