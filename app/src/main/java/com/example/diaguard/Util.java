package com.example.diaguard;

import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;

public class Util {
    public static final double MIN_THRESHOLD = 3.9;
    public static final double MAX_THRESHOLD = 7.8;

    public static int getGlucoseColor(float level) {
        if (level < MIN_THRESHOLD) {
            return Color.parseColor("#f46668");
        } else if (level > MAX_THRESHOLD) {
            return Color.parseColor("#fff374");
        } else {
            return Color.parseColor("#E8F5E9");
        }
    }

    public static LimitLine createLimitLine(float value) {
        LimitLine limitLine = new LimitLine(value);
        limitLine.setLineColor(Color.RED);
        limitLine.setLineWidth(0.6f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        return limitLine;
    }

}
