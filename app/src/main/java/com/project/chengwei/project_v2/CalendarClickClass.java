package com.project.chengwei.project_v2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by chengwei on 2017/10/27.
 */

public class CalendarClickClass extends Dialog implements android.view.View.OnClickListener {
    public Activity c;
    public Dialog d;
    public String title, content, date, time;
    public TextView text_title, text_content, text_date, text_time;

    public CalendarClickClass(Activity a, String title, String content, String date, String time) {
        super(a);
        this.c = a;
        this.title = title;
        this.content = content;
        this.date = date;
        this.time = time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alert_calendar);
        text_title = findViewById(R.id.text_title);
        text_content = findViewById(R.id.text_content);
        text_date = findViewById(R.id.text_date);
        text_time = findViewById(R.id.text_time);

        text_title.setText(this.title);
        text_content.setText(this.content);
        text_date.setText("活動日期 : "+ this.date);
        text_time.setText("活動時間 : "+this.time);
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_map:
//
//            case R.id.btn_magnifier:
//                Intent intent2 = new Intent();
//                intent2.setClass(v.getContext(), HomeActivity.class);
//                v.getContext().startActivity(intent2);
//                break;
//            default:
//                break;
//        }
//        dismiss();
    }
}
