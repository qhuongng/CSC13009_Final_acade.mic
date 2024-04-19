package com.example.acade_mic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

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
import android.widget.Toast;

import com.example.acade_mic.adapter.AlarmAdapter;
import com.example.acade_mic.model.Alarm;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AlarmActivity extends AppCompatActivity implements OnItemClickListener {
    final Calendar myCalendar = Calendar.getInstance();
    ArrayList<Alarm> alarms;
    AlarmAdapter alarmAdapter;
    AppDatabase db;
    Button addBtn;
    Button btnCancel;
    TextInputEditText startDate;
    TextInputEditText totalDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        startDate = (TextInputEditText) findViewById(R.id.startDate);
        totalDuration = (TextInputEditText) findViewById(R.id.totalDuration);
        alarms = new ArrayList<>();

        db = AppDatabase.getInstance(this);
        alarmAdapter = new AlarmAdapter(alarms, this);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AlarmActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        NumberPicker startHour = (NumberPicker) findViewById(R.id.startHour);
        String[] hourData = new String[24];
        for (int i = 0; i <= 23; i++) {
            hourData[i] = String.valueOf(i).length() < 2 ? ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startHour.setMinValue(0);
        startHour.setMaxValue(hourData.length - 1);
        startHour.setDisplayedValues(hourData);

        NumberPicker startMin = (NumberPicker) findViewById(R.id.startMin);
        String[] minData = new String[60];
        for (int i = 0; i <= 59; i++) {
            minData[i] = String.valueOf(i).length() < 2 ? ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startMin.setMinValue(0);
        startMin.setMaxValue(minData.length - 1);
        startMin.setDisplayedValues(minData);

        NumberPicker startSec = (NumberPicker) findViewById(R.id.startSec);
        String[] secData = new String[60];
        for (int i = 0; i <= 59; i++) {
            secData[i] = String.valueOf(i).length() < 2 ? ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startSec.setMinValue(0);
        startSec.setMaxValue(secData.length - 1);
        startSec.setDisplayedValues(secData);

        addBtn = (Button) findViewById(R.id.addBtn);
        addBtn.setOnClickListener((View v) -> {
            if (startDate.getText().toString().isEmpty() || totalDuration.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                return;
            }

            String startDateTime = startDate.getText().toString() + " " + formatTime(startHour.getValue(), startMin.getValue(), startSec.getValue());
            long totalDurationSec = Integer.parseInt(String.valueOf(totalDuration.getText()));

            LocalDateTime localDateTime = LocalDateTime.parse(startDateTime,
                    DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss"));

            long startTimeMillis = localDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();

            long endTimeMillis = startTimeMillis + 1000L * totalDurationSec;

            Intent intentStart = new Intent(this, RecordForegroundService.class);
            intentStart.setAction("START_ALARM");
            intentStart.putExtra("totalDuration", totalDurationSec * 1000L);
            PendingIntent PIStart = PendingIntent.getService(this, 0, intentStart, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, startTimeMillis, PIStart);

            Alarm toBeInserted = new Alarm(startTimeMillis, endTimeMillis);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isAddable = true;
                    for (Alarm alarm : alarms) {
                        if ((toBeInserted.getStartTimeMillis() <= alarm.getStartTimeMillis() && toBeInserted.getEndTimeMillis() <= alarm.getStartTimeMillis() && toBeInserted.getEndTimeMillis() > alarm.getStartTimeMillis()) ||
                                (toBeInserted.getStartTimeMillis() >= alarm.getEndTimeMillis() && toBeInserted.getEndTimeMillis() >= alarm.getEndTimeMillis() && toBeInserted.getStartTimeMillis() < alarm.getEndTimeMillis()) ||
                                (toBeInserted.getStartTimeMillis() <= alarm.getStartTimeMillis() && toBeInserted.getEndTimeMillis() >= alarm.getStartTimeMillis()) ||
                                (toBeInserted.getStartTimeMillis() <= alarm.getEndTimeMillis() && toBeInserted.getEndTimeMillis() >= alarm.getEndTimeMillis())
                        ) {
                            isAddable = false;
                        }
                    }
                    if (toBeInserted.getStartTimeMillis() < System.currentTimeMillis() + 30000L){
                        isAddable = false;
                    }
                    if (isAddable) {
                        db.alarmDao().insert(toBeInserted);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alarms.add(toBeInserted);
                                alarms.sort(new Comparator<Alarm>() {
                                    @Override
                                    public int compare(Alarm o1, Alarm o2) {
                                        return Long.compare(o1.getStartTimeMillis(), o2.getStartTimeMillis());
                                    }
                                });
                                alarmAdapter.notifyItemInserted(alarms.size() - 1);
                                alarmAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        runOnUiThread(()->{
                            Toast.makeText(getApplicationContext(), "Cannot add this alarm", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();
        });

        btnCancel = (MaterialButton) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener((View v) -> {
            finish();
        });

        RecyclerView recyclerView = findViewById(R.id.alarm_recyclerview);
        recyclerView.setAdapter(alarmAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchAll();
    }

    private void fetchAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                alarms.clear();
                List<Alarm> queryResult = db.alarmDao().getAll();
                alarms.addAll(queryResult);
                // remove an alarm if it has expired
                alarms.removeIf(alarm -> alarm.getEndTimeMillis() + 120L * 1000L < System.currentTimeMillis());
                alarms.sort(new Comparator<Alarm>() {
                    @Override
                    public int compare(Alarm o1, Alarm o2) {
                        return Long.compare(o1.getStartTimeMillis(), o2.getStartTimeMillis());
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alarmAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private String formatTime(int hour, int min, int sec) {
        String hourStr = String.valueOf(hour).length() < 2 ? ("0" + hour) : String.valueOf(hour);
        String minStr = String.valueOf(min).length() < 2 ? ("0" + min) : String.valueOf(min);
        String secStr = String.valueOf(sec).length() < 2 ? ("0" + sec) : String.valueOf(sec);
        return hourStr + ":" + minStr + ":" + secStr;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        startDate.setText(dateFormat.format(myCalendar.getTime()));
    }

    @Override
    public void onItemClickListener(int position) {

    }

    @Override
    public void onItemLongClickListener(int position) {

    }
}