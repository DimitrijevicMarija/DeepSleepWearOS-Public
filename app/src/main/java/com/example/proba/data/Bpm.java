package com.example.proba.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.ZonedDateTime;

@Entity
public class Bpm {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private double value;
    private ZonedDateTime time;
    private String timeString;

    public Bpm(long id, double value, ZonedDateTime time, String timeString) {
        this.id = id;
        this.value = value;
        this.time = time;
        this.timeString = timeString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }
}
