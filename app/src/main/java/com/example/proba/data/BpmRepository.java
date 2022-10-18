package com.example.proba.data;

import androidx.lifecycle.LiveData;

import java.util.List;

public class BpmRepository {

    private final BpmDao bpmDao;

    public BpmRepository(BpmDao bpmDao) {
        this.bpmDao = bpmDao;
    }

    public long insert(Bpm bpm){
        return bpmDao.insert(bpm);
    }

    public List<Bpm> getAll(){
        // dodatiiiii za niti
        return bpmDao.getAll();
    }

    public LiveData<List<Bpm>> getAllLiveData(){
        return bpmDao.getAllLiveData();
    }
}
