package com.example.acade_mic;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE audioId = :audioId")
    List<Bookmark> getBookmarksByAudioId(int audioId);

    @Insert
    void insert(Bookmark...bookmarks);

    @Delete
    void delete(Bookmark bookmark);

    @Update
    void update(Bookmark bookmark);
}
