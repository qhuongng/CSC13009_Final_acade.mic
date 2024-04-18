package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.acade_mic.model.Album;
import com.example.acade_mic.model.AudioRecord;
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

    @Query("DELETE FROM albums WHERE albumName =:name")
    void deletebyAlbumName(String name);

    @Query("UPDATE albums SET albumName = :newAlbumName WHERE albumName = :albumName")
    void updateAlbumName(String albumName, String newAlbumName);

    @Query("SELECT distinct albumName FROM albums where albumName like :query")
    List<String> searchDatabase(String query);
    @Query("delete from albums where recordID = :id")
    void deleteByIdRecord(int id);

    @Query("select * from albums where albumName =:albName and recordID =:id")
    Album checkExists(String albName, int id);
}
