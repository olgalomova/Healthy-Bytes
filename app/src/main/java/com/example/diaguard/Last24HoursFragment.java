package com.example.diaguard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.diaguard.db.GlucoseDao;
import com.example.diaguard.db.GlucoseDatabase;
import com.example.diaguard.db.GlucoseEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Last24HoursFragment extends Fragment {
    private static final double MIN_THRESHOLD = 3.9;
    private static final double MAX_THRESHOLD = 7.8;

    private LineChart glucoseChart;
    private TextView latestMeasurementTime;
    private TextView latestMeasurementValue;
    private View latestMeasurementBlock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_last_24_hours, container, false);
        initializeViews(view);

        GlucoseDao dao = GlucoseDatabase.getInstance(requireContext()).glucoseDao();
        dao.getEntriesLast24Hours().observe(getViewLifecycleOwner(), data -> {
                    updateLatestMeasurement(data);
                    setupChart(data);
                }
        );

        return view;
    }

    private void initializeViews(View view) {
        glucoseChart = view.findViewById(R.id.glucoseChart);
        latestMeasurementTime = view.findViewById(R.id.latestMeasurementTime);
        latestMeasurementValue = view.findViewById(R.id.latestMeasurementValue);
        latestMeasurementBlock = view.findViewById(R.id.latestMeasurementBlock);
    }

    private void updateLatestMeasurement(List<GlucoseEntry> sortedEntries) {
        if (!sortedEntries.isEmpty()) {
            GlucoseEntry lastEntry = sortedEntries.get(sortedEntries.size() - 1);
            latestMeasurementTime.setText(formatTimestamp(lastEntry.timestamp));
            latestMeasurementValue.setText(String.format(Locale.getDefault(), "%.1f", lastEntry.level));
            latestMeasurementBlock.setBackgroundColor(Util.getGlucoseColor(lastEntry.level));
        }
    }

    private static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void setupChart(List<GlucoseEntry> data) {
        List<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            float value = data.get(i).level;
            chartEntries.add(new Entry(i, value));
        }

        LineDataSet dataSet = new LineDataSet(chartEntries, "Glucose Levels");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(1.5f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setDrawValues(true);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        LineData lineData = new LineData(dataSet);
        glucoseChart.setData(lineData);
        glucoseChart.invalidate();

        setupXAxis(data);
        setupYAxis(data);
    }

    private void setupYAxis(List<GlucoseEntry> data) {
        YAxis leftAxis = glucoseChart.getAxisLeft();
        leftAxis.setGranularity(0.1f);
        leftAxis.setTextColor(Color.BLACK);

        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        for (GlucoseEntry entry : data) {
            float value = entry.level;
            if (value < minValue) minValue = value;
            if (value > maxValue) maxValue = value;
        }

        float yMin = Math.min(minValue, (float) MIN_THRESHOLD) - 1;
        float yMax = Math.max(maxValue, (float) MAX_THRESHOLD) + 1;

        leftAxis.setAxisMinimum(yMin);
        leftAxis.setAxisMaximum(yMax);

        leftAxis.addLimitLine(Util.createLimitLine((float) MIN_THRESHOLD));
        leftAxis.addLimitLine(Util.createLimitLine((float) MAX_THRESHOLD));

        glucoseChart.getAxisRight().setEnabled(false);
    }

    private void setupXAxis(List<GlucoseEntry> sortedEntries) {
        XAxis xAxis = glucoseChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < sortedEntries.size()) {
                    return formatTimestamp(sortedEntries.get(index).timestamp);
                }
                return "";
            }
        });
    }
}
