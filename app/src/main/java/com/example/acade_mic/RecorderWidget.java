package com.example.acade_mic;

import android.app.ActivityManager;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.sql.SQLOutput;

/**
 * Implementation of App Widget functionality.
 */
public class RecorderWidget extends AppWidgetProvider {
    public static String PLAY_BUTTON = "PLAY_BUTTON";
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        switch(intent.getAction()){
            case "TIME_UPDATE":
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recorder_widget);
                String message = intent.getStringExtra("message");
                if(message.equals("00:00")){
                    views.setTextViewText(R.id.status, "NOT RECORDING");
                }else{
                    views.setTextViewText(R.id.status, "RECORDING");
                }
                views.setTextViewText(R.id.appwidget_text, message);
                AppWidgetManager.getInstance(context).updateAppWidget(
                        new ComponentName(context, RecorderWidget.class),views);
                break;
            case "PLAY_BUTTON":
                System.out.println("CLICKED HERE");
                Intent activityIntent = new Intent(context, MainActivity.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activityIntent);
                break;
            case "TIME_PAUSED":
                RemoteViews views2 = new RemoteViews(context.getPackageName(), R.layout.recorder_widget);
                String message2 = intent.getStringExtra("message");
                views2.setTextViewText(R.id.status, "PAUSED");
                views2.setTextViewText(R.id.appwidget_text, message2);
                AppWidgetManager.getInstance(context).updateAppWidget(
                        new ComponentName(context, RecorderWidget.class),views2);
                break;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews;
        ComponentName watchWidget;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.recorder_widget);
        watchWidget = new ComponentName(context, RecorderWidget.class);

        Intent intent = new Intent(context, getClass());
        intent.setAction("PLAY_BUTTON");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        remoteViews.setOnClickPendingIntent(R.id.widget_play_btn, pi);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }
}