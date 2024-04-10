package com.example.acade_mic;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "transcriptionFiles", primaryKeys = {"audioId", "langCode"})
public class TranscriptionFile {
    private int audioId;
    private String content;
    private String summary;
    @NonNull
    private String langCode = "";

    public TranscriptionFile() {}

    public TranscriptionFile(int audioId, String content, @NonNull String langCode) {
        this.audioId = audioId;
        this.content = content;
        this.summary = "";
        this.langCode = langCode;
    }

    public TranscriptionFile(int audioId, String content, String summary, @NonNull String langCode) {
        this.audioId = audioId;
        this.content = content;
        this.summary = summary;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String code) {
        this.langCode = code;
    }
}
