package com.example.prognosisedge;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prognosisedge.models.WorkOrderReport;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SE_ReportDetailedFragment extends Fragment {

    private static final String ARG_REPORT = "report";
    private WorkOrderReport report;

    // Colors using your pastel theme
    private final int[] STATUS_COLORS = {
            Color.parseColor("#D1ECBF"),   // Green for Completed
            Color.parseColor("#FFF1B8"),   // Yellow for In Progress
            Color.parseColor("#FBB1A1"),   // Orange for Cancelled
            Color.parseColor("#FFE2E7"),   // Pink for Overdue
            Color.parseColor("#E3ECFD")    // Blue for Total
    };

    private final int[] PERFORMANCE_COLORS = {
            Color.parseColor("#E3ECFD"),   // Blue
            Color.parseColor("#D1ECBF"),   // Green
            Color.parseColor("#FFF1B8"),   // Yellow
            Color.parseColor("#FFE2E7"),   // Pink
            Color.parseColor("#FBB1A1")    // Orange
    };

    public static SE_ReportDetailedFragment newInstance(WorkOrderReport report) {
        SE_ReportDetailedFragment fragment = new SE_ReportDetailedFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPORT, (Serializable) report);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.se_reportdetailedfragment, container, false);

        report = (WorkOrderReport) getArguments().getSerializable(ARG_REPORT);
        if (report == null) return view;

        // Back navigation
        ImageView back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Text setup
        ((TextView) view.findViewById(R.id.report_title)).setText("Work Order Analytics");
        ((TextView) view.findViewById(R.id.stat_total_work_orders)).setText("Total Work Orders: " + report.getTotalWorkOrders());
        ((TextView) view.findViewById(R.id.stat_completed)).setText("Completed: " + report.getCompletedWorkOrders());
        ((TextView) view.findViewById(R.id.stat_in_progress)).setText("In Progress: " + report.getInProgressWorkOrders());

        DecimalFormat df = new DecimalFormat("#.##");
        ((TextView) view.findViewById(R.id.stat_avg_completion)).setText("Avg Completion: " + df.format(report.getAverageCompletionTime()) + " hrs");

        // Setup only 3 charts now
        setupStatusPieChart(view.findViewById(R.id.status_pie_chart));
        setupCompletionBarChart(view.findViewById(R.id.completion_bar_chart));
        setupPerformanceChart(view.findViewById(R.id.performance_chart));

        // Setup FAB for PDF export
        FloatingActionButton fabExportPdf = view.findViewById(R.id.fab_export_pdf);
        fabExportPdf.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FD6746")));
        fabExportPdf.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        fabExportPdf.setOnClickListener(v -> exportToPdf());

        return view;
    }

    private void exportToPdf() {
        try {
            // Create PDF document
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            // Add title
            paint.setTextSize(22f); // Larger title
            paint.setColor(Color.parseColor("#4E4E58"));
            paint.setFakeBoldText(true);
            canvas.drawText("Work Order Analytics Report", 50, 40, paint);

            // Add report details - larger text
            paint.setTextSize(12f); // Increased from 10f
            paint.setFakeBoldText(false);
            int yPosition = 75;

            canvas.drawText("Date Range: " + report.getDateRangeStart() + " to " + report.getDateRangeEnd(), 50, yPosition, paint);
            yPosition += 18;
            canvas.drawText("Generated by: " + report.getCreatedBy() + " | Generated on: " + new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()), 50, yPosition, paint);
            yPosition += 28;

            // Add summary statistics - larger text
            paint.setTextSize(16f); // Increased from 12f
            paint.setFakeBoldText(true);
            canvas.drawText("Work Order Summary", 50, yPosition, paint);
            yPosition += 25;

            paint.setTextSize(12f); // Increased from 9f
            paint.setFakeBoldText(false);
            canvas.drawText("Total: " + report.getTotalWorkOrders(), 70, yPosition, paint);
            canvas.drawText("Completed: " + report.getCompletedWorkOrders(), 200, yPosition, paint);
            canvas.drawText("In Progress: " + report.getInProgressWorkOrders(), 350, yPosition, paint);
            yPosition += 18;

            DecimalFormat df = new DecimalFormat("#.##");
            canvas.drawText("Cancelled: " + report.getCancelledWorkOrders(), 70, yPosition, paint);
            canvas.drawText("Overdue: " + report.getOverdueWorkOrders(), 200, yPosition, paint);
            canvas.drawText("Avg Time: " + df.format(report.getAverageCompletionTime()) + "h", 350, yPosition, paint);
            yPosition += 35;

            // Chart titles - larger
            paint.setTextSize(14f); // Increased from 11f
            paint.setFakeBoldText(true);
            canvas.drawText("Status Distribution", 50, yPosition, paint);
            canvas.drawText("Completion Analysis", 320, yPosition, paint);
            yPosition += 20;

            // Draw Pie Chart (left side) - fixed
            float pieChartBottom = drawPieChartFixed(canvas, 50, yPosition, 200, 150);

            // Draw Horizontal Bar Chart (right side) - fixed
            drawHorizontalBarChartFixed(canvas, 320, yPosition, 220, 120);

            // Move to next section - use the pie chart bottom position
            yPosition = (int) pieChartBottom + 30;

            // Draw Performance Chart (full width below both charts)
            paint.setTextSize(14f); // Increased from 12f
            paint.setFakeBoldText(true);
            paint.setColor(Color.parseColor("#4E4E58"));
            canvas.drawText("Performance Metrics", 50, yPosition, paint);
            yPosition += 20;

            drawPerformanceBarChartProper(canvas, 50, yPosition, 480, 180);

            // Add footer
            paint.setTextSize(10f); // Increased from 8f
            paint.setColor(Color.GRAY);
            paint.setFakeBoldText(false);
            canvas.drawText("Generated by PrognosisEdge App", 50, 820, paint);

            pdfDocument.finishPage(page);

            // Save the PDF
            String fileName = "WorkOrder_Report_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS);

                android.net.Uri uri = requireContext().getContentResolver().insert(android.provider.MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    java.io.OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                    pdfDocument.writeTo(outputStream);
                    outputStream.close();
                    Toast.makeText(getContext(), "PDF exported to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                pdfDocument.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(getContext(), "PDF exported to Downloads: " + fileName, Toast.LENGTH_LONG).show();
            }

            pdfDocument.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Fixed Pie Chart - proper circle with hole in center
    private float drawPieChartFixed(Canvas canvas, float x, float y, float width, float height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Make it a perfect circle
        float radius = Math.min(width, height) / 2 - 20;
        float centerX = x + width / 2;
        float centerY = y + height / 2;

        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        int total = report.getTotalWorkOrders();
        if (total == 0) return y + height + 60;

        float completed = (float) report.getCompletedWorkOrders() / total * 360;
        float inProgress = (float) report.getInProgressWorkOrders() / total * 360;
        float cancelled = (float) report.getCancelledWorkOrders() / total * 360;
        float overdue = (float) report.getOverdueWorkOrders() / total * 360;

        float startAngle = -90; // Start from top

        // Draw completed slice (green)
        if (completed > 0) {
            paint.setColor(Color.parseColor("#D1ECBF"));
            canvas.drawArc(oval, startAngle, completed, true, paint);
            startAngle += completed;
        }

        // Draw in progress slice (yellow)
        if (inProgress > 0) {
            paint.setColor(Color.parseColor("#FFF1B8"));
            canvas.drawArc(oval, startAngle, inProgress, true, paint);
            startAngle += inProgress;
        }

        // Draw cancelled slice (orange) - if any
        if (cancelled > 0) {
            paint.setColor(Color.parseColor("#FBB1A1"));
            canvas.drawArc(oval, startAngle, cancelled, true, paint);
            startAngle += cancelled;
        }

        // Draw overdue slice (pink) - if any
        if (overdue > 0) {
            paint.setColor(Color.parseColor("#FFE2E7"));
            canvas.drawArc(oval, startAngle, overdue, true, paint);
        }

        // Draw center hole (like a donut chart)
        paint.setColor(Color.WHITE);
        float innerRadius = radius * 0.4f;
        canvas.drawCircle(centerX, centerY, innerRadius, paint);

        // Add numbers inside slices
        paint.setColor(Color.parseColor("#4E4E58"));
        paint.setTextSize(12f);
        paint.setFakeBoldText(true);

        if (report.getCompletedWorkOrders() > 0) {
            // Position text in completed slice
            float angle = -90 + (completed / 2);
            float textRadius = radius * 0.7f;
            float textX = centerX + (float) (textRadius * Math.cos(Math.toRadians(angle)));
            float textY = centerY + (float) (textRadius * Math.sin(Math.toRadians(angle))) + 5;
            canvas.drawText(String.valueOf(report.getCompletedWorkOrders()), textX - 5, textY, paint);
        }

        if (report.getInProgressWorkOrders() > 0) {
            // Position text in in-progress slice
            float angle = -90 + completed + (inProgress / 2);
            float textRadius = radius * 0.7f;
            float textX = centerX + (float) (textRadius * Math.cos(Math.toRadians(angle)));
            float textY = centerY + (float) (textRadius * Math.sin(Math.toRadians(angle))) + 5;
            canvas.drawText(String.valueOf(report.getInProgressWorkOrders()), textX - 5, textY, paint);
        }

        // Draw legend below pie chart
        paint.setTextSize(11f); // Increased from 9f
        paint.setFakeBoldText(false);
        float legendY = y + height + 20;
        float legendX = x;

        // Completed legend
        if (report.getCompletedWorkOrders() > 0) {
            paint.setColor(Color.parseColor("#D1ECBF"));
            canvas.drawRect(legendX, legendY, legendX + 10, legendY + 10, paint);
            paint.setColor(Color.parseColor("#4E4E58"));
            canvas.drawText("Completed", legendX + 15, legendY + 8, paint);
            legendX += 90;
        }

        // In Progress legend
        if (report.getInProgressWorkOrders() > 0) {
            paint.setColor(Color.parseColor("#FFF1B8"));
            canvas.drawRect(legendX, legendY, legendX + 10, legendY + 10, paint);
            paint.setColor(Color.parseColor("#4E4E58"));
            canvas.drawText("In Progress", legendX + 15, legendY + 8, paint);
        }

        return legendY + 25;
    }

    // Fixed Horizontal Bar Chart - cleaner bars
    private void drawHorizontalBarChartFixed(Canvas canvas, float x, float y, float width, float height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int completed = report.getCompletedWorkOrders();
        int pending = report.getTotalWorkOrders() - completed;
        int maxValue = Math.max(completed, pending); // Minimum scale of 2

        float barHeight = 30;
        float spacing = 20;
        float chartStartX = x + 90;
        float chartWidth = width - 110;

        // Draw scale at top
        paint.setColor(Color.parseColor("#CCCCCC"));
        paint.setTextSize(9f);
        for (int i = 0; i <= maxValue; i++) {
            float scaleX = chartStartX + (i * chartWidth / maxValue);
            canvas.drawText(String.valueOf(i), scaleX - 5, y - 8, paint);

            // Draw vertical grid line
            paint.setColor(Color.parseColor("#F0F0F0"));
            canvas.drawLine(scaleX, y, scaleX, y + (barHeight * 2) + spacing, paint);
            paint.setColor(Color.parseColor("#CCCCCC"));
        }

        // Draw Pending/Other bar (top)
        paint.setColor(Color.parseColor("#4E4E58"));
        paint.setTextSize(11f); // Increased text size
        canvas.drawText("Pending/Other", x, y + barHeight/2 + 4, paint);

        if (pending > 0) {
            float pendingWidth = (float) pending / maxValue * chartWidth;
            paint.setColor(Color.parseColor("#FFF1B8"));
            canvas.drawRect(chartStartX, y, chartStartX + pendingWidth, y + barHeight, paint);

            // Draw value inside or next to bar
            paint.setColor(Color.parseColor("#4E4E58"));
            paint.setTextSize(11f);
            if (pendingWidth > 25) {
                canvas.drawText(String.valueOf(pending), chartStartX + pendingWidth/2 - 5, y + barHeight/2 + 4, paint);
            } else {
                canvas.drawText(String.valueOf(pending), chartStartX + pendingWidth + 5, y + barHeight/2 + 4, paint);
            }
        }

        // Draw Completed bar (bottom)
        float completedY = y + barHeight + spacing;
        paint.setColor(Color.parseColor("#4E4E58"));
        canvas.drawText("Completed", x, completedY + barHeight/2 + 4, paint);

        if (completed > 0) {
            float completedWidth = (float) completed / maxValue * chartWidth;
            paint.setColor(Color.parseColor("#D1ECBF"));
            canvas.drawRect(chartStartX, completedY, chartStartX + completedWidth, completedY + barHeight, paint);

            // Draw value inside or next to bar
            paint.setColor(Color.parseColor("#4E4E58"));
            paint.setTextSize(11f);
            if (completedWidth > 25) {
                canvas.drawText(String.valueOf(completed), chartStartX + completedWidth/2 - 5, completedY + barHeight/2 + 4, paint);
            } else {
                canvas.drawText(String.valueOf(completed), chartStartX + completedWidth + 5, completedY + barHeight/2 + 4, paint);
            }
        }
    }
    // Draw Performance Bar Chart exactly like phone
    private void drawPerformanceBarChartProper(Canvas canvas, float x, float y, float width, float height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Calculate percentages
        float completionRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getCompletedWorkOrders() / report.getTotalWorkOrders() * 100 : 0;
        float inProgressRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getInProgressWorkOrders() / report.getTotalWorkOrders() * 100 : 0;
        float cancelledRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getCancelledWorkOrders() / report.getTotalWorkOrders() * 100 : 0;
        float overdueRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getOverdueWorkOrders() / report.getTotalWorkOrders() * 100 : 0;

        float[] values = {completionRate, inProgressRate, cancelledRate, overdueRate, completionRate};
        String[] labels = {"Done", "Active", "Cancel", "Late", "Score"};
        int[] colors = {
                Color.parseColor("#E3ECFD"),
                Color.parseColor("#D1ECBF"),
                Color.parseColor("#FFF1B8"),
                Color.parseColor("#FFE2E7"),
                Color.parseColor("#FBB1A1")
        };

        // Draw Y-axis scale
        paint.setColor(Color.parseColor("#4E4E58"));
        paint.setTextSize(8f);
        for (int i = 0; i <= 100; i += 20) {
            float scaleY = y + height - (i * height / 100);
            canvas.drawText(String.valueOf(i), x - 15, scaleY + 3, paint);

            // Draw grid line
            paint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawLine(x, scaleY, x + width, scaleY, paint);
            paint.setColor(Color.parseColor("#4E4E58"));
        }

        float barWidth = 60;
        float spacing = (width - (5 * barWidth)) / 6;

        for (int i = 0; i < values.length; i++) {
            float barHeight = (values[i] / 100) * height;
            float barX = x + spacing + (i * (barWidth + spacing));

            // Draw bar
            paint.setColor(colors[i]);
            canvas.drawRect(barX, y + height - barHeight, barX + barWidth, y + height, paint);

            // Draw value on top
            paint.setColor(Color.parseColor("#4E4E58"));
            paint.setTextSize(9f);
            String valueText = String.format("%.1f", values[i]);
            canvas.drawText(valueText, barX + barWidth/4, y + height - barHeight - 8, paint);

            // Draw label below
            paint.setTextSize(8f);
            canvas.drawText(labels[i], barX + barWidth/6, y + height + 15, paint);
        }
    }

    // Chart 1: Work Order Status Distribution (Pie Chart)
    private void setupStatusPieChart(PieChart chart) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Add entries with actual values from your database
        if (report.getCompletedWorkOrders() > 0) {
            entries.add(new PieEntry(report.getCompletedWorkOrders(), "Completed"));
            colors.add(STATUS_COLORS[0]); // Green
        }

        if (report.getInProgressWorkOrders() > 0) {
            entries.add(new PieEntry(report.getInProgressWorkOrders(), "In Progress"));
            colors.add(STATUS_COLORS[1]); // Yellow
        }

        if (report.getCancelledWorkOrders() > 0) {
            entries.add(new PieEntry(report.getCancelledWorkOrders(), "Cancelled"));
            colors.add(STATUS_COLORS[2]); // Orange
        }

        if (report.getOverdueWorkOrders() > 0) {
            entries.add(new PieEntry(report.getOverdueWorkOrders(), "Overdue"));
            colors.add(STATUS_COLORS[3]); // Pink
        }

        // If no entries, show a placeholder
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No Data Available"));
            colors.add(STATUS_COLORS[4]); // Blue
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.parseColor("#4E4E58"));
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);

        // Show actual count values instead of percentages
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.setCenterTextSize(16f);
        chart.setCenterTextColor(Color.parseColor("#4E4E58"));
        chart.setHoleRadius(35f);
        chart.setTransparentCircleRadius(40f);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextSize(12f);
        chart.getLegend().setTextColor(Color.parseColor("#4E4E58"));
        chart.setEntryLabelTextSize(11f);
        chart.setEntryLabelColor(Color.parseColor("#4E4E58"));
        chart.invalidate();
    }

    // Chart 2: Completion vs Non-Completion (Horizontal Bar Chart)
    private void setupCompletionBarChart(HorizontalBarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        entries.add(new BarEntry(0, report.getCompletedWorkOrders()));
        labels.add("Completed");
        colors.add(STATUS_COLORS[0]);

        int nonCompleted = report.getTotalWorkOrders() - report.getCompletedWorkOrders();
        entries.add(new BarEntry(1, nonCompleted));
        labels.add("Pending/Other");
        colors.add(STATUS_COLORS[1]);

        BarDataSet dataSet = new BarDataSet(entries, "Completion Analysis");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#4E4E58"));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        chart.setData(data);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.invalidate();
    }

    private void setupPerformanceChart(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Calculate all percentages
        float completionRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getCompletedWorkOrders() / report.getTotalWorkOrders() * 100 : 0;

        float inProgressRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getInProgressWorkOrders() / report.getTotalWorkOrders() * 100 : 0;

        float cancelledRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getCancelledWorkOrders() / report.getTotalWorkOrders() * 100 : 0;

        float overdueRate = report.getTotalWorkOrders() > 0 ?
                (float) report.getOverdueWorkOrders() / report.getTotalWorkOrders() * 100 : 0;

        float efficiency = completionRate;

        // Use much shorter labels to prevent overlap
        entries.add(new BarEntry(0, completionRate));
        labels.add("Done");  // Shorter than "Complete"
        colors.add(PERFORMANCE_COLORS[0]);

        entries.add(new BarEntry(1, inProgressRate));
        labels.add("Active");  // Shorter than "Progress"
        colors.add(PERFORMANCE_COLORS[1]);

        entries.add(new BarEntry(2, cancelledRate));
        labels.add("Cancel");  // Shorter than "Cancelled"
        colors.add(PERFORMANCE_COLORS[2]);

        entries.add(new BarEntry(3, overdueRate));
        labels.add("Late");  // Shorter than "Overdue"
        colors.add(PERFORMANCE_COLORS[3]);

        entries.add(new BarEntry(4, efficiency));
        labels.add("Score");  // Shorter than "Efficiency"
        colors.add(PERFORMANCE_COLORS[4]);

        BarDataSet dataSet = new BarDataSet(entries, "Performance Metrics");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#4E4E58"));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f); // Even narrower bars for more space

        chart.setData(data);

        // Configure X-axis with maximum spacing
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setLabelRotationAngle(0f); // No rotation with short labels
        chart.getXAxis().setTextSize(10f);
        chart.getXAxis().setAvoidFirstLastClipping(true);
        chart.getXAxis().setYOffset(8f);
        chart.getXAxis().setSpaceMin(1f); // Maximum space around labels
        chart.getXAxis().setSpaceMax(1f);
        chart.getXAxis().setCenterAxisLabels(false);
        chart.getXAxis().setAxisMinimum(-0.5f); // Add padding on left
        chart.getXAxis().setAxisMaximum(4.5f); // Add padding on right

        // Configure Y-axis
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisRight().setEnabled(false);

        // Chart styling with more spacing
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawValueAboveBar(true);
        chart.setFitBars(true);

        // Extra padding for labels
        chart.setExtraOffsets(20, 15, 20, 40);
        chart.invalidate();
    }
}