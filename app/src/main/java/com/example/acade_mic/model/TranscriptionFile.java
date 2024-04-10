package com.example.acade_mic.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "transcriptionFiles", primaryKeys = {"audioId", "langCode"})
public class TranscriptionFile {
    private int audioId;
    private String content;
    @NonNull
    private String langCode = "";

    public TranscriptionFile() {}

    public TranscriptionFile(int audioId, String content, @NonNull String langCode) {
        this.audioId = audioId;
        this.content = content;
        this.langCode = langCode;
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

    public void setContent(String content) {
        this.content = content;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String code) {
        this.langCode = code;
    }
}
