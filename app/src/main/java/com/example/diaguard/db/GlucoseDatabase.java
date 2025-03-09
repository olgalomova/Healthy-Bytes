package com.example.diaguard.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {GlucoseEntry.class}, version = 2)
public abstract class GlucoseDatabase extends RoomDatabase {
    private static volatile GlucoseDatabase INSTANCE;

    public abstract GlucoseDao glucoseDao();

    public static GlucoseDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GlucoseDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GlucoseDatabase.class, "glucose_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
