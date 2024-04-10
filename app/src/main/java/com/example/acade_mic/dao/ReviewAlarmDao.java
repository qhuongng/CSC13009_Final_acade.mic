package com.example.acade_mic.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.acade_mic.model.Bookmark;
import com.example.acade_mic.model.ReviewAlarm;

@Dao
public interface ReviewAlarmDao {
    @Insert
    void insert(ReviewAlarm...reviewAlarms);

    @Query("DELETE from reviewAlarm where recordId = :audioId")
    void deleteAlarmByRecordId(int audioId);

}
