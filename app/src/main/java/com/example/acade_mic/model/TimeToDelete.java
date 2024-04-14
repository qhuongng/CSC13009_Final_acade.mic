package com.example.acade_mic.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TimeToDeletes")
public class TimeToDelete {
    @PrimaryKey
    private int idRecord;
    @NonNull
    private long timeIn;

    public TimeToDelete(){};
    public TimeToDelete(int id, long time){
        this.idRecord = id;
        this.timeIn = time;
    }

    public int getIdRecord() {
        return idRecord;
    }

    public void setIdRecord(int idRecord) {
        this.idRecord = idRecord;
    }

    public long getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(long timeIn) {
        this.timeIn = timeIn;
    }
}
