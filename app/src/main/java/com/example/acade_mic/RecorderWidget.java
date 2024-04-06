package com.example.acade_mic;

import android.app.ActivityManager;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.sql.SQLOutput;

/**
 * Implementation of App Widget functionality.
 */
public class RecorderWidget extends AppWidgetProvider {
    public static final String PLAY_BUTTON = "PLAY_BUTTON";
    public static final String TIME_UPDATE = "TIME_UPDATE";
    public static final String PLAY_BUTTON_SWITCH = "PLAY_BUTTON_SWITCH";
    public static final String PAUSE_BUTTON = "PAUSE_BUTTON";
    public static final String PAUSE_BUTTON_SWITCH = "PAUSE_BUTTON_SWITCH";
    public static final String TIME_PAUSED = "TIME_PAUSED";


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        switch (intent.getAction()) {
            case TIME_UPDATE:
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recorder_widget);
                String message = intent.getStringExtra("message");
                if (message.equals("00:00")) {
                    views.setTextViewText(R.id.status, "NOT RECORDING");
                } else {
                    views.setTextViewText(R.id.status, "RECORDING");
                }
                views.setTextViewText(R.id.appwidget_text, message);
                AppWidgetManager.getInstance(context).updateAppWidget(
                        new ComponentName(context, RecorderWidget.class), views);
                break;
            case PLAY_BUTTON:
                Intent recordIntent = new Intent(context.getApplicationContext(), RecordForegroundService.class);
                recordIntent.putExtra("message", "PLAY");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.getApplicationContext().startForegroundService(recordIntent);
                } else {
                    context.getApplicationContext().startService(recordIntent);
                }
                break;
            case PLAY_BUTTON_SWITCH:
                RemoteViews views5 = new RemoteViews(context.getPackageName(), R.layout.recorder_widget);
                views5.setViewVisibility(R.id.widget_pause_btn, View.VISIBLE);
                views5.setViewVisibility(R.id.widget_play_btn, View.GONE);
                AppWidgetManager.getInstance(context).updateAppWidget(
                        new ComponentName(context, RecorderWidget.class), views5);
                break;
            case PAUSE_BUTTON_SWITCH:
                RemoteViews views6 = new RemoteViews(context.getPackageName(), R.layout.recorder_widget);
                views6.setViewVisibility(R.id.widget_pause_btn, View.GONE);
                views6.setViewVisibility(R.id.widget_play_btn, View.VISIBLE);
                AppWidgetManager.getInstance(context).updateAppWidget(
                        new ComponentName(context, RecorderWidget.class), views6);
                break;
            case PAUSE_BUTTON:
                Intent recordIntent2 = new Intent(context.getApplicationContext(), RecordForegroundService.class);
                recordIntent2.putExtra("message", "PAUSE");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.getApplicationContext().startForegroundService(recordIntent2);
                } else {
                    context.getApplicationContext().startService(recordIntent2);
                }
                break;
            case TIME_PAUSED:
                RemoteViews views2 = new RemoteViews(context.getPackageName(), R.layout.recorder_widget);
                String message2 = intent.getStringExtra("message");
                views2.setTextViewText(R.id.status, "PAUSED");
                views2.setTextViewText(R.id.appwidget_text, message2);
                AppWidgetManager.getInstance(context).updateAppWidget(
                        new ComponentName(context, RecorderWidget.class), views2);
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

        Intent intent2 = new Intent(context, getClass());
        intent2.setAction("PAUSE_BUTTON");
        PendingIntent pi2 = PendingIntent.getBroadcast(context, 100, intent2, PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.widget_pause_btn, pi2);

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }
}