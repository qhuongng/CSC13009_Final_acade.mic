package com.example.acade_mic;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookMarks", primaryKeys = {"audioId","position"})
public class Bookmark {
    private int audioId;
    private int position;
    private String note;

    public Bookmark(){}
    public Bookmark(int audioId,int position, String note){
        this.audioId = audioId;
        this.position = position;
        this.note = note;
    }

    public int getAudioId() {
        return audioId;
    }

    public void setAudioId(int audioId) {
        this.audioId = audioId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
