package com.project.chengwei.project_v2;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    static final String KEY =  "com.<your_app_name>";
    static final String GROUP_NUM =  "0000";
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private Toolbar myToolbar;
    final int RequestCameraCode = 1;
    final int RequestCallCode = 2;
    final int RequestExternalStorageCode = 3;
    final int RequestLocationCode = 4;
    final int RequestPermissionCode = 999;
    private final int REQUEST_PERMISSION = 10;
    private ImageButton btn_phone;
    private ImageButton btn_video;
    private ImageButton btn_map;
    private ImageButton btn_magnifier;
    private ImageButton btn_sos;
    private ImageButton btn_guide_ok;
    private FrameLayout help_guide;
    private TextClock textClock;
    private TextView text_group_name;
    private String groupNum, myName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }

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
        btn_phone = (ImageButton)findViewById(R.id.btn_phone);
        btn_video = (ImageButton)findViewById(R.id.btn_video);
        btn_map = (ImageButton)findViewById(R.id.btn_map);
        btn_magnifier = (ImageButton)findViewById(R.id.btn_magnifier);
        btn_sos = (ImageButton)findViewById(R.id.btn_sos);
        btn_guide_ok = (ImageButton)findViewById(R.id.btn_guide_ok);
        help_guide = (FrameLayout)findViewById(R.id.help_guide);
        textClock = (TextClock)findViewById(R.id.textClock);
        text_group_name = (TextView)findViewById(R.id.text_group_name);
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
        // SOS Button
        btn_sos.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SosActivity();
                return true;
            }
        });
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
                    finish();
                    break;
                case R.id.btn_video:
                    //startActivity(new Intent(HomeActivity.this, WatchVideoActivity.class));
                    Intent intent = new Intent(getApplicationContext(),VideoElderActivity.class);
                    intent.putExtra("room_name",groupNum);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.btn_map:
                    //Toast.makeText(HomeActivity.this, "map !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeActivity.this, NavigationActivity.class));
                    finish();
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

    //--------------------------------------------------------------------------------------------//
    //-------------------------- Version and Permission ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void checkPermission() {
        int cameraPermission = ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.CAMERA);
        int readPermission = ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int callPermission = ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE);
        int locationPermission = ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED ||
        callPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED ||
        locationPermission != PackageManager.PERMISSION_GRANTED) {
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
        //拒絕位置
        else if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage("此應用程式需要ACCESS_FINE_LOCATION功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    RequestCallCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(HomeActivity.this,"Location Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //接受
        else{
            ActivityCompat.requestPermissions(HomeActivity.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION},
                    RequestPermissionCode);
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
            case RequestLocationCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(HomeActivity.this,"Location Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(HomeActivity.this,"Location Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setToolbar(){
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setNavigationIcon(R.drawable.ic_person_white);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                finish();
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        groupNum = cursor.getString(cursor.getColumnIndex("room"));
        text_group_name.setText(groupNum);
    }
    //Database : close database
    private void closeDB(){
        dbHelper.close();
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
}
