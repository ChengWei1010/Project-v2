package com.project.chengwei.project_v2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class CalendarAddActivity extends AppCompatActivity {

    private TimePickerDialog.OnTimeSetListener varTimeSetListener;
    private EditText edit_title,edit_content;
    private Button btn_done,edit_date,edit_time;
    private Context context;
    private Toolbar myToolbar;
    private TextView toolbar_title;
    private CharSequence text1 ="請填寫欄位";
    public String dateHolder,timeHolder, titleHolder, contentHolder;
    private String myGroup, myId;
    private DatabaseReference mDatabaseRef;
    private String formattedMonth,formattedDay,formattedDate;
    private int mYear, mMonth, mDay;
    private CalendarDetails calendarDetails = new CalendarDetails();

    int Year;
    int HourOfDay=30;
    int Minute;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_add);

        //取得房號
        myId = getIntent().getExtras().get("myId").toString();
        myGroup = getIntent().getExtras().get("myGroup").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup).child("calendar");

        initView();
        setToolbar();

        varTimeSetListener = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view , int hourOfDay , int minute){
                edit_time.setText(hourOfDay + "點" + minute+"分");
                HourOfDay=hourOfDay;
                Minute=minute;
            }
        };

        edit_date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(CalendarAddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        setDateFormat(year,month,day);
                        edit_date.setText(formattedDate);
                    }

                }, mYear,mMonth, mDay).show();
            }

        });

        edit_time.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){
                        Calendar calendar = Calendar.getInstance();
                        TimePickerDialog timeDialog = new TimePickerDialog(
                                CalendarAddActivity.this,
                                varTimeSetListener,
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                        );
                        timeDialog.show();
                    }
                });


        btn_done.setOnClickListener(new View.OnClickListener() {
            // Click done Button  必須要打曰期時間title,內容不一定打
            @Override
            public void onClick(View view) {

                String Title = edit_title.getText().toString();
                String Content = edit_content.getText().toString();

                if(formattedDate.length()!=10){
                    Toast.makeText(context, "日期有誤", Toast.LENGTH_SHORT).show();
                }
                else if(Title.toString().equals("")){
                    Toast.makeText(context, "請填寫標題", Toast.LENGTH_SHORT).show();
                }
                else if(Content.toString().equals("")){
                    Toast.makeText(context, "請填寫活動內容", Toast.LENGTH_SHORT).show();
                }
                else if(HourOfDay==30){
                    Toast.makeText(context, "時間有誤", Toast.LENGTH_SHORT).show();
                }
                else {
                    GetDataFromEditText();

                    calendarDetails.setDate(formattedDate);
                    calendarDetails.setTime(timeHolder);
                    calendarDetails.setTitle(titleHolder);
                    calendarDetails.setContent(contentHolder);

                    String CalendarEventID = mDatabaseRef.push().getKey();
                    calendarDetails.seteventId(CalendarEventID);
                    mDatabaseRef.child(CalendarEventID).setValue(calendarDetails);

                    Toast.makeText(CalendarAddActivity.this,"已新增", Toast.LENGTH_LONG).show();
                    backToCalendar();
                }
            }
        });
    }
    public String setDateFormat(int year,int monthOfYear,int dayOfMonth){
        monthOfYear++;
        if(monthOfYear < 10){
            formattedMonth = "0" + String.valueOf(monthOfYear);
        }
        else{
            formattedMonth = String.valueOf(monthOfYear);
        }
        if(dayOfMonth < 10){
                formattedDay = "0" + String.valueOf(dayOfMonth);
        }
        else{
            formattedDay = String.valueOf(dayOfMonth);
        }
        formattedDate = year + "-" + formattedMonth + "-" + formattedDay;
        Log.d("year",Integer.toString(year));
        Log.d("formattedMonth",formattedMonth);
        Log.d("formattedDay",formattedDay);
        return formattedDate;
    }
    public void initView(){
        edit_title = findViewById(R.id.edit_title);
        edit_content = findViewById(R.id.edit_content);
        btn_done = findViewById(R.id.btn_done);
        edit_date = findViewById(R.id.edit_date);
        edit_time = findViewById(R.id.edit_time);
        context = getApplicationContext();
        myToolbar = findViewById(R.id.toolbar_home);
        toolbar_title = findViewById(R.id.toolbar_title);
    }
    public void GetDataFromEditText(){
        dateHolder = edit_date.getText().toString().trim();
        timeHolder = edit_time.getText().toString().trim();
        titleHolder = edit_title.getText().toString().trim();
        contentHolder = edit_content.getText().toString().trim();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            backToCalendar();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void setToolbar(){
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar_title.setText("新增活動");
        myToolbar.setNavigationIcon(R.drawable.ic_back_left_white_50dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToCalendar();
            }
        });
    }
    public void backToCalendar(){
        Intent intent_calendar = new Intent(getApplicationContext(),CalendarActivity.class);
        intent_calendar.putExtra("myGroup",myGroup);
        intent_calendar.putExtra("myId", myId);
        startActivity(intent_calendar);
        finish();
    }
}


