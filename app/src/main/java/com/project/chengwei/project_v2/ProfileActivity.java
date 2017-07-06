package com.project.chengwei.project_v2;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewAddress;
    private TextView textViewBirthday;
    private ImageView profileImg;
    private ImageButton btn_editProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViews();

        //Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);
        //Go to home page
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(homeIntent);
            }
        });
        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(ProfileActivity.this, AddProfileActivity.class);
                startActivity(homeIntent);
            }
        });

        //Database : initial and insert profile data
        initDB();
        //Database : get data from database profile_tbl
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        textViewName.setText( cursor.getString(cursor.getColumnIndex("name")) );
        textViewPhone.setText( cursor.getString(cursor.getColumnIndex("phone")) );
        textViewAddress.setText( cursor.getString(cursor.getColumnIndex("address")) );
        textViewBirthday.setText( cursor.getString(cursor.getColumnIndex("birthday")) );

        // Load image from Database
        try {
            initDB();
            byte[] bytes = dbHelper.retrieveImageFromDB();
            Log.d("byte load from DB",bytes.toString());
            dbHelper.close();
            // Show Image from DB in ImageView
            profileImg.setImageBitmap(Utils.getImage(bytes));
        } catch (Exception e) {
            //Log.e(TAG, "<loadImageFromDB> Error : " + e.getLocalizedMessage());
            dbHelper.close();
        }
        closeDB();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewPhone = (TextView) findViewById(R.id.textViewPhone);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewBirthday = (TextView) findViewById(R.id.textViewBirthday);
        profileImg = (ImageView) findViewById(R.id.profileImg);
        btn_editProfile = (ImageButton) findViewById(R.id.btn_editProfile);
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database and show the profile saved in the database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
    }
    //Database : close database
    private void closeDB(){
        dbHelper.close();
    }
}
