package com.example.diaguard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        // Инициализация элементов
        TextView textView = findViewById(R.id.textCalculator);
        TextView textView1 = findViewById(R.id.textCalculator1);
        TextView textView2 = findViewById(R.id.textCalculator2);
        EditText editText1 = findViewById(R.id.editTextNumber1);
        EditText editText2 = findViewById(R.id.editTextNumber2);
        EditText editText3 = findViewById(R.id.editTextNumber3);
        EditText editText4 = findViewById(R.id.editTextNumber4);
        Button buttonCalculate = findViewById(R.id.buttonCalculate);
        GRIChartView chartView = findViewById(R.id.gri_chart);
        // Обработчик нажатия кнопки
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Считываем введённые значения
                    double VLow = Double.parseDouble(editText1.getText().toString());
                    double Low = Double.parseDouble(editText2.getText().toString());
                    double VHigh = Double.parseDouble(editText3.getText().toString());
                    double High = Double.parseDouble(editText4.getText().toString());

                    // Вычисляем компоненты гипогликемии и гипергликемии
                    double hypoComponent = VLow + (0.8 * Low);
                    double hyperComponent = VHigh + (0.5 * High);

                    // Вычисляем GRI
                    double GRI = (3.0 * VLow) + (2.4 * Low) + (1.6 * VHigh) + (0.8 * High);

                    // Выводим результат
                    textView.setText(String.format("Result GRI: %.2f", GRI));
                    textView1.setText(String.format("Result hypoComponent: %.2f", hypoComponent));
                    textView2.setText(String.format("Result hyperComponent: %.2f", hyperComponent));
                    chartView.setGriValue(GRI, hyperComponent, hypoComponent);
                    showZones(GRI);
                } catch (NumberFormatException e) {
                    textView.setText("Please, enter correct values.");
                    textView1.setText("Please, enter correct values.");
                    textView2.setText("Please, enter correct values.");
                }
            }
        });
    }
    private void showZones(double griValue) {
        LinearLayout zoneContainer = findViewById(R.id.zone_container);
        LinearLayout currentZoneContainer = findViewById(R.id.current_zone_container);

        zoneContainer.removeAllViews(); // Очищаем контейнер перед добавлением новых элементов
        currentZoneContainer.removeAllViews(); // Очищаем контейнер текущей зоны

        // Массив зон с цветами и подписями
        String[] zoneLabels = {"Zone A (0-20)", "Zone B (21-40)", "Zone C (41-60)", "Zone D (61-80)", "Zone E (81-100)"};
        int[] zoneColors = {Color.GREEN, Color.YELLOW, Color.rgb(255, 165, 0), Color.rgb(255, 105, 180), Color.rgb(139, 0, 0)};

        for (int i = 0; i < zoneLabels.length; i++) {
            // Создаем контейнер для каждой зоны
            LinearLayout zoneLayout = new LinearLayout(this);
            zoneLayout.setOrientation(LinearLayout.HORIZONTAL);
            zoneLayout.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 5, 0, 8); // Отступы между зонами
            zoneLayout.setLayoutParams(layoutParams);

            // Цветной квадратик
            View colorView = new View(this);
            colorView.setBackgroundColor(zoneColors[i]);
            LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(40, 40); // Размер квадратика
            colorView.setLayoutParams(colorParams);

            // Подпись зоны
            TextView zoneText = new TextView(this);
            zoneText.setText(zoneLabels[i]);
            zoneText.setTextSize(15); // Размер текста
            zoneText.setPadding(16, 0, 0, 0); // Отступ между квадратиком и текстом

            // Добавляем элементы в контейнер зоны
            zoneLayout.addView(colorView);
            zoneLayout.addView(zoneText);

            // Добавляем зону в общий контейнер
            zoneContainer.addView(zoneLayout);
        }

        // Определяем текущую зону на основе griValue
        String currentZone = "";
        if (griValue >= 0 && griValue <= 20) {
            currentZone = "Current GRI Zone: A (0-20)";
        } else if (griValue > 20 && griValue <= 40) {
            currentZone = "Current GRI Zone: B (21-40)";
        } else if (griValue > 40 && griValue <= 60) {
            currentZone = "Current GRI Zone: C (41-60)";
        } else if (griValue > 60 && griValue <= 80) {
            currentZone = "Current GRI Zone: D (61-80)";
        } else if (griValue > 80 && griValue <= 100) {
            currentZone = "Current GRI Zone: E (81-100)";
        }

        // Отображаем текущую зону
        TextView currentZoneText = new TextView(this);
        currentZoneText.setText(currentZone);
        currentZoneText.setTextSize(18);
        currentZoneText.setGravity(Gravity.CENTER);
        currentZoneText.setPadding(0, 8, 0, 0); // Отступ сверху

        // Добавляем текущую зону в контейнер
        currentZoneContainer.addView(currentZoneText);
    }
}


