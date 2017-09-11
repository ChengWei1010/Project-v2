package com.project.chengwei.project_v2;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ProfileAddActivity extends AppCompatActivity {
    static final String KEY_IS_FIRST_TIME =  "com.<your_app_name>.first_time";
    static final String KEY =  "com.<your_app_name>";
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "HomeActivity";
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private EditText editTextName,editTextPhone,editTextAddress,editTextRoom;
    private DatePicker pickBirthday;
    private ImageButton btn_manageDB;
    private ImageButton btn_editPhoto;
    private ImageView ImgView_photo;
    private DatabaseReference dbRef1,dbRef2;
    private String uuId;

    final int REQUEST_EXTERNAL_STORAGE = 999;
    final int REQUEST_IMAGE_CAPTURE = 99;
    final int REQUEST_CROP_IMAGE = 9;
    Intent cropIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        findViews();
        setListeners();
        initDB();

        // Enable if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            btn_editPhoto.setEnabled(true);
        }
        // Else ask for permission
        else {
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        // Load image from Database
        try {
            initDB();
            byte[] bytes = dbHelper.retrieveImageFromDB();
            Log.d("byte load from DB",bytes.toString());
            dbHelper.close();
            // Show Image from DB in ImageView
            ImgView_photo.setImageBitmap(Utils.getImage(bytes));
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
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        //editTextRoom = findViewById(R.id.editTextRoom);
        pickBirthday = findViewById(R.id.pickBirthday);

        btn_manageDB = findViewById(R.id.btn_manageDB);
        btn_editPhoto = findViewById(R.id.btn_editPhoto);
        ImgView_photo = findViewById(R.id.ImgView_photo);
        Drawable drawable;
        Resources res = this.getResources();
        if(isElder()){
            drawable = res.getDrawable(R.drawable.ic_elder, getTheme());
            ImgView_photo.setBackground(drawable);
        }
        else{
            drawable = res.getDrawable(R.drawable.ic_family, getTheme());
            ImgView_photo.setBackground(drawable);
        }
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners(){
        btn_manageDB.setOnClickListener(ImageBtnListener);
        btn_editPhoto.setOnClickListener(ImageBtnListener);
        //Manage the Database by clicking a button
        btn_manageDB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent dbManager = new Intent(ProfileAddActivity.this, AndroidDatabaseManager.class);
                startActivity(dbManager);
            }
        });
    }

    private android.view.View.OnClickListener ImageBtnListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_manageDB:
                    Intent dbManager = new Intent(ProfileAddActivity.this,AndroidDatabaseManager.class);
                    startActivity(dbManager);
                    break;
                case R.id.btn_editPhoto:
                    openImageChooser();
                    break;
            }
        }
    };
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- About Profile Photo ---------------------------------//
    //--------------------------------------------------------------------------------------------//
    // Choose an image from Gallery
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    // Show simple message using Toast
    void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Saving to Database...
                    if (saveImageInDB(selectedImageUri)) {
                        //showMessage("Image Saved in Database...");
                        ImgView_photo.setImageURI(selectedImageUri);
                    }

                    // Reading from Database after 3 seconds just to show the message
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (loadImageFromDB()) {
                                //showMessage("Image Loaded from Database...");
                            }
                        }
                    }, 3000);
                }
            }
        }
    }
    // Save image in Database
    Boolean saveImageInDB(Uri selectedImageUri) {
        try {
            //initDB();
            InputStream iStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] inputData = Utils.getBytes(iStream);
            Log.d("byte save to DB",inputData.toString());
            dbHelper.addImageByte(inputData);
            dbHelper.close();
            return true;
        } catch (IOException ioe) {
            Log.e(TAG, "<saveImageInDB> Error : " + ioe.getLocalizedMessage());
            dbHelper.close();
            return false;
        }
    }
    // Load image from Database
    Boolean loadImageFromDB() {
        try {
            //initDB();
            byte[] bytes = dbHelper.retrieveImageFromDB();
            Log.d("byte load from DB",bytes.toString());
            dbHelper.close();
            // Show Image from DB in ImageView
            ImgView_photo.setImageBitmap(Utils.getImage(bytes));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "<loadImageFromDB> Error : " + e.getLocalizedMessage());
            dbHelper.close();
            return false;
        }
    }
    //Database : save the change to database
    public void save(View v) {
        String hadSetUp = "1";
        String strPhone = editTextPhone.getText().toString();
        String strName = editTextName.getText().toString();
        String strAddr = editTextAddress.getText().toString();
        //String strRoom = editTextRoom.getText().toString();
        String birthday;
        initDB();
        Cursor cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);

        int year = pickBirthday.getYear();
        int month = pickBirthday.getMonth()+1;
        int date = pickBirthday.getDayOfMonth();
        birthday =  year + "-" + month + "-" + date;
        if(isValidPhoneNum(strPhone)==true){
            dbHelper.editProfileData(hadSetUp,strName, strPhone ,strAddr, birthday);
            initDB();
            closeDB();
            FireBaseUpdateData(uuId, strName);
            alertSuccess();
        }
        //可以改 room 時
