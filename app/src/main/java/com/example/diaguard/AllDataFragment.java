package com.example.diaguard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diaguard.db.GlucoseDao;
import com.example.diaguard.db.GlucoseDatabase;
import com.example.diaguard.db.GlucoseEntry;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllDataFragment extends Fragment {
    private CandleStickChart fullGlucoseChart;
    private RecyclerView historyList;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_data, container, false);
        fullGlucoseChart = view.findViewById(R.id.fullGlucoseChart);
        historyList = view.findViewById(R.id.historyList);

        GlucoseDao glucoseDao = GlucoseDatabase.getInstance(requireContext()).glucoseDao();

        LiveData<List<GlucoseEntry>> glucoseLiveData = glucoseDao.getAllEntriesLive();
        glucoseLiveData.observe(getViewLifecycleOwner(), glucoseEntries -> {
            setupChart(glucoseEntries);
            setupHistoryList(glucoseEntries);
        });

        return view;
    }


    private List<AggregatedData> aggregateDataDaily(List<GlucoseEntry> data) {
        List<AggregatedData> aggregated = new ArrayList<>();

        if (data.isEmpty()) return aggregated;

        long currentDay = -1;
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;

        for (GlucoseEntry entry : data) {
            // Округляем до  начала дня
            long dayTimestamp = entry.timestamp - (entry.timestamp % (24 * 60 * 60 * 1000));
            if (dayTimestamp != currentDay) {
                if (currentDay != -1) {
                    aggregated.add(new AggregatedData(currentDay, min, max));
                }
                currentDay = dayTimestamp;
                min = entry.level;
                max = entry.level;
            } else {
                min = Math.min(min, entry.level);
                max = Math.max(max, entry.level);
            }
        }

        aggregated.add(new AggregatedData(currentDay, min, max));
        return aggregated;
    }

    private static final int DAYS_TO_SHOW = 7;
    private static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;

    /**
     * Пример структуры для "агрегированных" данных одного дня.
     */
    private static class AggregatedData {
        long day;   // timestamp начала дня (округлённый)
        float min;
        float max;

        AggregatedData(long day, float min, float max) {
            this.day = day;
            this.min = min;
            this.max = max;
        }
    }

    /**
     * Основной метод постороения свечного графика, который всегда показывает
     * последние N (DAYS_TO_SHOW) дней, даже если нет данных.
     */
    private void setupChart(List<GlucoseEntry> allEntries) {
        // 1) Определим "конечный" день (today), округлив текущий момент
        long now = System.currentTimeMillis();
        long endDay = truncateToDay(now);
        // 2) "Начальный" день - (N-1) дней назад
        long startDay = endDay - (DAYS_TO_SHOW - 1) * DAY_MILLIS;

        // 3) Агрегируем данные по дням, но игнорируем те, что старше startDay
        //    и те, что моложе endDay + 1 день (на всякий случай).
        //    (Если нужно строго включительно до endDay, можно не делать + DAY_MILLIS)
        Map<Long, AggregatedData> dayMap = aggregateDailyInRange(allEntries, startDay, endDay + DAY_MILLIS);

        // 4) Создаём список AggregatedData на каждый день [startDay..endDay],
        //    заполняя пропущенные дни нулями.
        List<AggregatedData> filled = fillDays(dayMap, startDay, endDay);

        // 5) Формируем CandleEntry для каждого дня (index = 0..N-1)
        List<CandleEntry> candleEntries = new ArrayList<>();
        for (int i = 0; i < filled.size(); i++) {
            AggregatedData ad = filled.get(i);
            candleEntries.add(new CandleEntry(i, ad.max, ad.min, ad.min, ad.max));
        }

        // 6) Создаём DataSet и привязываем к графику
        CandleDataSet dataSet = getCandleDataSet(candleEntries);
        CandleData candleData = new CandleData(dataSet);

        fullGlucoseChart.setData(candleData);
        fullGlucoseChart.setScaleEnabled(true);
        fullGlucoseChart.setDragEnabled(true);
        fullGlucoseChart.setPinchZoom(true);
        fullGlucoseChart.invalidate();

        // 7) Настраиваем ось X (метки - даты)
        setupXAxis(filled);

        // 8) Настраиваем ось Y (гарантируем MIN_THRESHOLD / MAX_THRESHOLD)
        setupYAxis(filled);
    }

    /**
     * Агрегируем данные allEntries по дням, но берём только записи
     * в диапазоне [startDay..endDay).
     */
    private Map<Long, AggregatedData> aggregateDailyInRange(List<GlucoseEntry> allEntries, long startDay, long endDay) {

        Map<Long, AggregatedData> map = new HashMap<>();
        for (GlucoseEntry e : allEntries) {
            // Фильтруем по дате
            if (e.timestamp < startDay || e.timestamp >= endDay) {
                continue; // вне нужного диапазона
            }
            // Округляем timestamp до начала суток
            long dayTs = truncateToDay(e.timestamp);

            AggregatedData agg = map.get(dayTs);
            if (agg == null) {
                agg = new AggregatedData(dayTs, e.level, e.level);
                map.put(dayTs, agg);
            } else {
                // Обновляем min/max
                if (e.level < agg.min) agg.min = e.level;
                if (e.level > agg.max) agg.max = e.level;
            }
        }
        return map;
    }

    /**
     * Создаём упорядоченный список AggregatedData на интервал [startDay..endDay],
     * где для отсутствующих дней min=0, max=0.
     */
    private List<AggregatedData> fillDays(Map<Long, AggregatedData> dayMap, long startDay, long endDay) {
        List<AggregatedData> result = new ArrayList<>();

        long dayPointer = startDay;
        while (dayPointer <= endDay) {
            AggregatedData found = dayMap.get(dayPointer);
            if (found == null) {
                // нет записей за этот день -> 0,0
                result.add(new AggregatedData(dayPointer, 0f, 0f));
            } else {
                result.add(found);
            }
            dayPointer += DAY_MILLIS;
        }
        return result;
    }

    /**
     * "Округляем" миллисекунды до начала суток
     * (например, 2025-03-02 15:32 → 2025-03-02 00:00).
     */
    private long truncateToDay(long ts) {
        return ts - (ts % DAY_MILLIS);
    }

    /**
     * Настраиваем ось X:
     * - индексы свечей (0..N-1)
     * - метки дат из AggregatedData.day в формате "dd-MM"
     */
    private void setupXAxis(List<AggregatedData> finalData) {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM", Locale.getDefault());

        XAxis xAxis = fullGlucoseChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < finalData.size()) {
                    long dayTs = finalData.get(index).day;
                    return sdf.format(new Date(dayTs));
                }
                return "";
            }
        });
    }

    /**
     * Настраиваем ось Y, чтобы минимум и максимум
     * включали наши пороги (MIN_THRESHOLD, MAX_THRESHOLD),
     * а если в данных есть что-то за пределами, подстраиваемся под них.
     */
    private void setupYAxis(List<AggregatedData> data) {
        float realMin = Float.MAX_VALUE;
        float realMax = Float.MIN_VALUE;
        for (AggregatedData ad : data) {
            if (ad.min < realMin && ad.min > 0) realMin = ad.min;
            if (ad.max > realMax) realMax = ad.max;
        }

        float minT = (float) Util.MIN_THRESHOLD;
        float maxT = (float) Util.MAX_THRESHOLD;

        float yMin = Math.min(realMin, minT) - 1;
        float yMax = Math.max(realMax, maxT) + 1;

        YAxis leftAxis = fullGlucoseChart.getAxisLeft();
        leftAxis.setAxisMinimum(yMin);
        leftAxis.setAxisMaximum(yMax);

        leftAxis.addLimitLine(Util.createLimitLine(minT));
        leftAxis.addLimitLine(Util.createLimitLine(maxT));

        fullGlucoseChart.getAxisRight().setEnabled(false);
    }

    @NonNull
    private static CandleDataSet getCandleDataSet(List<CandleEntry> candleEntries) {
        CandleDataSet dataSet = new CandleDataSet(candleEntries, "Daily Glucose Candles");
        dataSet.setShadowColor(android.graphics.Color.GRAY);
        dataSet.setShadowWidth(1f);
        dataSet.setDecreasingColor(android.graphics.Color.RED);
        dataSet.setDecreasingPaintStyle(android.graphics.Paint.Style.FILL);
        dataSet.setIncreasingColor(android.graphics.Color.GREEN);
        dataSet.setIncreasingPaintStyle(android.graphics.Paint.Style.FILL);
        dataSet.setNeutralColor(android.graphics.Color.BLUE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getCandleLabel(CandleEntry entry) {
                if (entry.getHigh() == 0f && entry.getLow() == 0f) {
                    return "";
                }
                return String.format(Locale.getDefault(), "%.1f/%.1f", entry.getLow(), entry.getHigh());
            }
        });
        return dataSet;
    }

    private void setupHistoryList(List<GlucoseEntry> data) {
        historyList.setLayoutManager(new LinearLayoutManager(getContext()));

        GlucoseHistoryAdapter adapter = new GlucoseHistoryAdapter(data, new OnItemActionListener() {
            @Override
            public void onDelete(GlucoseEntry entry) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    GlucoseDao dao = GlucoseDatabase.getInstance(requireContext()).glucoseDao();
                    dao.deleteEntry(entry.timestamp);
                });
            }

            @Override
            public void onNoteModify(GlucoseEntry entry) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Note");

                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setLines(5);
                input.setHint("Enter the note");

                if (entry.note != null) {
                    input.setText(entry.note);
                }
                builder.setView(input);

                builder.setPositiveButton("Apply", (dialog, which) -> {
                    entry.note = input.getText().toString();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        GlucoseDao dao = GlucoseDatabase.getInstance(requireContext()).glucoseDao();
                        dao.updateEntry(entry);
                    });
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });
        historyList.setAdapter(adapter);
    }

}
