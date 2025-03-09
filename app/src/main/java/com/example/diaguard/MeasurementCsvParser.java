package com.example.diaguard;

import android.util.Log;

import com.example.diaguard.models.Measurement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MeasurementCsvParser {

    public static List<Measurement> parse(InputStream inputStream) throws IOException {
        List<Measurement> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        boolean isHeader = true;
        while ((line = reader.readLine()) != null) {
            if (isHeader) {
                isHeader = false;
                continue;
            }
            line = line.substring(0, line.lastIndexOf(';'));
            String[] tokens = line.split(";");
            if (tokens.length < 17) {
                Log.e(MeasurementCsvParser.class.toString(), "Count of columns is less than 17");
                continue;
            }
            Measurement m = new Measurement();
            m.setEntryDate(parseDate(tokens[1]));
            m.setAverageGlucose(parseDouble(tokens[2]));
            m.setBasalInsulin(parseDouble(tokens[3]));
            m.setBasalMetabolicRate(parseDouble(tokens[4]));
            m.setBolusInsulin(parseDouble(tokens[5]));
            m.setDailyInsulinDose(parseDouble(tokens[6]));
            m.setDevice(tokens[7].isEmpty() ? null : tokens[7]);
            m.setGmi(parseDouble(tokens[8]));
            m.setStddevGlucose(parseDouble(tokens[9]));
            m.setTimeActive(parseDouble(tokens[10]));
            m.setTimeInRangeHigh(parseDouble(tokens[11]));
            m.setTimeInRangeLow(parseDouble(tokens[12]));
            m.setTimeInRangeNormal(parseDouble(tokens[13]));
            m.setTimeInRangeVeryHigh(parseDouble(tokens[14]));
            m.setTimeInRangeVeryLow(parseDouble(tokens[15]));
            m.setVariationCoefficient(parseDouble(tokens[16]));
            list.add(m);
        }
        reader.close();
        return list;
    }

    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        // Ожидается формат "yyyy-MM-dd"
        return LocalDate.parse(dateStr);
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
