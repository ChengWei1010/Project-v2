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
    String uId,strName,strRoom,strStatus,strPwd,correctPwd,strImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDBref1,mDBref2,mDatabaseRef;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        pageId=1;

        checkPermission();
        initDB();
        signIn();
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
        btn_next = findViewById(R.id.btn_next);
        r1 = findViewById(R.id.r1);
        r2 = findViewById(R.id.r2);
        r3 = findViewById(R.id.r3);
        editTextName = findViewById(R.id.edit_name);
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        myPhoto = findViewById(R.id.myPhoto);
        btnCamera = findViewById(R.id.cameraBtn);
        btnChoose = findViewById(R.id.chooseBtn);
        btn_create = findViewById(R.id.btn_create);
        editTextGroupNum = findViewById(R.id.editTextGroupNum);
        editTextGroupPwd = findViewById(R.id.editTextGroupPwd);
        btn_elder = findViewById(R.id.btn_elder);
        btn_family = findViewById(R.id.btn_family);
        //        editTextName.setSelectAllOnFocus(true);
        //        editTextGroupNum.setSelectAllOnFocus(true);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------------- Listeners --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setUpListeners(){
        btn_elder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_elder.setBackgroundResource(R.drawable.btn_elder);
                btn_family.setBackgroundResource(R.drawable.btn_family0);
                getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(ELDERLY_MODE, true).commit();
                if(hasName()) {
                    //showMessage("e");
                    strStatus = "e";
                    btn_next.setBackgroundResource(R.drawable.next);
                }
            }
        });
        btn_family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_family.setBackgroundResource(R.drawable.btn_family);
                btn_elder.setBackgroundResource(R.drawable.btn_elder0);
                getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(ELDERLY_MODE, false).commit();
                if(hasName()) {
                    strStatus = "f";
                    btn_next.setBackgroundResource(R.drawable.next);
                }
            }
        });

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
                        if(hasName() && hasStatus()){
                            showStep2();
                            pageId=2;
                        }break;
                    case 2:
                        if(savePhoto()) {
                            showStep3();
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
                strStatus = "f";
                FireBaseCreateGroup();
            }
        });
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
                            showMessage("請檢查網路連線");
                        }

                    }
                });
    }
    public void FireBasePutData(String uId, String strName, String strRoom, String strStatus, String strPhone, String strImage) {
        mDBref1 = FirebaseDatabase.getInstance().getReference("groups").child(strRoom).child("members");
        mDBref2 = FirebaseDatabase.getInstance().getReference("members");

        Map<String, String> userData = new HashMap<>();
        userData.put("mId", uId);
        userData.put("mName", strName);
        userData.put("mGroup", strRoom);
        userData.put("mStatus",strStatus);
        userData.put("mPhone",strPhone);
        userData.put("mImage",strImage);
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
        if(isElder()) {
            strStatus = "e";
            saveSQLite();
            FireBasePutImage(uri_crop);
            //FireBasePutData(uId, strName, strRoom, strStatus, getMyPhoneNumber());
            startActivity(new Intent(SetUpActivity.this, HomeActivity.class));
            finish();
        }else{
            strStatus = "f";
            saveSQLite();
            FireBasePutImage(uri_crop);
            //FireBasePutData(uId, strName, strRoom, strStatus, getMyPhoneNumber());
            startActivity(new Intent(SetUpActivity.this, FamilyActivity.class));
            finish();
        }
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
        dbHelper.setProfileData(uId ,hadsetup, strName, strRoom, getMyPhoneNumber());
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
            //儲存名字,照片進去資料庫
            try {
                dbHelper.setProfileImg(strName, uriString);
                //showMessage("SQLite added");
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                            FireBasePutData(uId, strName, strRoom, strStatus, getMyPhoneNumber(), strImage);
                            Log.d("Upload","Success");
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
    private boolean hasName(){
        strName = editTextName.getText().toString();
        if(strName.isEmpty()){
            showMessage("請輸入姓名！");
            pageId = 1;
            return false;
        }else{
            return true;
        }
    }

    private boolean hasStatus(){
        if(strStatus.isEmpty()){
            showMessage("請點選身份！");
            pageId = 1;
            return false;
        }else{
            return true;
        }
    }
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
//    public boolean isValidRoomNum() {
//        strRoom = editTextGroupNum.getText().toString();
//        if (strRoom.length() != 4 || strRoom.equals(null)) {
//             btn_next.setBackgroundResource(R.drawable.start0);
//             Log.e("QQ","room not valid");
//             showMessage("請輸入正確的群組號碼");
//             pageId = 3;
//             return false;
//         }else{
//             return true;
//         }
//    }


//    public boolean isValidPwd(){
//        strPwd = editTextGroupPwd.getText().toString();
//        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(strRoom).child("pwd");
//        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                correctPwd = dataSnapshot.getValue(String.class);
//                if (strRoom.length() == 4 && strPwd.equals(correctPwd)) {
//                    Log.e("YA","pwd correct");
//                    btn_next.setBackgroundResource(R.drawable.start);
//                    start();
//                    check = true;
//                } else{
//                    btn_next.setBackgroundResource(R.drawable.start0);
//                    Log.e("QQ","pwd not correct");
//                    showMessage("密碼錯誤");
//                    pageId = 3;
//                    check = false;
//                }
//            }
//
//        @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//        return check;
//
//    }
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
    //------------------------------------ CheckPreferences --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
    }
}

