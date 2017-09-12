package com.project.chengwei.project_v2;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SetUpActivity extends AppCompatActivity {
    static final String KEY =  "com.<your_app_name>";
    static final String GROUP_NUM =  "GROUP_NUM";
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    private SQLiteDBHelper dbHelper;

    final int RequestCameraCode = 1;
    final int RequestCallCode = 2;
    final int RequestExternalStorageCode = 3;
    final int RequestLocationCode = 4;
    final int RequestSmsCode = 5;
    final int RequestPermissionCode = 999;
    private final int REQUEST_PERMISSION = 10;

    private ImageButton btn_elder, btn_family;
    private Button btn_start,btn_create,btn_back;
    private FrameLayout guide_room,guide_create_room;
    private EditText editTextName,editTextGroupNum,editTextPhone;
    //private EditText edit_group_num1,edit_group_num2,edit_group_num3,edit_group_num4;
    private TextView instruction1,instruction2,instruction3;
    private String uId,strName,strRoom,strStatus;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBref1,mDBref2;
    //private String uuId = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        signIn();
        checkPermission();
        findViews();
        setUpListeners();
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------- Version and Permission ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void checkPermission() {
        int cameraPermission = ActivityCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.CAMERA);
        int readPermission = ActivityCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int callPermission = ActivityCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.CALL_PHONE);
        int smsPermission = ActivityCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.SEND_SMS);
        int locationPermission = ActivityCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED ||
                callPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED ||
                locationPermission != PackageManager.PERMISSION_GRANTED || smsPermission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            RequestRuntimePermission();
        }
    }
    private void RequestRuntimePermission() {
        //拒絕相機
        if (ActivityCompat.shouldShowRequestPermissionRationale(SetUpActivity.this, android.Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(SetUpActivity.this)
                    .setMessage("此應用程式需要CAMERA功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetUpActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    RequestCameraCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SetUpActivity.this,"Camera Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕電話
        else if (ActivityCompat.shouldShowRequestPermissionRationale(SetUpActivity.this,
                android.Manifest.permission.CALL_PHONE)) {
            new AlertDialog.Builder(SetUpActivity.this)
                    .setMessage("此應用程式需要CALL_PHONE功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetUpActivity.this,
                                    new String[]{android.Manifest.permission.CALL_PHONE},
                                    RequestCallCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SetUpActivity.this,"Call Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕讀取及寫入
        else if (ActivityCompat.shouldShowRequestPermissionRationale(SetUpActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(SetUpActivity.this)
                    .setMessage("此應用程式需要READ及WRITE_EXTERNAL_STORAGE功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetUpActivity.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    RequestExternalStorageCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SetUpActivity.this,"External Storage Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕SMS
        else if (ActivityCompat.shouldShowRequestPermissionRationale(SetUpActivity.this,
                Manifest.permission.SEND_SMS)) {
            new AlertDialog.Builder(SetUpActivity.this)
                    .setMessage("此應用程式需要SEND_SMS功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetUpActivity.this,
                                    new String[]{android.Manifest.permission.SEND_SMS},
                                    RequestSmsCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SetUpActivity.this,"SMS Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕位置
        else if (ActivityCompat.shouldShowRequestPermissionRationale(SetUpActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(SetUpActivity.this)
                    .setMessage("此應用程式需要ACCESS_FINE_LOCATION功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetUpActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    RequestCallCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SetUpActivity.this,"Location Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //接受
        else{
            ActivityCompat.requestPermissions(SetUpActivity.this,new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.CALL_PHONE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE ,android.Manifest.permission.SEND_SMS, android.Manifest.permission.ACCESS_FINE_LOCATION},
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
                    Toast.makeText(SetUpActivity.this,"Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SetUpActivity.this,"Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestCameraCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetUpActivity.this,"Camera Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SetUpActivity.this,"Camera Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestCallCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetUpActivity.this,"Call Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SetUpActivity.this,"Call Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestExternalStorageCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetUpActivity.this,"External Storage Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SetUpActivity.this,"External Storage Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestLocationCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetUpActivity.this,"Location Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SetUpActivity.this,"Location Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestSmsCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetUpActivity.this,"SMS Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SetUpActivity.this,"SMS Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        guide_room = findViewById(R.id.guide_room);
        guide_create_room = findViewById(R.id.guide_create_room);
        instruction1 = findViewById(R.id.instruction1);
        instruction2 = findViewById(R.id.instruction2);
        instruction3 = findViewById(R.id.instruction3);
        //editTextRoom = (EditText) findViewById(R.id.edit_group_num);
        editTextName = findViewById(R.id.edit_name);
        editTextGroupNum  = findViewById(R.id.editTextGroupNum);

        editTextName.setSelectAllOnFocus(true);
        editTextGroupNum.setSelectAllOnFocus(true);
        btn_elder = findViewById(R.id.btn_elder);
        btn_family = findViewById(R.id.btn_family);
        btn_start = findViewById(R.id.btn_start);
        btn_create = findViewById(R.id.btn_create);
        btn_back = findViewById(R.id.btn_back);
//        edit_group_num1  = (EditText) findViewById(R.id.edit_group_num1);
//        edit_group_num2  = (EditText) findViewById(R.id.edit_group_num2);
//        edit_group_num3  = (EditText) findViewById(R.id.edit_group_num3);
//        edit_group_num4  = (EditText) findViewById(R.id.edit_group_num4);
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------- Onclick Listeners --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void setUpListeners(){
        btn_elder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(ELDERLY_MODE, true).commit();
                if(hasName()) {
                    signIn();
                    showSelectRoom();
                }
            }
        });
        btn_family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(ELDERLY_MODE, false).commit();
                if(hasName()) {
                    signIn();
                    showSelectRoom();
                    showCreateRoom();
                }
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strName = editTextName.getText().toString();
                strRoom = editTextGroupNum.getText().toString();
//                String num1 = edit_group_num1.getText().toString();
//                String num2 = edit_group_num2.getText().toString();
//                String num3 = edit_group_num3.getText().toString();
//                String num4 = edit_group_num4.getText().toString();
//                strRoom = num1 + num2 + num3 +num4;

                if(hasName()==true && isValidRoomNum(strRoom)==true) {
                    saveSQLite();
                    if (isElder()) {
                        strStatus = "e";
                        FireBasePutData(uId, strName, strRoom, strStatus, getMyPhoneNumber());
                        ElderEnter();
                    } else {
                        strStatus = "f";
                        FireBasePutData(uId, strName, strRoom, strStatus, getMyPhoneNumber());
                        FamilyEnter();
                    }
                }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSelectRoom();
            }
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strName = editTextName.getText().toString();
                strRoom = editTextGroupNum.getText().toString();
                strStatus = "f";
                FireBaseCreateGroup(uId, strName,strStatus);
                saveSQLite();
                //FamilyEnter();
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------------- FireBase ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private String getMyPhoneNumber(){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if(mTelephonyMgr.getLine1Number()==null){
            alertSetPhone();
            return "未設置手機號碼";
        }else
        return mTelephonyMgr.getLine1Number();
    }
    public void signIn(){
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("hi", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            uId = user.getUid();
                            //Toast.makeText(SetUpActivity.this, "login success. "+user.getUid(),Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("hi", "signInAnonymously:failure", task.getException());
                            Toast.makeText(SetUpActivity.this, "請檢查網路連線",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    public void FireBasePutData(String uId, String strName, String strRoom, String strStatus, String strPhone) {
        mDBref1 = FirebaseDatabase.getInstance().getReference("groups").child(strRoom).child("members");
        mDBref2 = FirebaseDatabase.getInstance().getReference("members");

        Map<String, String> userData = new HashMap<>();
        userData.put("mId", uId);
        userData.put("mName", strName);
        userData.put("mGroup", strRoom);
        userData.put("mStatus",strStatus);
        userData.put("mPhone",strPhone);
        mDBref1.child(uId).setValue(userData);
        mDBref2.child(uId).setValue(userData);
    }
    public void FireBaseCreateGroup(String uId, String strName, String strStatus) {
        int randomGroupNum = (int)(Math.random()*9000)+1000;
        String createRoom = Integer.toString(randomGroupNum);
        strRoom = createRoom;
        FireBasePutData(uId, strName, strRoom, strStatus, getMyPhoneNumber());
        Toast.makeText(SetUpActivity.this, "create room: " + createRoom, Toast.LENGTH_SHORT).show();
        editTextGroupNum.setText(strRoom);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- show UI --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void showSelectRoom(){
        btn_back.setVisibility(FrameLayout.VISIBLE);
        instruction1.setVisibility(FrameLayout.INVISIBLE);
        editTextName.setVisibility(FrameLayout.INVISIBLE);
        guide_room.setVisibility(FrameLayout.VISIBLE);
        btn_elder.setClickable(false);
        btn_family.setClickable(false);
        guideAnimation();
    }
    public void showCreateRoom(){
        btn_back.setVisibility(FrameLayout.VISIBLE);
        guide_create_room.setVisibility(FrameLayout.VISIBLE);
        guideAnimation();
    }
    public void hideSelectRoom(){
        btn_back.setVisibility(FrameLayout.INVISIBLE);
        instruction1.setVisibility(FrameLayout.VISIBLE);
        editTextName.setVisibility(FrameLayout.VISIBLE);
        guide_room.setVisibility(FrameLayout.INVISIBLE);
        guide_create_room.setVisibility(FrameLayout.INVISIBLE);
        btn_elder.setClickable(true);
        btn_family.setClickable(true);
    }
    public void ElderEnter(){
        Intent intent = new Intent();
        intent.setClass(SetUpActivity.this , HomeActivity.class);
        startActivity(intent);
        finish();
    }
    public void FamilyEnter(){
        startActivity(new Intent(SetUpActivity.this, FamilyActivity.class));
        finish();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- SQLiteDB -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
    }
    //Database : save the change to database
    public void saveSQLite() {
        String hadsetup = "1";

        initDB();
        Cursor cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        dbHelper.setProfileData(uId ,hadsetup, strName, strRoom, getMyPhoneNumber());
        closeDB();
    }
    //Database : close database
    private void closeDB(){
        dbHelper.close();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- check valid ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private boolean hasName(){
        strName = editTextName.getText().toString();
        if(strName.isEmpty()){
            showMessage("請輸入姓名！");
            return false;
        }else{
            return true;
        }
    }
    private boolean isValidRoomNum(String editTextRoom){
        if(editTextRoom.length()!=4){
            showMessage("請出入正確的群組號碼");
            return false;
        }
        return true;
    }
    // Show simple message using SnackBar
    private void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
    public void alertSetPhone() {
        Toast.makeText(SetUpActivity.this, "請檢查SIM卡，或設定手機號碼", Toast.LENGTH_SHORT).show();
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                finish();
//            }
//        }, 2 * 1000);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------ setAnimationListener ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void guideAnimation() {
        //AlphaAnimation(float fromAlpha, float toAlpha)
        //fromAlpha 起始透明度(Alpha)值
        //toAlpha 最後透明度(Alpha)值
        // 1.0f~0.0f
        //Animation am = new AlphaAnimation(1.0f, 0.0f);
        //setDuration (long durationMillis) 設定動畫開始到結束的執行時間
        //am.setDuration(2000);
        //setRepeatCount (int repeatCount) 設定重複次數 -1為無限次數 0
        //am.setRepeatCount(-1);
        //將動畫參數設定到圖片並開始執行動畫
        //btn_start.startAnimation(am);

//        final TranslateAnimation run = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
//        run.setDuration(2000);


//        btn_start.startAnimation(run);
//        run.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation arg0) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation arg0) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//                // TODO Auto-generated method stub
//                run.setFillAfter(true);
//            }
//        });
    }
}

