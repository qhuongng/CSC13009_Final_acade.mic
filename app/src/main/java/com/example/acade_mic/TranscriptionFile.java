package com.example.acade_mic;

import androidx.room.Entity;

@Entity(tableName = "transcriptionFiles", primaryKeys = {"audioId"})
public class TranscriptionFile {
    private int audioId;
    private String content;

    public TranscriptionFile(){}
    public TranscriptionFile(int audioId,int position){
        this.audioId = audioId;
        this.content = "";
    }

    public int getAudioId() {
        return audioId;
    }

    public void setAudioId(int audioId) {
        this.audioId = audioId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String note) {
        this.content = note;
    }
}
