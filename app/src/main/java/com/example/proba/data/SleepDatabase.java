package com.example.proba.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@TypeConverters(value = {ZonedDateConverter.class})
@Database(entities = {ActivityChange.class, Sleep.class, Bpm.class}, version = 3, exportSchema = false)
public abstract class SleepDatabase extends RoomDatabase {

    public abstract SleepDao sleepDao();
    public abstract ActivityChangeDao activityChangeDao();
    public abstract BpmDao bpmDao();

    private static final String DATABASE_NAME = "sleep-app.db";
    private static SleepDatabase instance = null;

    public static SleepDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (SleepDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            SleepDatabase.class,
                            DATABASE_NAME)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
