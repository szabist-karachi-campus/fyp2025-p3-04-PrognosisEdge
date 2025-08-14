package com.example.prognosisedge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<AppNotification> notificationList;

    public NotificationAdapter(List<AppNotification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.se__notifications, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification notification = notificationList.get(position);
        holder.categoryText.setText(notification.getCategory());
        holder.notificationTitle.setText(notification.getTitle());
        holder.notificationDescription.setText(notification.getDescription());

        // Optional: Remove notification on close button click
        holder.closeButton.setOnClickListener(v -> {
            notificationList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            notifyItemRangeChanged(holder.getAdapterPosition(), notificationList.size());
            NotificationStorage.saveNotifications(holder.itemView.getContext(), notificationList);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, notificationTitle, notificationDescription;
        ImageView closeButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.category_text);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDescription = itemView.findViewById(R.id.notification_description);
            closeButton = itemView.findViewById(R.id.close_button);
        }
    }
}
