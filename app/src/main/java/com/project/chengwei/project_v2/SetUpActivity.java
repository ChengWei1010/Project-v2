package com.project.chengwei.project_v2;

import android.*;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    final int RequestAudioCode = 307;
    private final int REQUEST_PERMISSION = 10;
    final int REQUEST_EXTERNAL_STORAGE = 999;
    final int REQUEST_IMAGE_CAPTURE = 99;
    final int REQUEST_CROP_IMAGE = 9;

    ImageButton btn_elder, btn_family;
    Button btn_next, btnCamera, btnChoose, btn_add;
    private ImageView r1, r2, r3, myPhoto;
    private TextView btn_create;
    boolean check=false, canStart=false;
    int pageId=1;
    String uriString;
    Intent cropIntent;
    Uri uri, uri_crop;
    Bitmap bitmap;
    File file;
    FileOutputStream fileoutputstream;
    ByteArrayOutputStream bytearrayoutputstream;

    Boolean Room=false, Pwd=false;

    private FrameLayout step1,step2,step3;
    private EditText editTextName,editTextGroupNum,editTextPhone,editTextGroupPwd;
    String uId,strName,strRoom,strPhone,strPwd,correctPwd,strImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDBref1,mDBref2,mDatabaseRef;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    static String LoggedIn_User_mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        OneSignal.startInit(this).init();
        pageId=1;

        checkPermission();
        signIn();
        initDB();
        findViews();
        setUpListeners();
        detectFocusName();
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
        int audioPermission = ActivityCompat.checkSelfPermission(SetUpActivity.this, Manifest.permission.RECORD_AUDIO);

        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED ||
                callPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED ||
                locationPermission != PackageManager.PERMISSION_GRANTED || smsPermission != PackageManager.PERMISSION_GRANTED ||
                audioPermission != PackageManager.PERMISSION_GRANTED) {
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
        //拒絕錄音
        else if (ActivityCompat.shouldShowRequestPermissionRationale(SetUpActivity.this, Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(SetUpActivity.this)
                    .setMessage("此應用程式需要錄製聲音功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SetUpActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    RequestAudioCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SetUpActivity.this,"Audio Permission Canceled",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //接受
        else{
            ActivityCompat.requestPermissions(SetUpActivity.this,new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.CALL_PHONE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE ,android.Manifest.permission.SEND_SMS, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO},
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
            case RequestAudioCode: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetUpActivity.this,"Audio Permission Granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SetUpActivity.this,"Audio Permission Canceled",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        btn_next = findViewById(R.id.btn_next);
        r1 = findViewById(R.id.r1);
        r2 = findViewById(R.id.r2);
        r3 = findViewById(R.id.r3);
        editTextName = findViewById(R.id.edit_name);
        editTextPhone = findViewById(R.id.edit_phone);
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        myPhoto = findViewById(R.id.myPhoto);
        btnCamera = findViewById(R.id.cameraBtn);
        btnChoose = findViewById(R.id.chooseBtn);
        btn_create = findViewById(R.id.btn_create);
        editTextGroupNum = findViewById(R.id.editTextGroupNum);
        editTextGroupPwd = findViewById(R.id.editTextGroupPwd);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------------- Listeners --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setUpListeners(){
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camIntent,REQUEST_IMAGE_CAPTURE);
            }
        });

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (pageId){
                    case 1:
                        if(hasNamePhone()){
                            showStep2();
                            pageId=2;
                        }break;
                    case 2:
                        if(savePhoto()) {
                            showStep3();
                            detectFocusRoom();
                            pageId=3;
                        }break;
                    case 3:
                        checkValidRoomNum();
                        checkValidPwdNum();
                        if(Room && Pwd) {
                            showMessage("welcome!");
                        }
//                        if(isValidRoomNum() && isValidPwd()){
//                            showMessage("start!");
//                        }
//                        else{
//                            showMessage("群組或密碼錯誤");
//                        }
                        break;
                    default:
                        showMessage("error");
                }
            }
        });
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //showMessage("create");
                strName = editTextName.getText().toString();
                strRoom = editTextGroupNum.getText().toString();
                FireBaseCreateGroup();
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------------- FireBase ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
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

                            //send Tag to Onesignal
                            LoggedIn_User_mId = user.getUid().toString();
                            OneSignal.sendTag("User_ID", LoggedIn_User_mId);
                            //Toast.makeText(SetUpActivity.this, "login success. "+user.getUid(),Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("hi", "signInAnonymously:failure", task.getException());

                            showMessage("請檢查網路連線");
                        }

                    }
                });
    }
    public void FireBasePutData(String uId, String strName, String strRoom, String strPhone, String strImage, String mHour, String mMinute) {
        mDBref1 = FirebaseDatabase.getInstance().getReference("groups").child(strRoom).child("members");
        mDBref2 = FirebaseDatabase.getInstance().getReference("members");

        Map<String, String> userData = new HashMap<>();
        userData.put("mId", uId);
        userData.put("mName", strName);
        userData.put("mGroup", strRoom);
        userData.put("mPhone",strPhone);
        userData.put("mImage",strImage);
        userData.put("TimeHour",mHour);
        userData.put("TimeMinute",mMinute);
        mDBref1.child(uId).setValue(userData);
        mDBref2.child(uId).setValue(userData);
    }
    public void FireBaseCreateGroup() {
        int randomGroupNum = (int)(Math.random()*9000)+1000;
        strRoom = Integer.toString(randomGroupNum);

        int randomGroupPwd = (int)(Math.random()*9000)+1000;
        strPwd = Integer.toString(randomGroupPwd);

        mDBref1 = FirebaseDatabase.getInstance().getReference("groups").child(strRoom);
        Map<String, String> roomPwd = new HashMap<>();
        roomPwd.put("pwd", strPwd);
        mDBref1.setValue(roomPwd);

        Toast.makeText(SetUpActivity.this, "create room: " + strRoom, Toast.LENGTH_SHORT).show();
        editTextGroupNum.setText(strRoom);
        editTextGroupNum.setEnabled(false);
        editTextGroupPwd.setText(strPwd);
        editTextGroupPwd.setEnabled(false);
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
    private void detectFocusName(){
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                //如果字數達到10，取消自己焦點，隱藏虛擬鍵盤
                if(editTextPhone.getText().toString().length()==10) {
                    editTextPhone.clearFocus();
                    IBinder mIBinder = SetUpActivity.this.getCurrentFocus().getWindowToken();
                    InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
                    btn_next.setBackgroundResource(R.drawable.next);
                }
            }
        };

        //加入文字監聽
        editTextName.addTextChangedListener(mTextWatcher);
        editTextPhone.addTextChangedListener(mTextWatcher);
    }
    private void detectFocusRoom(){
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
//                //如果字數達到4，取消自己焦點，下一個EditText取得焦點
//                if(editTextGroupNum.getText().toString().length()==4) {
//                    editTextGroupNum.clearFocus();
//                    editTextGroupNum.requestFocus();
//                }
                if(editTextGroupPwd.getText().toString().length()==4) {
                    editTextGroupPwd.clearFocus();
                    IBinder mIBinder = SetUpActivity.this.getCurrentFocus().getWindowToken();
                    InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
                    btn_next.setBackgroundResource(R.drawable.start);
                    //下方為顯示虛擬鍵盤
                    //mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        };
        //加入文字監聽
        editTextGroupNum.addTextChangedListener(mTextWatcher);
        editTextGroupPwd.addTextChangedListener(mTextWatcher);
    }
    private void showStep2(){
        btn_next.setBackgroundResource(R.drawable.next0);
        step1.setVisibility(FrameLayout.INVISIBLE);
        step2.setVisibility(FrameLayout.VISIBLE);
        r1.setBackgroundResource(R.drawable.r01);
        r2.setBackgroundResource(R.drawable.r2);
        r3.setBackgroundResource(R.drawable.r03);
        strName = editTextName.getText().toString();
    }
    private void showStep3(){
        btn_next.setBackgroundResource(R.drawable.start0);
        step2.setVisibility(FrameLayout.INVISIBLE);
        step3.setVisibility(FrameLayout.VISIBLE);

        r1.setBackgroundResource(R.drawable.r01);
        r2.setBackgroundResource(R.drawable.r02);
        r3.setBackgroundResource(R.drawable.r3);
        strRoom = editTextGroupNum.getText().toString();
    }
    private void start(){
        saveSQLite();
        FireBasePutImage(uri_crop);
        startActivity(new Intent(SetUpActivity.this, WelcomeActivity.class));
        finish();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- SQLiteDB -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
    }
    public void saveSQLite() {
        String hadsetup = "1";

        initDB();
        Cursor cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        dbHelper.setProfileData(uId ,hadsetup, strName, strRoom, strPhone);
        dbHelper.setNotification(0);

        Cursor cursor_time = dbHelper.getSendTime();
        cursor.moveToPosition(0);
        dbHelper.setSendTime(00,00);
        dbHelper.setNotification(0);
        closeDB();
    }
    private void closeDB(){
        dbHelper.close();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- save photo -----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //選擇相簿裡的照片
        if(requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK && data != null){
            uri = data.getData();
            cropImage();
        }
        //相機拍的照片
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null){
            uri = data.getData();
            cropImage();
        }
        //裁剪照片後顯示
        else if(requestCode == REQUEST_CROP_IMAGE && resultCode == RESULT_OK && data != null){
            bytearrayoutputstream = new ByteArrayOutputStream();

            Bundle bundle = data.getExtras();
            bitmap = bundle.getParcelable("data");
            myPhoto.setImageBitmap(bitmap);
            btn_next.setBackgroundResource(R.drawable.next);
            savePhoto();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void cropImage() {
        try{
            cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri,"image/*");

            cropIntent.putExtra("crop","true");
            cropIntent.putExtra("outputX",1000);
            cropIntent.putExtra("outputY",1000);
            cropIntent.putExtra("aspectX",1);
            cropIntent.putExtra("aspectY",1);
            cropIntent.putExtra("noFaceDetection", true);
            cropIntent.putExtra("scaleUpIfNeeded",true);
            cropIntent.putExtra("return-data",true);

            startActivityForResult(cropIntent,REQUEST_CROP_IMAGE);
        }
        catch (ActivityNotFoundException ex){
        }
    }
    private boolean savePhoto(){
        if(bitmap == null){
            showMessage("請設置照片！");
            //TODO : 預設一張照片給它
            //bitmap = DrawableCompat();
            pageId = 2;
            return false;
            //return true;
        }else{
            //儲存剪裁後的照片到外部空間
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearrayoutputstream);

            file = new File(Environment.getExternalStorageDirectory(),
                    "crop_image" + String.valueOf(System.currentTimeMillis()) + ".jpg");

            uri_crop = Uri.fromFile(file);
            uriString = uri_crop.toString();

            try {
                file.createNewFile();
                fileoutputstream = new FileOutputStream(file);
                fileoutputstream.write(bytearrayoutputstream.toByteArray());
                fileoutputstream.close();
                //showMessage("image ready");
            } catch (Exception e) {
                e.printStackTrace();
            }
//            //儲存名字,照片進去資料庫
//            try {
//                dbHelper.setProfileImg(strName, uriString);
//                //showMessage("SQLite added");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return true;
        }
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------- Store Image to Firebase------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void FireBasePutImage(Uri filePath){
        //Create file metadata including the content type
        //If you do not provide a contentType and Cloud Storage cannot infer a default from the file extension, Cloud Storage uses application/octet-stream.
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        if (filePath != null) {
            mStorage = FirebaseStorage.getInstance();
            mStorageRef = mStorage.getReference();
            StorageReference ref = mStorageRef.child("images").child(strRoom).child(uId+".jpg");
            //Storage upload
            ref.putFile(filePath, metadata)
                    //上傳完畢
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //取得下載的url
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            strImage = downloadUri.toString();
                            //Database Upload
                            FireBasePutData(uId, strName, strRoom, strPhone, strImage, "00", "00");
                            Log.d("Upload","Success");

                            //儲存名字,照片進去SQLite
                            try {
                                dbHelper.setProfileImg(strName, strImage);
                                Log.d("Store into SQLite","Success");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    //上傳失敗
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("Upload","Failed");
                        }
                    })
                    //上傳中
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- check valid ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private boolean hasNamePhone(){
        strName = editTextName.getText().toString();
        strPhone = editTextPhone.getText().toString();
        if(strName.isEmpty()){
            showMessage("請輸入姓名！");
            pageId = 1;
            return false;
        }
        else if(strPhone.isEmpty()){
            showMessage("請輸入手機！");
            pageId = 1;
            return false;
        }
        else{
            return true;
        }
    }
//    private boolean hasName(){
//        strName = editTextName.getText().toString();
//        if(strName.isEmpty()){
//            showMessage("請輸入姓名！");
//            pageId = 1;
//            return false;
//        }else{
//            return true;
//        }
//    }
//
//    private boolean hasPhone(){
//        strPhone = editTextPhone.getText().toString();
//        if(strPhone.isEmpty()){
//            showMessage("請輸入手機！");
//            pageId = 1;
//            return false;
//        }else{
//
//            return true;
//        }
//    }
    private void checkValidRoomNum() {
        strRoom = editTextGroupNum.getText().toString();
        if (strRoom.length() != 4 || strRoom.equals(null)) {
            btn_next.setBackgroundResource(R.drawable.start0);
            Log.e("QQ", "room not valid");
            showMessage("群組有誤");
            pageId = 3;
            Room = false;
        }else{
            Room = true;
        }
    }
    private void checkValidPwdNum(){
        strPwd = editTextGroupPwd.getText().toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(strRoom).child("pwd");
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                correctPwd = dataSnapshot.getValue(String.class);
                if (strRoom.length() == 4 && strPwd.equals(correctPwd)) {
                    Log.e("YA","pwd correct");
                    btn_next.setBackgroundResource(R.drawable.start);
                    Pwd = true;
                    start();
                    showMessage("start");
                } else{
                    btn_next.setBackgroundResource(R.drawable.start0);
                    Log.e("QQ","pwd not correct");
                    showMessage("密碼有誤");
                    pageId = 3;
                    Pwd = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
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
}

