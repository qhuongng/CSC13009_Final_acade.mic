package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.acade_mic.model.Album;
import com.example.acade_mic.model.Bookmark;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface AlbumDao {
    @Query("SELECT DISTINCT albumName FROM albums")
    List<String> getAllAlbumName();

    @Query("SELECT recordID FROM albums WHERE albumName=:albumName")
    List<Integer>getAllrecordIDbyAlbumName(String albumName);

    @Insert
    void insert(Album...albums);

    @Update
    void update(Album album);
}
