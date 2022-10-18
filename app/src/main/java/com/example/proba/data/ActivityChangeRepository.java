package com.example.proba.data;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ActivityChangeRepository {
    private final ActivityChangeDao avtivityChangeDao;

    public ActivityChangeRepository(ActivityChangeDao avtivityChangeDao) {
        this.avtivityChangeDao = avtivityChangeDao;
    }

    public long insert(ActivityChange activityChange){
        return avtivityChangeDao.insert(activityChange);
    }

    public List<ActivityChange> getAll(){
        // dodatiiiii za niti
        return avtivityChangeDao.getAll();
    }

    public LiveData<List<ActivityChange>> getAllLiveData(){
        return avtivityChangeDao.getAllLiveData();
    }

    public ActivityChange getLastChange(){
        return avtivityChangeDao.getLastChange();
    }


}
