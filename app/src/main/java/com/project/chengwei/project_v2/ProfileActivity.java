package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    static final String KEY_IS_FIRST_TIME =  "com.<your_app_name>.first_time";
    static final String KEY =  "com.<your_app_name>";
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private Toolbar myToolbar;
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewAddress;
    private TextView textViewBirthday;
    private TextView textViewRoom;
    private ImageView profileImg;
    private ImageButton btn_editProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViews();
        setToolbar();
        setListeners();
        initDB();
        closeDB();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        myToolbar = (Toolbar) findViewById(R.id.toolbar_home);
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewPhone = (TextView) findViewById(R.id.textViewPhone);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewBirthday = (TextView) findViewById(R.id.textViewBirthday);
        textViewRoom = (TextView) findViewById(R.id.textViewRoom);
        profileImg = (ImageView) findViewById(R.id.profileImg);
        btn_editProfile = (ImageButton) findViewById(R.id.btn_editProfile);
    }
    public void setListeners() {
        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(ProfileActivity.this, ProfileAddActivity.class);
                startActivity(Intent);
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database and show the profile saved in the database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
        //Database : get data from database profile_tbl
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        textViewName.setText( cursor.getString(cursor.getColumnIndex("name")) );
        textViewPhone.setText( cursor.getString(cursor.getColumnIndex("phone")) );
        textViewAddress.setText( cursor.getString(cursor.getColumnIndex("address")) );
        textViewBirthday.setText( cursor.getString(cursor.getColumnIndex("birthday")) );
        textViewRoom.setText( cursor.getString(cursor.getColumnIndex("room")) );

        // Load image from Database
//        try {
//            initDB();
//            byte[] bytes = dbHelper.retrieveImageFromDB();
//            Log.d("byte load from DB",bytes.toString());
//            dbHelper.close();
//            // Show Image from DB in ImageView
//            profileImg.setImageBitmap(Utils.getImage(bytes));
//        } catch (Exception e) {
//            //Log.e(TAG, "<loadImageFromDB> Error : " + e.getLocalizedMessage());
//            dbHelper.close();
//        }
    }
    //Database : close database
    private void closeDB(){
        dbHelper.close();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setToolbar(){
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isElder()) {
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(ProfileActivity.this, FamilyActivity.class));
                    finish();
                }
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences ----------------------------------------//
    //--------------------------------------------------------------------------------------------//

    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
        //settings = getSharedPreferences(data,0);
        //return settings.getBoolean(elderlyMode,false);
    }
}
