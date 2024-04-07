package com.example.acade_mic;
import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AudioRecord.class, Bookmark.class, TranscriptionFile.class}, version = 3,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AudioRecordDao audioRecordDao();
    public abstract BookmarkDao bookmarkDao();
    public  abstract TranscriptionFileDao transcriptionFileDao();

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