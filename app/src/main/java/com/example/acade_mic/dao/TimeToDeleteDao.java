package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.acade_mic.model.Album;
import com.example.acade_mic.model.TimeToDelete;

@Dao
public interface TimeToDeleteDao {
    @Insert
    void insert(TimeToDelete...TimeToDeletes);
    @Query("delete from timetodeletes where idRecord = :id")
    void deleteByIdRecord(int id);
}
