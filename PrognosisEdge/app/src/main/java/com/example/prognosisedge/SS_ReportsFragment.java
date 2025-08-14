package com.example.prognosisedge;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prognosisedge.models.MachineReport;
import com.example.prognosisedge.models.MachineReportRequest;
import com.example.prognosisedge.models.MachineReportResponse;
import com.example.prognosisedge.network.ApiClient;
import com.example.prognosisedge.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SS_ReportsFragment extends Fragment {

    private EditText startDateEditText;
    private EditText endDateEditText;
    private Button generateReportBtn;
    private LinearLayout reportsContainer;

    // Separate SharedPreferences for different purposes
    private SharedPreferences reportPrefs;  // For storing reports
    private SharedPreferences userPrefs;    // For getting user data

    private Gson gson = new Gson();
    private static final String REPORT_PREF_NAME = "report_prefs";
    private static final String USER_PREF_NAME = "PrognosisEdgePrefs";
    private static final String KEY_REPORTS = "machine_reports";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ss__reportsfragment, container, false);

        // Initialize both SharedPreferences
        reportPrefs = requireContext().getSharedPreferences(REPORT_PREF_NAME, Context.MODE_PRIVATE);
        userPrefs = requireContext().getSharedPreferences(USER_PREF_NAME, Context.MODE_PRIVATE);

        startDateEditText = view.findViewById(R.id.start_date);
        endDateEditText = view.findViewById(R.id.end_date);
        generateReportBtn = view.findViewById(R.id.generate_report_button);
        reportsContainer = view.findViewById(R.id.reports_container);

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        generateReportBtn.setOnClickListener(v -> {
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();

            // Get username from user preferences with correct key
            String username = userPrefs.getString("userName", "admin");

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            } else {
                generateMachineReport(startDate, endDate, username);
            }
        });

        loadStoredReports();

        return view;
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    dateEditText.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void generateMachineReport(String startDate, String endDate, String username) {
        ApiService apiService = ApiClient.getRetrofitClient().create(ApiService.class);
        MachineReportRequest request = new MachineReportRequest(startDate, endDate, username);

        Call<MachineReportResponse> call = apiService.generateMachineReport(request);

        call.enqueue(new Callback<MachineReportResponse>() {
            @Override
            public void onResponse(Call<MachineReportResponse> call, Response<MachineReportResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MachineReport> reports = response.body().getReports();
                    String message = response.body().getMessage();

                    // Always store and display all reports from backend
                    storeReportsLocally(reports);
                    displayReports(reports);

                    // Show appropriate message
                    if (message != null && message.contains("already exists")) {
                        Toast.makeText(getContext(), "Report already exists for this date range!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Machine report generated successfully!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No machine reports available for this range.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MachineReportResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to connect: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeReportsLocally(List<MachineReport> reports) {
        // Store reports in report preferences
        String json = gson.toJson(reports);
        reportPrefs.edit().putString(KEY_REPORTS, json).apply();
    }

    private void loadStoredReports() {
        // Load reports from report preferences
        String json = reportPrefs.getString(KEY_REPORTS, null);
        if (json != null) {
            Type type = new TypeToken<List<MachineReport>>() {}.getType();
            List<MachineReport> storedReports = gson.fromJson(json, type);
            if (storedReports != null) displayReports(storedReports);
        }
    }

    private void displayReports(List<MachineReport> reports) {
        reportsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (MachineReport report : reports) {
            View view = inflater.inflate(R.layout.ss_report_card, null);

            TextView titleView = view.findViewById(R.id.report_title);
            TextView descriptionView = view.findViewById(R.id.report_description);

            // Set the card title and description
            titleView.setText("Machine: " + report.getSerialNumber());

            // Format dates properly without time
            String formattedDescription = "Date: " + formatDateForDisplay(report.getDateRangeStart()) +
                    " to " + formatDateForDisplay(report.getDateRangeEnd());
            descriptionView.setText(formattedDescription);

            view.setOnClickListener(v -> {
                SS_ReportDetailedFragment detailedFragment = SS_ReportDetailedFragment.newInstance(report);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailedFragment)
                        .addToBackStack(null)
                        .commit();
            });

            reportsContainer.addView(view);
        }
    }

    // ADD this helper method to format dates properly
    private String formatDateForDisplay(String dateString) {
        try {
            // Parse the date string from backend (e.g., "Mon, 21 Apr 2025 19:00:00 GMT")
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            Date date = inputFormat.parse(dateString);

            // Format to simple date (e.g., "21 Apr 2025")
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            return outputFormat.format(date);
        } catch (Exception e) {
            // If parsing fails, try to extract just the date part
            if (dateString != null && dateString.contains(",")) {
                String[] parts = dateString.split(",");
                if (parts.length > 1) {
                    String datePart = parts[1].trim();
                    if (datePart.contains(" ")) {
                        String[] dateParts = datePart.split(" ");
                        if (dateParts.length >= 3) {
                            return dateParts[0] + " " + dateParts[1] + " " + dateParts[2];
                        }
                    }
                }
            }
            // Fallback: return original string
            return dateString;
        }
    }
}