//        if(isValidPhoneNum(strPhone)==true && isValidRoomNum(strRoom)){
//            dbHelper.editProfileData(hadSetUp,strName, strPhone ,strAddr, birthday,strRoom);
//            closeDB();
//            FireBaseUpdateData(uuId, strName, strRoom);
//            alertSuccess();
//        }
    }
    private boolean isValidPhoneNum(String editTextPhone){
        if(editTextPhone.length()!=10 && editTextPhone.matches("\\d+")){
            showMessage("請輸入有效的電話號碼！");
            return false;
        }
        return true;
    }
//    private boolean isValidRoomNum(String editTextRoom){
//        if(editTextRoom.length()!=4){
//            showMessage("請輸入有效的群組號碼！");
//            return false;
//        }
//        return true;
//    }
//    private void cropImage() {
//        try{
//            cropIntent = new Intent("com.android.camera.action.CROP");
//            cropIntent.setDataAndType(uri,"image/*");
//
//            cropIntent.putExtra("crop","true");
//            cropIntent.putExtra("outputX",1000);
//            cropIntent.putExtra("outputY",1000);
//            cropIntent.putExtra("aspectX",1);
//            cropIntent.putExtra("aspectY",1);
//            cropIntent.putExtra("noFaceDetection", true);
//            cropIntent.putExtra("scaleUpIfNeeded",true);
//            cropIntent.putExtra("return-data",true);
//
//            startActivityForResult(cropIntent,REQUEST_CROP_IMAGE);
//        }
//        catch (ActivityNotFoundException ex){
//        }
//    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(isElder()) {
                startActivity(new Intent(ProfileAddActivity.this, HomeActivity.class));
                finish();
            }else{
                startActivity(new Intent(ProfileAddActivity.this, FamilyActivity.class));
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    //cancel edit and go back to profile page
    public void cancel(View v){
        if(isElder()) {
            startActivity(new Intent(ProfileAddActivity.this, HomeActivity.class));
            finish();
        }else{
            startActivity(new Intent(ProfileAddActivity.this, FamilyActivity.class));
            finish();
        }
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Firebase -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void FireBaseUpdateData(String uuId, String strName) {
        //dbRef1 = FirebaseDatabase.getInstance().getReference("groups").child(strRoom).child("members");
        dbRef2 = FirebaseDatabase.getInstance().getReference("members");

        try {
            dbRef2.child(uuId).child("mName").setValue(strName);
            //dbRef2.child(uuId).child("mGroup").setValue(strRoom);
         } catch (Exception e) {
             e.printStackTrace();
         }
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        uuId = cursor.getString(cursor.getColumnIndex("uuid"));
        editTextName.setText( cursor.getString(cursor.getColumnIndex("name")) );
        editTextName.setSelectAllOnFocus(true);
        editTextPhone.setText( cursor.getString(cursor.getColumnIndex("phone")) );
        editTextAddress.setText( cursor.getString(cursor.getColumnIndex("address")) );
        //editTextRoom.setText( cursor.getString(cursor.getColumnIndex("room")) );

        String birthday = cursor.getString(cursor.getColumnIndex("birthday"));
        String[] parts = birthday.split("-");
        int mYear = Integer.parseInt(parts[0]); //yyyy
        int mMonth = Integer.parseInt(parts[1])-1; // mm
        int mDate = Integer.parseInt(parts[2]); // dd
        pickBirthday.init(mYear, mMonth, mDate, null);
    }
    //Database : close database
    private void closeDB(){
        dbHelper.close();
    }
    public void alertSuccess() {
        Toast toast = Toast.makeText(this, "saved!", Toast.LENGTH_SHORT);
        toast.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(isElder()) {
                    startActivity(new Intent(ProfileAddActivity.this, HomeActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(ProfileAddActivity.this, FamilyActivity.class));
                    finish();
                }
            }
        }, 1000);
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
