package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class WelcomeActivity extends AppCompatActivity {
    static final String KEY_IS_FIRST_TIME =  "com.<your_app_name>.first_time";
    static final String KEY =  "com.<your_app_name>";
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private Boolean mhadSetUp;
    private String hadSetUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    mhadSetUp = hadSetUp();
                    sleep(3000);  //Delay of 2 seconds
                } catch (Exception e) {

                } finally {
                    //if its first time, got to setUpActivity
                    if(isFirstTime()){
                        Log.e("status","is first");
                        initDB(); //Database : initial and insert profile data
                        closeDB();
                        getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(KEY_IS_FIRST_TIME, false).commit();
                        startActivity(new Intent(WelcomeActivity.this, SetUpActivity.class));
                        finish();
                    }
                    //if else, check the mode stored in the Shared Preferences
                    else {
                        initDB();
                        if(!mhadSetUp) {
                            Log.e("status","not first not set up");
                            startActivity(new Intent(WelcomeActivity.this, SetUpActivity.class));
                            finish();
                        }
                        else {
                            if (isElder()) {
                                Log.e("status","elder, set up");
                                startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                                finish();
                            } else if (!isElder()) {
                                Log.e("status","family, set up");
                                startActivity(new Intent(WelcomeActivity.this, FamilyActivity.class));
                                finish();
                            }
                        }
                        closeDB();
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
    public boolean hadSetUp(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
        //Database : get data from database profile_tbl
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        hadSetUp = cursor.getString(cursor.getColumnIndex("hadSetUp"));
        if(hadSetUp.equals("1")){
            return true;
        }else{
            return false;
        }
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
    // Show simple message using SnackBar
    private void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
