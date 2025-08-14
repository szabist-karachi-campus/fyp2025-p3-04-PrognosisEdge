package com.example.prognosisedge;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prognosisedge.models.MaintenanceRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MaintenanceHistoryAdapter extends RecyclerView.Adapter<MaintenanceHistoryAdapter.MaintenanceViewHolder> {

    private final List<MaintenanceRecord> maintenanceRecords;
    private final OnRecordClickListener listener;

    // Constructor
    public MaintenanceHistoryAdapter(List<MaintenanceRecord> maintenanceRecords, OnRecordClickListener listener) {
        this.maintenanceRecords = maintenanceRecords;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaintenanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.maintenance_records, parent, false);
        return new MaintenanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaintenanceViewHolder holder, int position) {
        MaintenanceRecord record = maintenanceRecords.get(position);
        holder.bind(record);

        // Dynamically set the status light color
        int color = getStatusColor(record.getStatus());
        holder.statusLight.setBackgroundTintList(ColorStateList.valueOf(color));

        // Dynamically set the background tint based on status
        int backgroundColor = getBackgroundColor(holder.itemView, record.getStatus());
        holder.itemView.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
    }

    @Override
    public int getItemCount() {
        return maintenanceRecords.size();
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "upcoming":
                return Color.parseColor("#FFF200"); // Yellow
            case "in progress":
                return Color.parseColor("#0026FF"); // Blue
            case "completed":
                return Color.parseColor("#00FF00"); // Green
            case "over due":
                return Color.parseColor("#FF6600"); // Orange
            case "cancelled":
                return Color.parseColor("#FF0000"); // Red
            default:
                return Color.parseColor("#CCCCCC"); // Gray for unknown status
        }
    }

    private int getBackgroundColor(View view, String status) {
        int colorResId;
        switch (status.toLowerCase()) {
            case "completed":
                colorResId = R.color.green;
                break;
            case "cancelled":
                colorResId = R.color.pink;
                break;
            case "in progress":
                colorResId = R.color.blue;
                break;
            case "over due":
                colorResId = R.color.orange;
                break;
            case "upcoming":
                colorResId = R.color.yellow;
                break;
            default:
                colorResId = R.color.Assigned; // Default gray for unknown statuses
        }
        return ContextCompat.getColor(view.getContext(), colorResId);
    }

    // ViewHolder Class
    class MaintenanceViewHolder extends RecyclerView.ViewHolder {

        private final TextView workOrderTitleTextView, statusTextView, engineerTextView;
        private final TextView scheduledDateTextView, startedDateTextView, endedDateTextView;
        private final View statusLight;

        public MaintenanceViewHolder(@NonNull View itemView) {
            super(itemView);
            workOrderTitleTextView = itemView.findViewById(R.id.work_order_title);
            statusTextView = itemView.findViewById(R.id.status);
            engineerTextView = itemView.findViewById(R.id.engineer);
            scheduledDateTextView = itemView.findViewById(R.id.work_order_assigned_date);
            startedDateTextView = itemView.findViewById(R.id.work_order_started_date);
            endedDateTextView = itemView.findViewById(R.id.work_order_ended_date);
            statusLight = itemView.findViewById(R.id.status_light);

            // Item click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onRecordClick(maintenanceRecords.get(position));
                }
            });
        }

        public void bind(MaintenanceRecord record) {
            workOrderTitleTextView.setText(record.getTitle());
            statusTextView.setText(record.getStatus());
            engineerTextView.setText(record.getAssignedEngineer());

            // Format and set the scheduled, started, and ended dates
            scheduledDateTextView.setText(formatDate(record.getScheduledAt())); // Scheduled Date
            startedDateTextView.setText(formatDate(record.getStartedAt()));    // Started Date
            endedDateTextView.setText(formatDate(record.getEndedAt()));        // Ended Date
        }

        private String formatDate(String dateStr) {
            // Return "N/A" for null, empty, or invalid dates
            if (dateStr == null || dateStr.isEmpty() || dateStr.equalsIgnoreCase("null")) {
                return "N/A";
            }

            try {
                // Expected date format from the backend
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

                // Parse and format the date
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return "N/A"; // Fallback for unparsable dates
            }
        }
    }

    // Interface for click listener
    public interface OnRecordClickListener {
        void onRecordClick(MaintenanceRecord record);
    }
}
