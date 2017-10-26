package com.project.chengwei.project_v2;

import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    private DatePickerDialog.OnDateSetListener varDateSetListener;
    private TimePickerDialog.OnTimeSetListener varTimeSetListener;
    private EditText edit_title,edit_content;
    private Button btn_done,edit_date,edit_time;
    private Context context;
    private Toolbar myToolbar;
    private TextView toolbar_title;
    private CharSequence text1 ="請填寫欄位";

    public static final String Database_Path = "Calendar";
    public String dateHolder,timeHolder, titleHolder, contentHolder;
    private String myGroup, myId;
    private DatabaseReference mDatabaseRef;

    int Year;
    int MonthOfYear;
    int DayOfMonth;
    int HourOfDay=30;
    int Minute;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_add);

        //取得房號
        myId = getIntent().getExtras().get("myId").toString();
        myGroup = getIntent().getExtras().get("myGroup").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup);

        initView();
        setToolbar();

        varDateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view , int year , int monthOfYear , int dayOfMonth){
                edit_date.setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);
                Year=year;
                MonthOfYear=monthOfYear;
                DayOfMonth=dayOfMonth;
            }
        };

        varTimeSetListener = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(
                    TimePicker view , int hourOfDay , int minute){
                edit_time.setText(hourOfDay + "點" + minute+"分");
                HourOfDay=hourOfDay;
                Minute=minute;
            }
        };

        edit_date.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog dateDialog = new DatePickerDialog(
                                CalendarAddActivity.this,
                                varDateSetListener,
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                        );
                        dateDialog.show();
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

                if(Year==0)
                    Toast.makeText(context, text1+"1", Toast.LENGTH_SHORT).show();

                else if(HourOfDay==30)
                    Toast.makeText(context, text1+"2", Toast.LENGTH_SHORT).show();

                else  if(Title.toString().equals("") != false)
                    Toast.makeText(context, Title+"請填寫標題", Toast.LENGTH_SHORT).show();

                else  if(Year==0&&HourOfDay==30)
                    Toast.makeText(context, text1+"4", Toast.LENGTH_SHORT).show();

                else  if(Year==0&&Title.toString().equals("") == false)
                    Toast.makeText(context, text1+"5", Toast.LENGTH_SHORT).show();

                else  if(HourOfDay==30&&Title.toString().equals("") == false)
                    Toast.makeText(context, text1+"6", Toast.LENGTH_SHORT).show();

                else  if(HourOfDay==30&&Title==null&&Year==0&&Title.toString().equals("") == false)
                    Toast.makeText(context, text1+"7", Toast.LENGTH_SHORT).show();

                else {
                    CalendarDetails calendarDetails = new CalendarDetails();
                    GetDataFromEditText();

                    calendarDetails.setDate(dateHolder);
                    calendarDetails.setTime(timeHolder);
                    calendarDetails.setTitle(titleHolder);
                    calendarDetails.setContent(contentHolder);

                    mDatabaseRef = mDatabaseRef.child("calendar");

                    String CalendarEventID = mDatabaseRef.push().getKey();
                    calendarDetails.seteventId(CalendarEventID);
                    mDatabaseRef.child(CalendarEventID).setValue(calendarDetails);

                    Toast.makeText(CalendarAddActivity.this,"已新增", Toast.LENGTH_LONG).show();
                    backToCalendar();
                }
            }
        });
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


