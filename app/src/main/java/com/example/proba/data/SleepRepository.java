package com.example.proba.data;

import androidx.lifecycle.LiveData;

import java.util.List;

public class SleepRepository {

    private final SleepDao sleepDao;

    public SleepRepository(SleepDao sleepDao) {
        this.sleepDao = sleepDao;
    }

    public long insert(Sleep sleep){
        return sleepDao.insert(sleep);
    }

    public List<Sleep> getAll(){
        // ovde za nit dodati
        return sleepDao.getAll();
    }

    public LiveData<List<Sleep>> getAllLiveData(){
        return sleepDao.getAllLiveData();
    }

    public Sleep getLastSleep(){
        return sleepDao.getLastSleep();
    }

    public  Sleep getPreviousSleep(long cur){
        return sleepDao.getPreviousSleep(cur);
    }

    public Sleep getNextSleep(long cur){
        return sleepDao.getNextSleep(cur);
    }
}
