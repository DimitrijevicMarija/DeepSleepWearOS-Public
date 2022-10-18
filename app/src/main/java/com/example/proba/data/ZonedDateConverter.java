package com.example.proba.data;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateConverter {
    @TypeConverter
    public long dateToTimestamp(ZonedDateTime time) {
        return time.toEpochSecond();
    }

    @TypeConverter
    public ZonedDateTime timestampToDate(long timestamp) {
        return Instant.ofEpochSecond(timestamp).atZone( ZoneId.of("Europe/Belgrade"));
    }
}
