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

import com.example.prognosisedge.models.WorkOrder;

import java.util.List;

public class SE_WorkOrderAdapter extends RecyclerView.Adapter<SE_WorkOrderAdapter.WorkOrderViewHolder> {

    private final List<WorkOrder> workOrders;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WorkOrder workOrder);
    }

    public SE_WorkOrderAdapter(List<WorkOrder> workOrders, OnItemClickListener listener) {
        this.workOrders = workOrders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workorder_items, parent, false);
        return new WorkOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkOrderViewHolder holder, int position) {
        WorkOrder workOrder = workOrders.get(position);
        holder.title.setText(workOrder.getTitle());
        holder.status.setText(workOrder.getStatus());
        holder.date.setText(workOrder.getScheduledDate()); // Use scheduledDate for display
        holder.time.setText(workOrder.getScheduledTime()); // Use scheduledTime for display
        holder.notes.setText(workOrder.getNotes());

        // Dynamically set status light color
        int color;
        switch (workOrder.getStatus()) {
            case "Upcoming":
                color = Color.parseColor("#FFF200"); // Yellow
                break;
            case "In Progress":
                color = Color.parseColor("#0026FF"); // Blue
                break;
            case "Completed":
                color = Color.parseColor("#00FF00"); // Green
                break;
            case "Overdue":
                color = Color.parseColor("#FF6600"); // Orange
                break;
            case "Cancelled":
                color = Color.parseColor("#FF0000"); // Red
                break;
            default:
                color = Color.parseColor("#CCCCCC"); // Gray for unknown status
        }
        holder.statusLight.setBackgroundTintList(ColorStateList.valueOf(color));

        // Dynamically set background color based on status
        if (workOrder.getStatus().equalsIgnoreCase("Completed")) {
            holder.itemView.setBackgroundResource(R.drawable.cards);
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.green));
        } else if (workOrder.getStatus().equalsIgnoreCase("Cancelled")) {
            holder.itemView.setBackgroundResource(R.drawable.cards);
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.pink));
        } else if (workOrder.getStatus().equalsIgnoreCase("In Progress")) {
            holder.itemView.setBackgroundResource(R.drawable.cards);
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.blue));
        } else if (workOrder.getStatus().equalsIgnoreCase("Overdue")) {
            holder.itemView.setBackgroundResource(R.drawable.cards);
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.orange));
        } else if (workOrder.getStatus().equalsIgnoreCase("Upcoming")) {
            holder.itemView.setBackgroundResource(R.drawable.cards);
            holder.itemView.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.yellow));
        }

        // Set up click listener for each item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(workOrder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workOrders.size();
    }

    static class WorkOrderViewHolder extends RecyclerView.ViewHolder {
        TextView title, notes, date, time, status; // Added time field
        View statusLight;

        public WorkOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.work_order_title);
            notes = itemView.findViewById(R.id.work_order_notes);
            date = itemView.findViewById(R.id.work_order_assigned_date);
            time = itemView.findViewById(R.id.work_order_assigned_time); // Initialize time field
            status = itemView.findViewById(R.id.work_order_status);
            statusLight = itemView.findViewById(R.id.status_light);
        }
    }
}
