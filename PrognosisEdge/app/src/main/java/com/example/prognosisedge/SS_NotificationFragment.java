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

import java.util.ArrayList;
import java.util.List;

public class SS_NotificationFragment extends Fragment {

    private NotificationAdapter notificationAdapter;

    // Rename to AppNotification to avoid conflict with android.app.Notification
    public static List<AppNotification> notificationList = SystemSupervisorActivity.ssNotificationList;
    public static SS_NotificationFragment currentInstance = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ss__notificationfragment, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Default (only if needed)
        if (notificationList.isEmpty()) {
            notificationList.add(new AppNotification("Welcome", "No new notifications yet", "You're all caught up!"));
        }

        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        return rootView;
    }

    // ✅ Optional helper for real-time update if fragment is visible
    public void notifyDataChanged() {
        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentInstance = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        currentInstance = null;
    }
}
