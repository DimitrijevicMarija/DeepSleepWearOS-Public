package com.example.proba.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.ZonedDateTime;

@Entity
public class Sleep {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long duration; // in seconds
    private ZonedDateTime start;
    private ZonedDateTime end;

    public Sleep(long id, long duration, ZonedDateTime start, ZonedDateTime end) {
        this.id = id;
        this.duration = duration;
        this.start = start;
        this.end = end;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }
}
