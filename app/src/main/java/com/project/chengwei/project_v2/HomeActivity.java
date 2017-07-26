package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppLaunchChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSION = 10;
    private SQLiteDBHelper dbHelper;
    private ImageButton btn_phone;
    private ImageButton btn_video;
    private ImageButton btn_map;
    private ImageButton btn_magnifier;
    private ImageButton btn_sos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_person_white);

        findViews();
        setListeners();

        // SOS Button
        btn_sos.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SosActivity();
                return true;
            }
        });

        initDB(); //Database : initial and insert profile data
        closeDB(); //Database : get data from database profile_tbl

        //Go to profile page
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(homeIntent);
            }
        });
    }

    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        btn_phone = (ImageButton)findViewById(R.id.btn_phone);
        btn_video = (ImageButton)findViewById(R.id.btn_video);
        btn_map = (ImageButton)findViewById(R.id.btn_map);
        btn_magnifier = (ImageButton)findViewById(R.id.btn_magnifier);
        btn_sos = (ImageButton)findViewById(R.id.btn_sos);
    }

    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners(){
        btn_phone.setOnClickListener(ImageBtnListener);
        btn_video.setOnClickListener(ImageBtnListener);
        btn_map.setOnClickListener(ImageBtnListener);
        btn_magnifier.setOnClickListener(ImageBtnListener);
    }

    //--------------------------------------------------------------------------------------------//
    //------------------------------------ ImageBtnListener --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private android.view.View.OnClickListener ImageBtnListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_phone:
                    //Toast.makeText(HomeActivity.this, "phone !", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(HomeActivity.this , ContactListActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_video:
                    //Toast.makeText(HomeActivity.this, "video !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeActivity.this, VideoActivity.class));
                    break;
                case R.id.btn_map:
                    //Toast.makeText(HomeActivity.this, "map !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeActivity.this, NavigationActivity.class));
                    break;
                case R.id.btn_magnifier:
                    Toast.makeText(HomeActivity.this, "mag !", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(HomeActivity.this, MagnifierActivity.class));
                    break;
            }
        }
    };
    //--------------------------------------------------------------------------------------------//
    //-------------------------- Version and Permission ------------------------------------------//
    //--------------------------------------------------------------------------------------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void SosActivity(){
        Intent homeIntent = new Intent(HomeActivity.this, SosActivity.class);
        startActivity(homeIntent);
    }

    // 位置情報許可の確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        }
        // 拒否していた場合
        else {
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this, "必須要許可GPS", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "無法使用地圖", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }



    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database and show the profile saved in the database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
    }

    //Database : close database
    private void closeDB() {
        dbHelper.close();
    }
}
