package com.example.prognosisedge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SE_NotificationFragment extends Fragment {

    private NotificationAdapter notificationAdapter;

    // Use shared notification list from ServiceEngineerActivity
    public static List<AppNotification> notificationList = ServiceEngineerActivity.seNotificationList;
    public static SE_NotificationFragment currentInstance = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.se__notificationfragment, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load stored notifications from SharedPreferences
        notificationList.clear();
        notificationList.addAll(NotificationStorage.loadNotifications(requireContext()));

        // Add default notification if list is empty
        if (notificationList.isEmpty()) {
            notificationList.add(new AppNotification("Welcome", "No new notifications yet", "You're all caught up!"));
        }

        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        return rootView;
    }

    // Helper method for real-time updates when fragment is visible
    public void notifyDataChanged() {
        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentInstance = this;

        // Refresh notifications from storage in case they were updated
        notificationList.clear();
        notificationList.addAll(NotificationStorage.loadNotifications(requireContext()));
        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        currentInstance = null;
    }
}