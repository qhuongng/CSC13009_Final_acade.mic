package com.example.acade_mic;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.PersistableBundle;

import com.example.acade_mic.model.AudioRecord;

import java.io.File;

public class FileDeleteJobService extends JobService {

    AppDatabase db = AppDatabase.getInstance(this);
    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle extras = params.getExtras();
        if (extras != null) {
            String path = extras.getString("filePath");
            int id = extras.getInt("id");
            File delFile = new File(path);
            if(delFile != null)  delFile.delete();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    db.audioRecordDao().delete(db.audioRecordDao().getRecbyID(id));
                    db.albumDao().deleteByIdRecord(id);
                    db.bookmarkDao().deleteBookmarksByRecordId(id);
                    db.transcriptionFileDao().delete(id);
                }
            }).start();
            jobFinished(params,false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
