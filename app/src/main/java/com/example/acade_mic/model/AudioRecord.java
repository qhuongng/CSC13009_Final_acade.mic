package com.example.acade_mic.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "audioRecords")
public class AudioRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @Ignore
    private boolean isChecked = false;
    private String filename;
    private String filePath;
    private Long timestamp;
    private String duration;
    private String ampsPath;

    // Empty constructor required by Room
    public AudioRecord() {
    }

    // Constructor to initialize fields
    public AudioRecord(String filename, String filePath, Long timestamp, String duration, String ampsPath) {
        this.filename = filename;
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.duration = duration;
        this.ampsPath = ampsPath;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAmpsPath() {
        return ampsPath;
    }

    public void setAmpsPath(String ampsPath) {
        this.ampsPath = ampsPath;
    }
}

