package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.acade_mic.model.AudioRecord;

import java.util.List;

@Dao
public interface AudioRecordDao {
    @Query("SELECT * FROM audioRecords")
    List<AudioRecord> getAll();
    @Query("SELECT * FROM audioRecords where id = :id")
    AudioRecord getRecbyID(int id);
    @Query("SELECT * FROM audioRecords where filename like :query")
    List<AudioRecord> searchDatabase(String query);
    // "..."  indicates that the method can accept zero or more AudioRecord objects as arguments.
    @Insert
    void insert(AudioRecord...audioRecords);

    @Delete
    void delete(AudioRecord audioRecord);

    @Delete
    void delete(List<AudioRecord> audioRecords);

    @Update
    void update(AudioRecord audioRecord);
}
