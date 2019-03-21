package com.alienonwork.earthquake;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;


import java.util.List;

@Dao
public interface EarthquakeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertEarthquakes(List<Earthquake> earthquakes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertEarthquake(Earthquake earthquake);

    @Delete
    public void deleteEarthquake(Earthquake earthquake);

    @Query("SELECT * FROM earthquake ORDER BY mDate DESC")
    public LiveData<List<Earthquake>> loadAllEarthquakes();

    @Query("SELECT mId as _id, " +
            "mDetails as suggest_text_1, " +
            "mId as suggest_intent_data_id " +
            "FROM earthquake " +
            "WHERE mDetails LIKE :query " +
            "ORDER BY mdate DESC")
    public Cursor generateSeachSuggestions(String query);

    @Query("SELECT * " +
            "FROM earthquake " +
            "WHERE mDetails LIKE :query " +
            "ORDER BY mdate DESC")
    public LiveData<List<Earthquake>> searchEarthquakes(String query);

    @Query("SELECT * " +
            "FROM earthquake " +
            "WHERE mId = :id " +
            "LIMIT 1")
    public LiveData<Earthquake> getEarthquake(String id);

    @Query("SELECT * FROM earthquake ORDER BY mDate DESC")
    public List<Earthquake> loadAllEarthquakeBlocking();
}