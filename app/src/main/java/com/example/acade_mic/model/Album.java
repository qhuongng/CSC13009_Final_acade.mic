package com.example.acade_mic.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "albums")

public class Album {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String albumName;
    @Nullable
    private int recordID;
    public Album(){}
    public Album(String albumName){
        this.albumName=albumName;
    }

    public Album(String albumName, int recordID){
        this.albumName = albumName;
        this.recordID = recordID;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int getRecordID() {
        return recordID;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }
}
