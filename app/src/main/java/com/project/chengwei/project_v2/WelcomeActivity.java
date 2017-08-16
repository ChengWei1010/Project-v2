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
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    private SQLiteDBHelper dbHelper;

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
                        initDB(); //Database : initial and insert profile data
                        closeDB(); //Database : get data from database profile_tbl
                        getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_FIRST_TIME, false).commit();
                        Intent i = new Intent(WelcomeActivity.this, SetUpActivity.class);
                        startActivity(i);
                        finish();
                    }
                    //if else, check the mode stored in the Shared Preferences
                    else {
                        if(isElder()){
                            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                            finish();
                        }
                        else if(!isElder()){
                            startActivity(new Intent(WelcomeActivity.this, FamilyActivity.class));
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
//        Boolean shared = getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(KEY_IS_FIRST_TIME, true);
//        Boolean sqlHasValue = ;
//        if(shared==true||sqlHasValue==true)return true;
    }
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
        //settings = getSharedPreferences(data,0);
        //return settings.getBoolean(elderlyMode,false);
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database and show the profile saved in the database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
//        cursor = dbHelper.getProfileData();
//        cursor.moveToPosition(0);
//        text_group_name.setText( cursor.getString(cursor.getColumnIndex("room")) );
    }

    //Database : close database
    private void closeDB() {
        dbHelper.close();
    }

}
