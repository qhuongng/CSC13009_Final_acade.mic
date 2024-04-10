package com.example.acade_mic;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotifyAlarmActivity extends AppCompatActivity {
    final Calendar myCalendar = Calendar.getInstance();
    Button addBtn;
    EditText startDate;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_alarm);
        startDate = (EditText)findViewById(R.id.startDate);
        db = AppDatabase.getInstance(this);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(NotifyAlarmActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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


    }
    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        startDate.setText(dateFormat.format(myCalendar.getTime()));
    }
}
