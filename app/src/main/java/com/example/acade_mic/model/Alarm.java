package com.example.acade_mic.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long startTimeMillis;

    private long endTimeMillis;

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public Alarm(long startTimeMillis, long endTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
    }
}
