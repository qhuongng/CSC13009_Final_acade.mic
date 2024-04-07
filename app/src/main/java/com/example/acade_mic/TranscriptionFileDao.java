package com.example.acade_mic;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TranscriptionFileDao {
    @Query("SELECT * FROM transcriptionFiles WHERE audioId = :audioId LIMIT 1")
    TranscriptionFile getTranscript(int audioId);

    @Insert
    void insert(TranscriptionFile...transcript);

    @Query("Delete from transcriptionFiles where audioId = :audioId")
    void delete(int audioId);

    @Update
    void update(TranscriptionFile transcript);
}
