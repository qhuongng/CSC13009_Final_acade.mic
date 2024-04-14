package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.acade_mic.model.TranscriptionFile;

import java.util.List;

@Dao
public interface TranscriptionFileDao {
    @Query("SELECT * FROM transcriptionFiles WHERE audioId = :audioId AND langCode = :langCode")
    TranscriptionFile getTranscript(int audioId, String langCode);

    @Query("SELECT * FROM transcriptionFiles WHERE audioId = :audioId")
    List<TranscriptionFile> getTranscripts(int audioId);

    @Insert
    void insert(TranscriptionFile...transcript);

    @Query("Delete from transcriptionFiles where audioId = :audioId AND langCode = :langCode")
    void delete(int audioId, String langCode);

    @Query("Delete from transcriptionFiles where audioId = :audioId")
    void delete(int audioId);

    @Update
    void update(TranscriptionFile transcript);
}
