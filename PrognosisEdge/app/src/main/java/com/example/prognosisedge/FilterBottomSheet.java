package com.example.prognosisedge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterBottomSheet extends BottomSheetDialogFragment {
    private Map<String, List<String>> filters; // Category to options map
    private FilterListener filterListener; // Callback for filter selection

    // Constructor to set filters
    public FilterBottomSheet(Map<String, List<String>> filters) {
        this.filters = filters != null ? filters : new HashMap<>();
    }

    // Listener setter
    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        // Container for dynamic filters
        ViewGroup filterContainer = view.findViewById(R.id.filter_container);

        // Map to hold selected options
        Map<String, String> selectedFilters = new HashMap<>();

        // Dynamically add filter categories and options
        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            String category = entry.getKey();
            List<String> options = entry.getValue();

            // Add category title
            TextView categoryTitle = new TextView(requireContext());
            categoryTitle.setText(category);
            categoryTitle.setTextSize(16f);
            categoryTitle.setPadding(0, 12, 0, 8);
            filterContainer.addView(categoryTitle);

            // Add ChipGroup for options
            ChipGroup chipGroup = new ChipGroup(requireContext());
            chipGroup.setChipSpacing(8);

            for (String option : options) {
                Chip chip = new Chip(requireContext());
                chip.setText(option);
                chip.setCheckable(true); // Make chips checkable
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedFilters.put(category, option); // Add to selected filters
                    } else {
                        selectedFilters.remove(category); // Remove if unchecked
                    }
                });
                chipGroup.addView(chip);
            }

            filterContainer.addView(chipGroup);
        }

        // Apply Button
        Button applyButton = view.findViewById(R.id.apply_button);
        applyButton.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onApplyFilters(selectedFilters);
            }
            dismiss();
        });

        // Reset Button
        Button resetButton = view.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> {
            selectedFilters.clear(); // Clear selected filters
            filterContainer.removeAllViews(); // Reset the chips
            for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                String category = entry.getKey();
                List<String> options = entry.getValue();

                TextView categoryTitle = new TextView(requireContext());
                categoryTitle.setText(category);
                categoryTitle.setTextSize(16f);
                categoryTitle.setPadding(0, 12, 0, 8);
                filterContainer.addView(categoryTitle);

                ChipGroup chipGroup = new ChipGroup(requireContext());
                chipGroup.setChipSpacing(8);
                for (String option : options) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(option);
                    chip.setCheckable(true);
                    chipGroup.addView(chip);
                }
                filterContainer.addView(chipGroup);
            }
        });

        return view;
    }

    // Listener interface for filters
    public interface FilterListener {
        void onApplyFilters(Map<String, String> selectedFilters);
    }
}
