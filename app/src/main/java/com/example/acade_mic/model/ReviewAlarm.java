package com.example.acade_mic.model;

import androidx.room.Entity;

@Entity(tableName = "reviewAlarm",primaryKeys = "recordId")
public class ReviewAlarm {
    private int recordId;
    private long startTime;
    public ReviewAlarm(){}
    public ReviewAlarm(int id , long time){
        recordId = id;
        startTime = time;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
