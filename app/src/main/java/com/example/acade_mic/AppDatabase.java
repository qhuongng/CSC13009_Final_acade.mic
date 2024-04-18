package com.example.acade_mic;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.acade_mic.dao.AlarmDao;
import com.example.acade_mic.dao.AlbumDao;
import com.example.acade_mic.dao.AudioRecordDao;
import com.example.acade_mic.dao.BookmarkDao;
import com.example.acade_mic.dao.ReviewAlarmDao;
import com.example.acade_mic.dao.TranscriptionFileDao;
import com.example.acade_mic.model.Alarm;
import com.example.acade_mic.model.Album;
import com.example.acade_mic.model.AudioRecord;
import com.example.acade_mic.model.Bookmark;
import com.example.acade_mic.model.ReviewAlarm;
import com.example.acade_mic.model.TranscriptionFile;


@Database(entities = {Album.class, AudioRecord.class, Bookmark.class, TranscriptionFile.class, Alarm.class, ReviewAlarm.class}, version = 9,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AudioRecordDao audioRecordDao();
    public abstract BookmarkDao bookmarkDao();
    public abstract AlarmDao alarmDao();
    public  abstract TranscriptionFileDao transcriptionFileDao();
    public abstract ReviewAlarmDao reviewAlarmDao();
    public  abstract AlbumDao albumDao();


    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,"audioRecords")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}