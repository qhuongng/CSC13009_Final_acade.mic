package com.example.acade_mic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    private MainActivity activity = null;

    public void setActivity(MainActivity activity) {
        System.out.println("HERE GOESSSSSSSSSSSSSSS");
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("PAUSE_REC")) {
            System.out.println("EREHhiwehfiwehfiwehfdiwehuid");
            activity.pauseRec();
        }
    }
}
