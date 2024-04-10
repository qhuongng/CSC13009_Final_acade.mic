package com.example.acade_mic.model;

import androidx.room.Entity;

@Entity(tableName = "reviewAlarm",primaryKeys = "recordId")
public class ReviewAlarm {
    private int recordId;
    private boolean isAlarm;
    public ReviewAlarm(){}
    public ReviewAlarm(int id){
        recordId = id;
        isAlarm = false;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public void setAlarm(boolean alarm) {
        isAlarm = alarm;
    }
}
