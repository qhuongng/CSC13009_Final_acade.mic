package com.example.acade_mic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {
    final Calendar myCalendar= Calendar.getInstance();
    Button addBtn;
    EditText startDate;
    EditText totalDuration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        startDate = (EditText) findViewById(R.id.startDate);
        totalDuration = (EditText) findViewById(R.id.totalDuration);
        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
            }
        };

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AlarmActivity.this ,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        NumberPicker startHour = (NumberPicker) findViewById(R.id.startHour);
        String[] hourData = new String[24];
        for(int i = 0; i <= 23; i++){
            hourData[i] = String.valueOf(i).length() < 2 ?  ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startHour.setMinValue(0);
        startHour.setMaxValue(hourData.length-1);
        startHour.setDisplayedValues(hourData);

        NumberPicker startMin = (NumberPicker) findViewById(R.id.startMin);
        String[] minData = new String[60];
        for(int i = 0; i <= 59; i++){
            minData[i] = String.valueOf(i).length() < 2 ?  ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startMin.setMinValue(0);
        startMin.setMaxValue(minData.length-1);
        startMin.setDisplayedValues(minData);

        NumberPicker startSec = (NumberPicker) findViewById(R.id.startSec);
        String[] secData = new String[60];
        for(int i = 0; i <= 59; i++){
               secData[i] = String.valueOf(i).length() < 2 ?  ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startSec.setMinValue(0);
        startSec.setMaxValue(secData.length-1);
        startSec.setDisplayedValues(secData);

        addBtn = (Button) findViewById(R.id.addBtn);
        addBtn.setOnClickListener((View v)->{
            String startDateTime = startDate.getText().toString() + " " + formatTime(startHour.getValue(), startMin.getValue(),startSec.getValue());
            long totalDurationSec = Integer.parseInt(String.valueOf(totalDuration.getText()));

            LocalDateTime localDateTime = LocalDateTime.parse(startDateTime,
                    DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss") );

            long startTimeMillis = localDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();

            Intent intentStart = new Intent(this, RecordForegroundService.class);
            intentStart.setAction("START_ALARM");
            intentStart.putExtra("totalDuration", totalDurationSec * 1000L);
            PendingIntent PIStart = PendingIntent.getService(this, 0, intentStart, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, startTimeMillis, PIStart);
        });
    }

    private String formatTime(int hour, int min, int sec){
        String hourStr = String.valueOf(hour).length() < 2 ? ("0" + hour) : String.valueOf(hour);
        String minStr = String.valueOf(min).length() < 2 ? ("0" + min) : String.valueOf(min);
        String secStr = String.valueOf(sec).length() < 2 ? ("0" + sec) : String.valueOf(sec);
        return hourStr + ":" + minStr + ":" + secStr;
    }

    private void updateLabel(){
        String myFormat="dd/MM/yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        startDate.setText(dateFormat.format(myCalendar.getTime()));
    }

}