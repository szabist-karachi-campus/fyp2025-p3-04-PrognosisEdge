package com.example.prognosisedge;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.RectF;
import android.os.Environment;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prognosisedge.models.MachineReport;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SS_ReportDetailedFragment extends Fragment {

    private static final String ARG_REPORT = "report";
    private MachineReport report;

    // Colors using your pastel theme
    private final int[] FAILURE_COLORS = {
            Color.parseColor("#FBB1A1"),   // Orange for Detergent Low
            Color.parseColor("#FFE2E7"),   // Pink for Pressure Drop
            Color.parseColor("#FFF1B8"),   // Yellow for Temperature Anomaly
            Color.parseColor("#E3ECFD")    // Blue for Water Flow Issue
    };

    private final int[] SENSOR_COLORS = {
            Color.parseColor("#E3ECFD"),   // Blue for Flow Rate
            Color.parseColor("#D1ECBF"),   // Green for Pressure
            Color.parseColor("#FFF1B8"),   // Yellow for Detergent
            Color.parseColor("#FFE2E7"),   // Pink for Hydraulic
            Color.parseColor("#FBB1A1"),   // Orange for Temp Fluctuation
            Color.parseColor("#FD6746"),   // Accent color for Oil Temp
            Color.parseColor("#64818C")    // Icon color for Coolant
    };

    public static SS_ReportDetailedFragment newInstance(MachineReport report) {
        SS_ReportDetailedFragment fragment = new SS_ReportDetailedFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPORT, (Serializable) report);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ss_reportdetailedfragment, container, false);

        report = (MachineReport) getArguments().getSerializable(ARG_REPORT);
        if (report == null) return view;

        // Back navigation
        ImageView back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Text setup
        ((TextView) view.findViewById(R.id.report_title)).setText("Machine: " + report.getSerialNumber());
        ((TextView) view.findViewById(R.id.stat_total_readings)).setText("Total Readings: " + report.getTotalReadings());
        ((TextView) view.findViewById(R.id.stat_failure)).setText("Failures: " + report.getFailureCount());
        ((TextView) view.findViewById(R.id.stat_no_failure)).setText("No Failures: " + report.getNoFailureCount());

        // Charts
        setupFailureChart(view.findViewById(R.id.failure_type_chart));
        setupSensorChart(view.findViewById(R.id.avg_values_chart));

        // Setup FAB for PDF export
        FloatingActionButton fabExportPdf = view.findViewById(R.id.fab_export_pdf);
        fabExportPdf.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FD6746")));
        fabExportPdf.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        fabExportPdf.setOnClickListener(v -> exportToPdf());

        return view;
    }

    // PDF Export Method for SS Reports
    // Also update the main exportToPdf method to give more space for the sensor chart
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
            paint.setTextSize(22f);
            paint.setColor(Color.parseColor("#4E4E58"));
            paint.setFakeBoldText(true);
            canvas.drawText("Machine Analytics Report", 50, 40, paint);

            // Add report details
            paint.setTextSize(12f);
            paint.setFakeBoldText(false);
            int yPosition = 75;

            canvas.drawText("Machine: " + report.getSerialNumber(), 50, yPosition, paint);
            yPosition += 18;
            canvas.drawText("Generated on: " + new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()), 50, yPosition, paint);
            yPosition += 28;

            // Add summary statistics
            paint.setTextSize(16f);
            paint.setFakeBoldText(true);
            canvas.drawText("Asset Summary", 50, yPosition, paint);
            yPosition += 25;

            paint.setTextSize(12f);
            paint.setFakeBoldText(false);
            canvas.drawText("Total Readings: " + report.getTotalReadings(), 70, yPosition, paint);
            canvas.drawText("Failures: " + report.getFailureCount(), 250, yPosition, paint);
            canvas.drawText("No Failures: " + report.getNoFailureCount(), 400, yPosition, paint);
            yPosition += 35;

            // Chart titles
            paint.setTextSize(14f);
            paint.setFakeBoldText(true);
            canvas.drawText("Failure Types Breakdown", 50, yPosition, paint);
            yPosition += 20;

            // Draw Failure Types Chart (horizontal bar chart)
            float failureChartBottom = drawFailureTypesChart(canvas, 50, yPosition, 480, 140); // Reduced height
            yPosition = (int) failureChartBottom + 25;

            // Sensor Averages Chart
            paint.setTextSize(14f);
            paint.setFakeBoldText(true);
            paint.setColor(Color.parseColor("#4E4E58"));
            canvas.drawText("Sensor Averages", 50, yPosition, paint);
            yPosition += 20;

            drawSensorAveragesChart(canvas, 50, yPosition, 480, 220); // Increased height for labels

            // Add footer
            paint.setTextSize(10f);
            paint.setColor(Color.GRAY);
            paint.setFakeBoldText(false);
            canvas.drawText("Generated by PrognosisEdge App", 50, 820, paint);

            pdfDocument.finishPage(page);

            // Save the PDF (rest remains the same)
            String fileName = "Machine_Report_" + report.getSerialNumber() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

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

    // Draw Failure Types Horizontal Bar Chart
    private float drawFailureTypesChart(Canvas canvas, float x, float y, float width, float height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Failure data
        int[] failureCounts = {
                report.getDetergentLow(),
                report.getPressureDrop(),
                report.getTemperatureAnomaly(),
                report.getWaterFlowIssue()
        };

        String[] failureLabels = {
                "Detergent Low",
                "Pressure Drop",
                "Temp Anomaly",
                "Water Flow"
        };

        int[] failureColors = {
                Color.parseColor("#FBB1A1"),   // Orange
                Color.parseColor("#FFE2E7"),   // Pink
                Color.parseColor("#FFF1B8"),   // Yellow
                Color.parseColor("#E3ECFD")    // Blue
        };

        // Find max value for scaling
        int maxValue = 0;
        for (int count : failureCounts) {
            maxValue = Math.max(maxValue, count);
        }
        if (maxValue == 0) maxValue = 5; // Minimum scale

        float barHeight = 25;
        float spacing = 15;
        float chartStartX = x + 120;
        float chartWidth = width - 140;

        // Draw scale at top
        paint.setColor(Color.parseColor("#CCCCCC"));
        paint.setTextSize(9f);
        for (int i = 0; i <= maxValue; i += Math.max(1, maxValue/5)) {
            float scaleX = chartStartX + (i * chartWidth / maxValue);
            canvas.drawText(String.valueOf(i), scaleX - 5, y - 8, paint);

            // Draw vertical grid line
            paint.setColor(Color.parseColor("#F0F0F0"));
            canvas.drawLine(scaleX, y, scaleX, y + (barHeight + spacing) * failureCounts.length, paint);
            paint.setColor(Color.parseColor("#CCCCCC"));
        }

        // Draw bars
        for (int i = 0; i < failureCounts.length; i++) {
            float barY = y + (i * (barHeight + spacing));

            // Draw label
            paint.setColor(Color.parseColor("#4E4E58"));
            paint.setTextSize(11f);
            canvas.drawText(failureLabels[i], x, barY + barHeight/2 + 4, paint);

            // Draw bar if value > 0
            if (failureCounts[i] > 0) {
                float barWidth = (float) failureCounts[i] / maxValue * chartWidth;
                paint.setColor(failureColors[i]);
                canvas.drawRect(chartStartX, barY, chartStartX + barWidth, barY + barHeight, paint);

                // Draw value
                paint.setColor(Color.parseColor("#4E4E58"));
                paint.setTextSize(11f);
                if (barWidth > 25) {
                    canvas.drawText(String.valueOf(failureCounts[i]), chartStartX + barWidth/2 - 5, barY + barHeight/2 + 4, paint);
                } else {
                    canvas.drawText(String.valueOf(failureCounts[i]), chartStartX + barWidth + 5, barY + barHeight/2 + 4, paint);
                }
            } else {
                // Draw "0" for zero values
                paint.setColor(Color.parseColor("#CCCCCC"));
                canvas.drawText("0", chartStartX + 5, barY + barHeight/2 + 4, paint);
            }
        }

        return y + (barHeight + spacing) * failureCounts.length + 10;
    }

    // Draw Sensor Averages Bar Chart
    // Updated drawSensorAveragesChart method with fixed label positioning
    private void drawSensorAveragesChart(Canvas canvas, float x, float y, float width, float height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Sensor data
        double[] sensorValues = {
                report.getAvgFlow(),
                report.getAvgPressureStability(),
                report.getAvgDetergent(),
                report.getAvgHydraulic() / 100.0, // Scale down hydraulic
                report.getAvgTempFluctuation(),
                report.getAvgOilTemp(),
                report.getAvgCoolant()
        };

        String[] sensorLabels = {
                "Flow",
                "Pressure",
                "Detergent",
                "Hydraulic",
                "Temp Fluct",
                "Oil Temp",
                "Coolant"
        };

        int[] sensorColors = {
                Color.parseColor("#E3ECFD"),   // Blue
                Color.parseColor("#D1ECBF"),   // Green
                Color.parseColor("#FFF1B8"),   // Yellow
                Color.parseColor("#FFE2E7"),   // Pink
                Color.parseColor("#FBB1A1"),   // Orange
                Color.parseColor("#FD6746"),   // Accent
                Color.parseColor("#64818C")    // Icon color
        };

        // Find max value for scaling
        double maxValue = 0;
        for (double value : sensorValues) {
            maxValue = Math.max(maxValue, value);
        }
        if (maxValue == 0) maxValue = 100; // Default scale

        // Reduce chart height to make room for labels
        float chartHeight = height - 40; // Leave 40px for labels at bottom

        // Draw Y-axis scale
        paint.setColor(Color.parseColor("#4E4E58"));
        paint.setTextSize(8f);
        DecimalFormat df = new DecimalFormat("#.#");
        for (int i = 0; i <= 5; i++) {
            double scaleValue = (maxValue / 5) * i;
            float scaleY = y + chartHeight - (i * chartHeight / 5);
            canvas.drawText(df.format(scaleValue), x - 25, scaleY + 3, paint);

            // Draw grid line
            paint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawLine(x, scaleY, x + width, scaleY, paint);
            paint.setColor(Color.parseColor("#4E4E58"));
        }

        float barWidth = 50;
        float spacing = (width - (sensorValues.length * barWidth)) / (sensorValues.length + 1);

        for (int i = 0; i < sensorValues.length; i++) {
            float barHeight = (float) (sensorValues[i] / maxValue * chartHeight);
            float barX = x + spacing + (i * (barWidth + spacing));

            // Draw bar
            paint.setColor(sensorColors[i]);
            canvas.drawRect(barX, y + chartHeight - barHeight, barX + barWidth, y + chartHeight, paint);

            // Draw value on top of bar
            paint.setColor(Color.parseColor("#4E4E58"));
            paint.setTextSize(8f);
            String valueText = df.format(sensorValues[i]);
            // Center the text above the bar
            float textWidth = paint.measureText(valueText);
            canvas.drawText(valueText, barX + (barWidth - textWidth) / 2, y + chartHeight - barHeight - 8, paint);

            // Draw label below chart area (no rotation, smaller text)
            paint.setTextSize(9f);
            float labelWidth = paint.measureText(sensorLabels[i]);
            float labelX = barX + (barWidth - labelWidth) / 2; // Center under bar
            float labelY = y + chartHeight + 15; // Below the chart

            canvas.drawText(sensorLabels[i], labelX, labelY, paint);
        }
    }
    private void setupFailureChart(HorizontalBarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Always add all failure types, even if count is 0
        entries.add(new BarEntry(0, report.getDetergentLow()));
        labels.add("Detergent Low");
        colors.add(FAILURE_COLORS[0]);

        entries.add(new BarEntry(1, report.getPressureDrop()));
        labels.add("Pressure Drop");
        colors.add(FAILURE_COLORS[1]);

        entries.add(new BarEntry(2, report.getTemperatureAnomaly()));
        labels.add("Temp Anomaly");
        colors.add(FAILURE_COLORS[2]);

        entries.add(new BarEntry(3, report.getWaterFlowIssue()));
        labels.add("Water Flow");
        colors.add(FAILURE_COLORS[3]);

        if (entries.isEmpty()) {
            // Hide chart if no data
            chart.setVisibility(View.GONE);
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Failure Types");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#4E4E58")); // Using your textColor

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        chart.setData(data);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setDrawGridLines(false);

        // Configure Y-axis
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        // Chart styling
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);

        chart.invalidate();
    }

    private void setupSensorChart(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Add all sensor readings as separate bars
        entries.add(new BarEntry(0, (float) report.getAvgFlow()));
        labels.add("Flow Rate");
        colors.add(SENSOR_COLORS[0]);

        entries.add(new BarEntry(1, (float) report.getAvgPressureStability()));
        labels.add("Pressure");
        colors.add(SENSOR_COLORS[1]);

        entries.add(new BarEntry(2, (float) report.getAvgDetergent()));
        labels.add("Detergent");
        colors.add(SENSOR_COLORS[2]);

        // Scale down hydraulic pressure by dividing by 100 for better visualization
        entries.add(new BarEntry(3, (float) (report.getAvgHydraulic() / 100.0)));
        labels.add("Hydraulic (/100)");
        colors.add(SENSOR_COLORS[3]);

        entries.add(new BarEntry(4, (float) report.getAvgTempFluctuation()));
        labels.add("Temp Fluct");
        colors.add(SENSOR_COLORS[4]);

        entries.add(new BarEntry(5, (float) report.getAvgOilTemp()));
        labels.add("Oil Temp");
        colors.add(SENSOR_COLORS[5]);

        entries.add(new BarEntry(6, (float) report.getAvgCoolant()));
        labels.add("Coolant");
        colors.add(SENSOR_COLORS[6]);

        BarDataSet dataSet = new BarDataSet(entries, "Sensor Averages");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#4E4E58")); // Using your textColor

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        chart.setData(data);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setLabelRotationAngle(-45f); // Rotate labels for better readability

        // Configure Y-axis
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        // Chart styling
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(true);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setFitBars(true);

        // Add some padding
        chart.setExtraOffsets(5, 10, 5, 20);

        chart.invalidate();
    }
}