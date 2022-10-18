package com.example.proba.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ActivityChangeDao {

    @Insert
    long insert(ActivityChange activityChange);


    @Query("SELECT * FROM ActivityChange")
    List<ActivityChange> getAll();

    @Query("SELECT * FROM ActivityChange")
    LiveData<List<ActivityChange>> getAllLiveData();

    @Query("SELECT * FROM ActivityChange WHERE id = (select max(id) from ActivityChange)")
    ActivityChange getLastChange();


}
