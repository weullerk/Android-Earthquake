package com.alienonwork.earthquake;

import android.arch.persistence.room.Room;
import android.content.Context;

public class EarthquakeDatabaseAccessor {
    private static EarthquakeDatabase EarthquakeDatabaseInstance;
    private static final String EARTHQUAKE_DB_NAME = "earthquake_db";

    private EarthquakeDatabaseAccessor() {}

    public static EarthquakeDatabase getInstance(Context context) {
        if (EarthquakeDatabaseInstance == null) {
            EarthquakeDatabaseInstance = Room.databaseBuilder(context, EarthquakeDatabase.class, EARTHQUAKE_DB_NAME).build();
        }

        return EarthquakeDatabaseInstance;
    }
}
