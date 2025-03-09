package com.example.diaguard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class GRIChartView extends View {

    private Paint paint;
    private double griValue = 0;
    private double hyperComponent = 0; // Значение для оси Y
    private double hypoComponent = 0; // Значение для оси X

    public GRIChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(40); // Размер текста для отображения зоны
    }

    public void setGriValue(double griValue, double hyperComponent, double hypoComponent) {
        this.griValue = griValue;
        this.hyperComponent = hyperComponent;
        this.hypoComponent = hypoComponent;
        invalidate(); // Перерисовать View
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawChart(canvas);
    }

    private void drawChart(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // Оси
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        canvas.drawLine(50, height - 50, width - 50, height - 50, paint); // Ось X
        canvas.drawLine(50, height - 50, 50, 50, paint); // Ось Y

        // Зоны
        drawZone(canvas, 0, 20, Color.GREEN); // Zone A
        drawZone(canvas, 20, 40, Color.YELLOW); // Zone B
        drawZone(canvas, 40, 60, Color.rgb(255, 165, 0)); // Zone C (Orange)
        drawZone(canvas, 60, 80, Color.rgb(255, 105, 180)); // Zone D (Pink)
        drawZone(canvas, 80, 100, Color.rgb(139, 0, 0)); // Zone E (DarkRed)
    }

    private void drawZone(Canvas canvas, int startValue, int endValue, int color) {
        int width = getWidth();
        int height = getHeight();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        Path path = new Path();
        path.moveTo(50 + (width - 100) * startValue / 100f, height - 50);
        path.lineTo(50 + (width - 100) * endValue / 100f, height - 50);
        path.lineTo(50, height - 50 - (height - 100) * endValue / 100f);
        path.lineTo(50, height - 50 - (height - 100) * startValue / 100f);
        path.close();

        canvas.drawPath(path, paint);
    }
}