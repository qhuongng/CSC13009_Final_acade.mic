package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.acade_mic.model.Bookmark;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE audioId = :audioId")
    List<Bookmark> getBookmarksByAudioId(int audioId);

    @Insert
    void insert(Bookmark...bookmarks);


    @Query("Delete from bookMarks where audioId = :audioId and position = :position")
    void delete(int audioId, int position);
    @Query("DELETE from bookMarks where audioId = :audioId")
    void deleteBookmarksByRecordId(int audioId);


    @Update
    void update(Bookmark bookmark);
}
