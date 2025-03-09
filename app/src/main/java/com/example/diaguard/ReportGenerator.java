package com.example.diaguard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import com.example.diaguard.db.GlucoseEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportGenerator {

    /**
     * Генерирует PDF‑отчет по данным измерений.
     *
     * @param context    контекст приложения
     * @param entries    список записей GlucoseEntry из базы данных
     * @param outputPath путь для сохранения PDF‑файла
     */
    @SuppressLint("DefaultLocale")
    public static void generatePdfReport(Context context, List<GlucoseEntry> entries, String outputPath) {
        // Создаем новый PDF документ
        PdfDocument document = new PdfDocument();
        // Определяем размеры страницы (например, A4 ~ 595x842 точек)
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        // Начинаем первую страницу
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);
        int x = 20;
        int y = 30;

        // Заголовок отчета
        paint.setFakeBoldText(true);
        canvas.drawText("Glucose Measurement Report", x, y, paint);
        paint.setFakeBoldText(false);
        y += 30;

        // Раздел: Измерения с отклонениями (выше или ниже пороговых значений)
        canvas.drawText("Measurements exceeding threshold:", x, y, paint);
        y += 20;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        boolean foundOutOfRange = false;
        for (GlucoseEntry entry : entries) {
            if (entry.level < Util.MIN_THRESHOLD || entry.level > Util.MAX_THRESHOLD) {
                String dateStr = sdf.format(new Date(entry.timestamp));
                String note = (entry.note != null && !entry.note.isEmpty()) ? entry.note : "No notes";
                String line = String.format(Locale.getDefault(), "%s – Level: %.1f, Note: %s",
                        dateStr, entry.level, note);
                canvas.drawText(line, x, y, paint);
                y += 15;
                foundOutOfRange = true;
                // Если строк становится много, можно добавить логику переноса на новую страницу (опущено для простоты)
            }
        }
        if (!foundOutOfRange) {
            canvas.drawText("No measurements with deviations", x, y, paint);
            y += 15;
        }
        y += 20;

        // Раздел: Анализ временных интервалов
        canvas.drawText("Time Interval Analysis:", x, y, paint);
        y += 20;
        int morningCount = 0;   // 06:00–12:00
        int afternoonCount = 0; // 12:00–18:00
        int eveningCount = 0;   // 18:00–24:00
        int nightCount = 0;     // 00:00–06:00

        for (GlucoseEntry entry : entries) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(entry.timestamp);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 6 && hour < 12) {
                morningCount++;
            } else if (hour >= 12 && hour < 18) {
                afternoonCount++;
            } else if (hour >= 18) {
                eveningCount++;
            } else {
                nightCount++;
            }
        }

        canvas.drawText(String.format("Morning (06:00–12:00): %d measurements", morningCount), x, y, paint);
        y += 15;
        canvas.drawText(String.format("Afternoon (12:00–18:00): %d measurements", afternoonCount), x, y, paint);
        y += 15;
        canvas.drawText(String.format("Evening (18:00–24:00): %d measurements", eveningCount), x, y, paint);
        y += 15;
        canvas.drawText(String.format("Night (00:00–06:00): %d measurements", nightCount), x, y, paint);
        y += 30;

        // Дополнительный раздел: Общая статистика (например, среднее, минимум, максимум)
        if (!entries.isEmpty()) {
            float sum = 0f;
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            for (GlucoseEntry entry : entries) {
                sum += entry.level;
                if (entry.level < min) min = entry.level;
                if (entry.level > max) max = entry.level;
            }
            float avg = sum / entries.size();
            canvas.drawText(String.format(Locale.getDefault(), "Average level: %.1f", avg), x, y, paint);
            y += 15;
            canvas.drawText(String.format(Locale.getDefault(), "Minimum level: %.1f", min), x, y, paint);
            y += 15;
            canvas.drawText(String.format(Locale.getDefault(), "Maximum level: %.1f", max), x, y, paint);
            y += 15;
        }

        // Завершаем страницу
        document.finishPage(page);

        // Записываем PDF в файл
        try {
            File file = new File(outputPath);
            document.writeTo(new FileOutputStream(file));
            Log.d("ReportGenerator", "Report saved: " + outputPath);
        } catch (IOException e) {
            Log.e("ReportGenerator", "Error saving report: ", e);
        }
        document.close();
    }
}
