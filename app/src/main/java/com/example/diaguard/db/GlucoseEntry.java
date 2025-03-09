package com.example.diaguard.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "glucose_data")
public class GlucoseEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "glucose_level")
    public float level;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "note")
    public String note;

    public GlucoseEntry(float level, long timestamp) {
        this.level = level;
        this.timestamp = timestamp;
    }
}
