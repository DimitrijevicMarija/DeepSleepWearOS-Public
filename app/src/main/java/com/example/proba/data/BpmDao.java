package com.example.proba.data;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BpmDao {

    @Insert
    long insert(Bpm bpm);

    @Query("SELECT * FROM Bpm")
    List<Bpm> getAll();

    @Query("SELECT * FROM Bpm")
    LiveData<List<Bpm>> getAllLiveData();
}
