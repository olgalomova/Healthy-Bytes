package com.example.diaguard.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GlucoseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GlucoseEntry entry);

    @Update
    void updateEntry(GlucoseEntry entry);

    @Query("DELETE FROM glucose_data WHERE timestamp = :timestamp")
    void deleteEntry(long timestamp);

    @Query("SELECT * FROM glucose_data ORDER BY timestamp DESC")
    LiveData<List<GlucoseEntry>> getAllEntriesLive();
    @Query("SELECT * FROM glucose_data ORDER BY timestamp DESC")
    List<GlucoseEntry> getAllEntries();
    @Query("SELECT * FROM glucose_data WHERE timestamp >= (strftime('%s', 'now') - 86400) * 1000 ORDER BY timestamp")
    LiveData<List<GlucoseEntry>> getEntriesLast24Hours();
}

