package com.example.diaguard;

import android.app.AlertDialog;
import android.content.Context;

import com.example.diaguard.models.Measurement;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AnalysisResultDialog {

    public static void analyzeAndShowDialog(Context context, List<Measurement> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            showErrorDialog(context, "No data available for analysis.");
            return;
        }

        // Period (difference between first and last entry date)
        LocalDate firstDate = measurements.get(0).getEntryDate();
        LocalDate lastDate = measurements.get(measurements.size() - 1).getEntryDate();
        long period = ChronoUnit.DAYS.between(firstDate, lastDate);
        if (period == 0) period = 1; // Avoid division by zero

        // Total days with data
        int totalDaysWithData = measurements.size();

        // Initialize variables
        double timeInRange = 0;
        double totalGlucoseSum = 0;
        double totalGlucoseEntries = 0;
        double stddevGlucoseSum = 0;
        int stddevEntries = 0;
        double totalBasalInsulin = 0;
        double totalBolusInsulin = 0;
        double totalInsulinDose = 0;

        // Process measurements
        for (Measurement m : measurements) {
            if (m.getTimeInRangeNormal() != null) timeInRange += m.getTimeInRangeNormal();
            if (m.getTimeInRangeLow() != null) timeInRange += m.getTimeInRangeLow();
            if (m.getTimeInRangeHigh() != null) timeInRange += m.getTimeInRangeHigh();
            if (m.getAverageGlucose() != null) {
                totalGlucoseSum += m.getAverageGlucose();
                totalGlucoseEntries++;
            }
            if (m.getStddevGlucose() != null) {
                stddevGlucoseSum += m.getStddevGlucose();
                stddevEntries++;
            }
            if (m.getBasalInsulin() != null) totalBasalInsulin += m.getBasalInsulin();
            if (m.getBolusInsulin() != null) totalBolusInsulin += m.getBolusInsulin();
            if (m.getDailyInsulinDose() != null) totalInsulinDose += m.getDailyInsulinDose();
        }

        // Convert time in range to hours
        timeInRange /= 60;

        // Compute averages
        double avgGlucose = totalGlucoseEntries > 0 ? totalGlucoseSum / totalGlucoseEntries : 0;
        double stddevGlucose = stddevEntries > 0 ? stddevGlucoseSum / stddevEntries : 0;

        // Compute GMI
        double gmi = 3.31 + 0.02392 * avgGlucose;

        // Compute sensor usage
        double sensorUsage = ((double) totalDaysWithData / Math.max(period, 1)) * 100;

        // Formatting result
        String analysisResult = String.format(
                "Period: %d days\n" +
                        "Sensor Usage: %.2f%%\n" +
                        "Time in Range: %.2f hours\n" +
                        "Average Glucose: %.2f mg/dL\n" +
                        "Glucose Variability: %.2f\n" +
                        "Glucose Management Indicator (GMI): %.2f\n" +
                        "Total Dose of Insulin: %.2f IU\n" +
                        "Total Basal Insulin: %.2f IU\n" +
                        "Total Bolus Insulin: %.2f IU",
                period, sensorUsage, timeInRange, avgGlucose, stddevGlucose, gmi, totalInsulinDose, totalBasalInsulin, totalBolusInsulin
        );

        // Show result
        showAnalysisDialog(context, analysisResult);
    }

    private static void showAnalysisDialog(Context context, String analysisMessage) {
        new AlertDialog.Builder(context)
                .setTitle("Analysis Results")
                .setMessage(analysisMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showErrorDialog(Context context, String errorMessage) {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }
}
