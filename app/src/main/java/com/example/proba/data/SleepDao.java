package com.example.proba.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SleepDao {

    @Insert
    long insert(Sleep sleep);

    @Query("SELECT * FROM Sleep")
    List<Sleep> getAll();

    @Query("SELECT * FROM Sleep")
    LiveData<List<Sleep>> getAllLiveData();

    @Query("SELECT * FROM Sleep WHERE id = (select max(id) from Sleep)")
    Sleep getLastSleep();

    @Query("SELECT * FROM Sleep WHERE id = (select max(id) from Sleep where id < :cur)")
    Sleep getPreviousSleep(long cur);

    @Query("SELECT * FROM Sleep WHERE id = (select min(id) from Sleep where id > :cur)")
    Sleep getNextSleep(long cur);
}
