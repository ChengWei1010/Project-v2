package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {
    static final String KEY_IS_FIRST_TIME =  "com.<your_app_name>.first_time";
    static final String KEY =  "com.<your_app_name>";
    private SharedPreferences settings;
    private static final String data = "DATA";
    private static final String elderlyMode = "ELDERLY_MODE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(2000);  //Delay of 5 seconds
                } catch (Exception e) {

                } finally {
                    //if its first time, got to setUpActivity
                    if(isFirstTime()){
                        getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_FIRST_TIME, false).commit();
                        Intent i = new Intent(WelcomeActivity.this, SetUpActivity.class);
                        startActivity(i);
                        finish();
                    }
                    //if else, check the mode stored in the Shared Preferences
                    else {
                        if(isElder()){
                            Intent homeIntent = new Intent(WelcomeActivity.this, HomeActivity.class);
                            startActivity(homeIntent);
                            finish();
                        }
                        else if(!isElder()){
                            Intent Intent = new Intent(WelcomeActivity.this, FamilyActivity.class);
                            startActivity(Intent);
                            finish();
                        }
                    }
                }
            }
        };
        welcomeThread.start();
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isFirstTime(){
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(KEY_IS_FIRST_TIME, true);
    }
    public boolean isElder() {
        settings = getSharedPreferences(data,0);
        return settings.getBoolean(elderlyMode,false);
    }
//    public void readSharedPreferences(){
//        String myName = "";
//        settings = getSharedPreferences(data,0);
//        myName = settings.getString(name, "");
//        Toast.makeText(WelcomeActivity.this, "is elder", Toast.LENGTH_SHORT).show();
//    }
}
