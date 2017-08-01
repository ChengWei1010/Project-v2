package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class SetUpActivity extends AppCompatActivity {
    private ImageButton btn_elder;
    private ImageButton btn_family;
    static final String KEY_IS_FIRST_TIME =  "com.<your_app_name>.first_time";
    static final String KEY =  "com.<your_app_name>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        if(isFirstTime()){
            getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_FIRST_TIME, false).commit();

        }
        else {
            //Toast.makeText(SetUpActivity.this, "not first time", Toast.LENGTH_SHORT).show();
            finish();
            Intent setUpIntent = new Intent(SetUpActivity.this, HomeActivity.class);
            startActivity(setUpIntent);
        }
        btn_elder = (ImageButton)findViewById(R.id.btn_elder);
        btn_elder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SetUpActivity.this , HomeActivity.class);
                startActivity(intent);
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckFirstTime ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isFirstTime(){
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(KEY_IS_FIRST_TIME, true);
    }

}

