package com.project.chengwei.project_v2;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextClock;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;

public class HomeActivity extends AppCompatActivity {
    final int RequestCameraCode = 1;
    final int RequestCallCode = 2;
    final int RequestExternalStorageCode = 3;
    final int RequestPermissionCode = 999;
    private final int REQUEST_PERMISSION = 10;
    private SQLiteDBHelper dbHelper;
    private ImageButton btn_phone;
    private ImageButton btn_video;
    private ImageButton btn_map;
    private ImageButton btn_magnifier;
    private ImageButton btn_sos;
    private ImageButton btn_guide_ok;
    private FrameLayout help_guide;
    private TextClock textClock;
    private String groupNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
        //Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_person_white);
        //Go to home page
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViews();
        setListeners();

        //groupNum = getIntent().getExtras().get("groupNum").toString();
        //Toast.makeText(HomeActivity.this, "enter" + groupNum, Toast.LENGTH_SHORT).show();

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
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
        btn_guide_ok = (ImageButton)findViewById(R.id.btn_guide_ok);
        help_guide =(FrameLayout)findViewById(R.id.help_guide);
        textClock =(TextClock)findViewById(R.id.textClock);
    }

    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners(){
        btn_phone.setOnClickListener(ImageBtnListener);
        btn_video.setOnClickListener(ImageBtnListener);
        btn_map.setOnClickListener(ImageBtnListener);
        btn_magnifier.setOnClickListener(ImageBtnListener);
        btn_guide_ok.setOnClickListener(ImageBtnListener);
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
                    startActivity(new Intent(HomeActivity.this, ContactListActivity.class));
                    break;
                case R.id.btn_video:
                    //Toast.makeText(HomeActivity.this, "video !", Toast.LENGTH_SHORT).show();
                    Intent homeIntent = new Intent(getApplicationContext(),WatchVideoActivity.class);
                    homeIntent.putExtra("groupNum",groupNum);
                    finish();
                    startActivity(homeIntent);
                    break;
                case R.id.btn_map:
                    //Toast.makeText(HomeActivity.this, "map !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeActivity.this, NavigationActivity.class));
                    break;
                case R.id.btn_magnifier:
                    Toast.makeText(HomeActivity.this, "mag !", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(HomeActivity.this, MagnifierActivity.class));
                    break;
                case R.id.btn_guide_ok:
                    closeGuide();
                    break;
            }
        }
    };

    private void SosActivity(){
        Intent homeIntent = new Intent(HomeActivity.this, SosActivity.class);
        startActivity(homeIntent);
    }


    // 位置情報許可の確認
//    public void checkPermission() {
//        // 既に許可している
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//        }
//        // 拒否していた場合
//        else {
//            requestLocationPermission();
//        }
//    }

    // 許可を求める
//    private void requestLocationPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//            ActivityCompat.requestPermissions(HomeActivity.this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
//
//        } else {
//            Toast toast = Toast.makeText(this, "必須要許可GPS", Toast.LENGTH_SHORT);
//            toast.show();
//
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);
//
//        }
//    }

    // 結果の受け取り
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == REQUEST_PERMISSION) {
//            // 使用が許可された
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                return;
//
//            } else {
//                // それでも拒否された時の対応
//                Toast toast = Toast.makeText(this, "無法使用地圖", Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        }
//    }

    //--------------------------------------------------------------------------------------------//
    //-------------------------- Version and Permission ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void checkPermission() {
        int cameraPermission = ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.CAMERA);
        int readPermission = ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int callPermission = ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE);
        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED ||
        callPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            RequestRuntimePermission();
        }
    }

    private void RequestRuntimePermission() {
        //拒絕相機
        if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage("此應用程式需要CAMERA功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(HomeActivity.this,"Camera Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕電話
        else if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                Manifest.permission.CALL_PHONE)) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage("此應用程式需要CALL_PHONE功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    RequestCallCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(HomeActivity.this,"Call Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕讀取及寫入
        else if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage("此應用程式需要READ及WRITE_EXTERNAL_STORAGE功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    RequestExternalStorageCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(HomeActivity.this,"External Storage Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //接受
        else{
            ActivityCompat.requestPermissions(HomeActivity.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},RequestPermissionCode);
        }
    }

    //跳出權限要求時，按允許或拒絕
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(HomeActivity.this,"Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeActivity.this,"Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestCameraCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(HomeActivity.this,"Camera Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeActivity.this,"Camera Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestCallCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(HomeActivity.this,"Call Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeActivity.this,"Call Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestExternalStorageCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(HomeActivity.this,"External Storage Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeActivity.this,"External Storage Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------- Options Item -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                openGuide();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openGuide(){
        help_guide.setVisibility(View.VISIBLE);
        btn_sos.setClickable(false);
        btn_phone.setClickable(false);
        btn_video.setClickable(false);
        btn_map.setClickable(false);
        btn_magnifier.setClickable(false);
        textClock.setVisibility(View.INVISIBLE);
    }
    private void closeGuide(){
        help_guide.setVisibility(View.GONE);
        textClock.setVisibility(View.VISIBLE);
        btn_sos.setClickable(true);
        btn_phone.setClickable(true);
        btn_video.setClickable(true);
        btn_map.setClickable(true);
        btn_magnifier.setClickable(true);
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
