package com.example.diaguard;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.example.diaguard.db.GlucoseDatabase;
import com.example.diaguard.db.GlucoseEntry;
import com.example.diaguard.models.Measurement;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);

        // Инициализация компонентов
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        Button trackDataBtn = findViewById(R.id.trackDataBtn);

        // Настройка адаптера для ViewPager2 с фрагментами
        viewPager.setAdapter(new HistoryPagerAdapter(this));

        // Связь TabLayout с ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Latest Data");
            } else {
                tab.setText("History");
            }
        }).attach();

        // Кнопка "Track Data" открывает новую активность
        trackDataBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Настройка Toolbar и добавление ActionBarDrawerToggle для бургер-меню
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Добавляем кнопку бургер-меню для открытия/закрытия панели
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_report) {
                Toast.makeText(HistoryActivity.this, "Report is being formed...", Toast.LENGTH_SHORT).show();
                // Выполняем генерацию отчёта в фоновом потоке
                new Thread(() -> {
                    // Получаем данные из базы
                    List<GlucoseEntry> entries = GlucoseDatabase
                            .getInstance(HistoryActivity.this)
                            .glucoseDao()
                            .getAllEntries();

                    // Формируем путь для сохранения файла (файл сохранится во внешней директории приложения)
                    final String outputPath = getExternalFilesDir(null).getAbsolutePath() + "/report.pdf";

                    // Генерируем PDF‑отчёт
                    ReportGenerator.generatePdfReport(HistoryActivity.this, entries, outputPath);

                    // Получаем URI для файла через FileProvider
                    File pdfFile = new File(outputPath);
                    final Uri pdfUri = FileProvider.getUriForFile(
                            HistoryActivity.this,
                            HistoryActivity.this.getPackageName() + ".fileprovider",
                            pdfFile);

                    // Открываем PDF на UI‑потоке
                    runOnUiThread(() -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(pdfUri, "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(HistoryActivity.this, "No application found to open PDF", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
            if (item.getItemId() == R.id.calculator) {
                Intent intent = new Intent(HistoryActivity.this, CalculatorActivity.class);
                startActivity(intent);
            }
            if (item.getItemId() == R.id.nav_analyze_file) {
                openFileChooser();
            }
            if (item.getItemId() == R.id.menu_chat) {
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    List<Measurement> measurements = MeasurementCsvParser.parse(inputStream);

                    // Анализ данных и вывод результатов через отдельный класс
                    AnalysisResultDialog.analyzeAndShowDialog(this, measurements);

                } catch (IOException e) {
                    e.printStackTrace();
                    AnalysisResultDialog.showErrorDialog(this, "Не удалось прочитать выбранный файл.");
                }
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Выберите CSV файл"), PICK_FILE_REQUEST_CODE);
    }
}