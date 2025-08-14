package com.example.prognosisedge;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MaintenancePagerAdapter extends FragmentStateAdapter {

    public MaintenancePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SE_MaintenanceHistory(); // First tab
            case 1:
                return new SE_ScheduleMaintenanceFragment(); // Second tab
            default:
                return new SE_MaintenanceHistory();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs
    }
}
