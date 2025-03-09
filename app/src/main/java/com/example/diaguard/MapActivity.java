package com.example.diaguard;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.diaguard.db.GlucoseDao;
import com.example.diaguard.db.GlucoseDatabase;
import com.example.diaguard.db.GlucoseEntry;
import com.example.diaguard.notification.InactivityReminderScheduler;
import com.example.diaguard.notification.NotificationHelper;
import com.example.diaguard.notification.RegularReminderScheduler;

import java.util.concurrent.Executors;

public class MapActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    private GlucoseDao glucoseDao;
    private final RegularReminderScheduler regularScheduler = new RegularReminderScheduler();
    private final InactivityReminderScheduler inactivityScheduler = new InactivityReminderScheduler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
        NotificationHelper.createNotificationChannel(this);

        regularScheduler.schedule(this);

        glucoseDao = GlucoseDatabase.getInstance(getApplicationContext()).glucoseDao();
        EditText glucoseInput = findViewById(R.id.glucoseInput);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> {
            String input = glucoseInput.getText().toString();
            if (!input.isEmpty()) {
                saveGlucoseLevel(Float.parseFloat(input));

                Toast.makeText(MapActivity.this, "Data saved!", Toast.LENGTH_SHORT).show();

                inactivityScheduler.cancel(MapActivity.this);
                inactivityScheduler.schedule(MapActivity.this);

                Intent intent = new Intent(MapActivity.this, HistoryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MapActivity.this, "Enter glucose level!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveGlucoseLevel(float level) {
        long timestamp = System.currentTimeMillis();
        GlucoseEntry entry = new GlucoseEntry(level, timestamp);

        Executors.newSingleThreadExecutor().execute(() -> glucoseDao.insert(entry));
    }
}
