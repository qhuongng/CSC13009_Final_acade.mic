package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.example.acade_mic.AudioRecord;
import com.example.acade_mic.model.Alarm;

import java.util.List;

@Dao
public interface AlarmDao {
    @Query("SELECT * FROM alarms")
    List<Alarm> getAll();

    @Insert
    void insert(Alarm...alarms);

    @Delete
    void delete(List<Alarm> alarms);
    @Delete
    void delete(Alarm alarm);
}
