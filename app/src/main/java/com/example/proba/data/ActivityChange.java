package com.example.proba.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.ZonedDateTime;

@Entity
public class ActivityChange {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String newState;
    private ZonedDateTime time;

    public ActivityChange(long id, String newState, ZonedDateTime time) {
        this.id = id;
        this.newState = newState;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String action) {
        this.newState = action;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }
}
