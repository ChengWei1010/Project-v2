package com.project.chengwei.project_v2;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    private Button btn_saveProfile;
    private ImageButton btn_manageDB;
    private ImageButton btn_camera, btn_choose;
    private ImageView ImgView_photo;
    private DatabaseReference dbRef1,dbRef2;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private String uuId,groupNum;
    Boolean hadCrop = false;

    final int REQUEST_EXTERNAL_STORAGE = 999;
    final int REQUEST_IMAGE_CAPTURE = 99;
    final int REQUEST_CROP_IMAGE = 9;
    Intent cropIntent;

    private Uri uri, uri_crop;
    private String uriString, strImage;
    private ByteArrayOutputStream bytearrayoutputstream;
    private FileOutputStream fileoutputstream;
    private File file;
    private Bitmap bitmap;

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
            btn_camera.setEnabled(true);
            btn_choose.setEnabled(true);
        }
        // Else ask for permission
        else {
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        // Load image from Database
        try {
            initDB();
            String imageString = dbHelper.retrieveImageFromDB();
            Log.d("String load from DB",imageString);
            dbHelper.close();
//            ImgView_photo.setImageURI(Uri.parse(imageString));
            Glide.with(this)
                    .load(imageString)
                    .error(R.drawable.ic_family)//load失敗的Drawable
                    .into(ImgView_photo);
            uriString = imageString;
        } catch (Exception e) {
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

        btn_saveProfile = findViewById(R.id.btn_saveProfile);
        btn_manageDB = findViewById(R.id.btn_manageDB);
        btn_camera = findViewById(R.id.cameraBtn);
        btn_choose = findViewById(R.id.chooseBtn);
        ImgView_photo = findViewById(R.id.ImgView_photo);

//        Drawable drawable;
//        Resources res = this.getResources();
//        if(isElder()){
//            drawable = res.getDrawable(R.drawable.ic_elder, getTheme());
//            ImgView_photo.setImageDrawable(drawable);
//        }
//        else{
//            drawable = res.getDrawable(R.drawable.ic_family, getTheme());
//            ImgView_photo.setImageDrawable(drawable);
//        }
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners(){
        btn_manageDB.setOnClickListener(ImageBtnListener);
        btn_camera.setOnClickListener(ImageBtnListener);
        btn_choose.setOnClickListener(ImageBtnListener);
        //Manage the Database by clicking a button
        btn_manageDB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent dbManager = new Intent(ProfileAddActivity.this, AndroidDatabaseManager.class);
                startActivity(dbManager);
            }
        });
        btn_saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //saveImageInLocal();
                saveDB();
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
                case R.id.cameraBtn:
                    openCamera();
                    break;
                case R.id.chooseBtn:
                    openImageChooser();
                    break;
            }
        }
    };
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- About Profile Photo ---------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Take photo from Camera
    void openCamera(){
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camIntent,REQUEST_IMAGE_CAPTURE);
    }
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
        //選擇相簿裡的照片
        if(requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null){
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
            ImgView_photo.setImageBitmap(bitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == SELECT_PICTURE) {
//                Uri selectedImageUri = data.getData();
//                if (null != selectedImageUri) {
//                    // Saving to Database...
//                    if (saveImageInDB(selectedImageUri)) {
//                        //showMessage("Image Saved in Database...");
//                        ImgView_photo.setImageURI(selectedImageUri);
//                    }
//
//                    // Reading from Database after 3 seconds just to show the message
////                    new Handler().postDelayed(new Runnable() {
////                        @Override
////                        public void run() {
////                            if (loadImageFromDB()) {
////                                //showMessage("Image Loaded from Database...");
////                            }
////                        }
////                    }, 3000);
//                }
//            }
//        }
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
            hadCrop = true;
        }
        catch (ActivityNotFoundException ex){
        }
    }
    private void saveImageInLocal(){
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearrayoutputstream);

        file = new File(Environment.getExternalStorageDirectory(),
                "crop_image"+String.valueOf(System.currentTimeMillis())+".jpg");

        uri_crop = Uri.fromFile(file);
        uriString = uri_crop.toString();

        try{
            file.createNewFile();
            fileoutputstream = new FileOutputStream(file);
            fileoutputstream.write(bytearrayoutputstream.toByteArray());
            fileoutputstream.close();
            //Toast.makeText(ProfileAddActivity.this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // Save image in Database
//    Boolean saveImageInDB(Uri selectedImageUri) {
//        try {
//            //initDB();
//            InputStream iStream = getContentResolver().openInputStream(selectedImageUri);
//            byte[] inputData = Utils.getBytes(iStream);
//            Log.d("byte save to DB",inputData.toString());
//            dbHelper.addImageByte(inputData);
//            dbHelper.close();
//            return true;
//        } catch (IOException ioe) {
//            Log.e(TAG, "<saveImageInDB> Error : " + ioe.getLocalizedMessage());
//            dbHelper.close();
//            return false;
//        }
//    }
    // Load image from Database
//    Boolean loadImageFromDB() {
//        try {
//            //initDB();
//            byte[] bytes = dbHelper.retrieveImageFromDB();
//            Log.d("byte load from DB",bytes.toString());
//            dbHelper.close();
//            // Show Image from DB in ImageView
//            ImgView_photo.setImageBitmap(Utils.getImage(bytes));
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "<loadImageFromDB> Error : " + e.getLocalizedMessage());
//            dbHelper.close();
//            return false;
//        }
//    }
    //Database : save the change to database
    public void saveDB() {
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
            if(hadCrop){
                saveImageInLocal();
//                dbHelper.saveEditImage(uriString);
            } else{
//                dbHelper.saveEditImage(uriString);
            }
            FireBaseUpdateImage(uri_crop);
            FireBaseUpdateData(uuId, strName, strPhone, groupNum, uriString);
            closeDB();
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
    private void FireBaseUpdateImage(Uri filePath){
        //Create file metadata including the content type
        //If you do not provide a contentType and Cloud Storage cannot infer a default from the file extension, Cloud Storage uses application/octet-stream.
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        if (filePath != null) {
            mStorage = FirebaseStorage.getInstance();
            mStorageRef = mStorage.getReference();
            StorageReference ref = mStorageRef.child("images").child(groupNum).child(uuId+".jpg");
            //Storage upload
            ref.putFile(filePath, metadata)
                    //上傳完畢
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //取得下載的url
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            strImage = downloadUri.toString();
                            FireBaseUpdateData(uuId, editTextName.getText().toString(), editTextPhone.getText().toString(), groupNum, strImage);
                            Log.d("Upload","Success");
                            //存照片到sqlite
                            dbHelper.saveEditImage(strImage);
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

    public void FireBaseUpdateData(String uId, String strName, String strPhone, String strGroup, String strImage) {
        dbRef1 = FirebaseDatabase.getInstance().getReference("groups").child(strGroup).child("members");
        dbRef2 = FirebaseDatabase.getInstance().getReference("members");

        try {
            dbRef1.child(uId).child("mName").setValue(strName);
            dbRef1.child(uId).child("mGroup").setValue(strGroup);
            dbRef1.child(uId).child("mPhone").setValue(strPhone);
            dbRef1.child(uId).child("mImage").setValue(strImage);

            dbRef2.child(uId).child("mName").setValue(strName);
            dbRef2.child(uId).child("mGroup").setValue(strGroup);
            dbRef2.child(uId).child("mPhone").setValue(strPhone);
            dbRef2.child(uId).child("mImage").setValue(strImage);
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
        uuId = cursor.getString(cursor.getColumnIndex("uid"));
        editTextName.setText( cursor.getString(cursor.getColumnIndex("name")) );
        editTextName.setSelectAllOnFocus(true);
        editTextPhone.setText( cursor.getString(cursor.getColumnIndex("phone")) );
        editTextAddress.setText( cursor.getString(cursor.getColumnIndex("address")) );
        groupNum = cursor.getString(cursor.getColumnIndex("room"));

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
