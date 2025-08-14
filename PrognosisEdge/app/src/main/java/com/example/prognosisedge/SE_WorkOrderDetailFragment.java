package com.example.prognosisedge;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prognosisedge.models.UpdateTaskRequest;
import com.example.prognosisedge.models.WorkOrder;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SE_WorkOrderDetailFragment extends Fragment {

    private TextView titleTextView, assignedDateTextView, assignedTimeTextView, supervisorCommentsTextView;
    private EditText notesEditText;
    private Spinner statusSpinner;
    private Button updateButton;
    private ImageView backButton;

    private WorkOrder workOrder;
    private Calendar selectedDateTime;

    private TaskUpdateListener taskUpdateListener; // Interface listener

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.se__workorder_detail_fragment, container, false);

        // Initialize views
        initializeViews(view);

        // Retrieve arguments
        if (getArguments() != null) {
            workOrder = getArguments().getParcelable("workOrder");
            populateData();
        }

        // Back button logic
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        updateButton.setOnClickListener(v -> {
            if (validateInputs()) {
                updateWorkOrder();
            }
        });

        return view;
    }

    private void initializeViews(View view) {
        titleTextView = view.findViewById(R.id.work_order_title);
        assignedDateTextView = view.findViewById(R.id.work_order_assigned_date);
        assignedTimeTextView = view.findViewById(R.id.work_order_assigned_time);
        supervisorCommentsTextView = view.findViewById(R.id.work_order_supervisor_comments_section);
        notesEditText = view.findViewById(R.id.work_order_notes);
        statusSpinner = view.findViewById(R.id.status_spinner);
        updateButton = view.findViewById(R.id.update_button);
        backButton = view.findViewById(R.id.back_button);

        // Set up date picker
        assignedDateTextView.setOnClickListener(v -> {
            if (isReschedulingAllowed()) {
                showDatePicker();
            } else {
                Toast.makeText(requireContext(), "Date selection is only allowed for tasks in 'Upcoming' status.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up time picker
        assignedTimeTextView.setOnClickListener(v -> {
            if (isReschedulingAllowed()) {
                showTimePicker();
            } else {
                Toast.makeText(requireContext(), "Time selection is only allowed for tasks in 'Upcoming' status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isReschedulingAllowed() {
        return "Upcoming".equalsIgnoreCase(workOrder.getStatus());
    }

    private void populateData() {
        titleTextView.setText(workOrder.getTitle());
        assignedDateTextView.setText(workOrder.getScheduledDate());
        assignedTimeTextView.setText(workOrder.getScheduledTime());
        supervisorCommentsTextView.setText(workOrder.getComments());
        notesEditText.setText(workOrder.getNotes());

        String[] statusOptions = getResources().getStringArray(R.array.status_options);
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(workOrder.getStatus())) {
                statusSpinner.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime = Calendar.getInstance();
                    selectedDateTime.set(year, month, dayOfMonth);
                    assignedDateTextView.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        if (selectedDateTime == null) {
            Toast.makeText(requireContext(), "Please select a date first.", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    assignedTimeTextView.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private boolean validateInputs() {
        String updatedDate = assignedDateTextView.getText().toString().trim();
        String updatedTime = assignedTimeTextView.getText().toString().trim();

        // Validation for rescheduling
        if (isReschedulingAllowed() && (updatedDate.isEmpty() || updatedTime.isEmpty())) {
            Toast.makeText(requireContext(), "Date and time cannot be empty for rescheduling.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedDateTime != null && selectedDateTime.before(Calendar.getInstance())) {
            Toast.makeText(requireContext(), "Date and time cannot be in the past.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateWorkOrder() {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);

        String updatedStatus = statusSpinner.getSelectedItem().toString();
        String updatedNotes = notesEditText.getText().toString().trim();
        String updatedDate = assignedDateTextView.getText().toString().trim();
        String updatedTime = assignedTimeTextView.getText().toString().trim();

        UpdateTaskRequest request = new UpdateTaskRequest(updatedStatus, updatedDate + " " + updatedTime, updatedNotes);

        apiService.updateWorkOrder(workOrder.getTaskId(), request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Work order updated successfully!", Toast.LENGTH_SHORT).show();

                            // Notify the listener about the update
                            if (taskUpdateListener != null) {
                                taskUpdateListener.onTaskUpdated();
                            }

                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update work order.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(requireContext(), "System temporarily unavailable. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setTaskUpdateListener(TaskUpdateListener listener) {
        this.taskUpdateListener = listener;
    }
}