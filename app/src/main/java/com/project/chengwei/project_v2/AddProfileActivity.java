package com.project.chengwei.project_v2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class AddProfileActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "HomeActivity";
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextAddress;
    private DatePicker pickBirthday;
    private ImageButton btn_manageDB;
    private ImageButton btn_editPhoto;
    private ImageView ImgView_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        findViews();
        setListeners();


        //Manage the Database by clicking a button
        btn_manageDB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent dbManager = new Intent(AddProfileActivity.this, AndroidDatabaseManager.class);
                startActivity(dbManager);
            }
        });
        //----------------------------------------------------------------------------------------//
        //--------------------------------------- Database ---------------------------------------//
        //----------------------------------------------------------------------------------------//
        initDB();        //Database : initial and insert profile data

        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        editTextName.setText( cursor.getString(cursor.getColumnIndex("name")) );
        editTextName.setSelectAllOnFocus(true);
        editTextPhone.setText( cursor.getString(cursor.getColumnIndex("phone")) );
        editTextAddress.setText( cursor.getString(cursor.getColumnIndex("address")) );

        String birthday = cursor.getString(cursor.getColumnIndex("birthday"));
        String[] parts = birthday.split("-");
        int mYear = Integer.parseInt(parts[0]); //yyyy
        int mMonth = Integer.parseInt(parts[1])-1; // mm
        int mDate = Integer.parseInt(parts[2]); // dd
        pickBirthday.init(mYear, mMonth, mDate, null);

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
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        pickBirthday = (DatePicker) findViewById(R.id.pickBirthday);

        btn_manageDB = (ImageButton) findViewById(R.id.btn_manageDB);
        btn_editPhoto = (ImageButton) findViewById(R.id.btn_editPhoto);
        ImgView_photo = (ImageView) findViewById(R.id.ImgView_photo);
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners(){
        btn_manageDB.setOnClickListener(ImageBtnListener);
        btn_editPhoto.setOnClickListener(ImageBtnListener);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ ImageBtnListener --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private android.view.View.OnClickListener ImageBtnListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_manageDB:
                    Intent dbManager = new Intent(AddProfileActivity.this,AndroidDatabaseManager.class);
                    startActivity(dbManager);
                    break;
                case R.id.btn_editPhoto:
                    openImageChooser();
                    break;
            }
        }
    };
    //--------------------------------------------------------------------------------------------//
    //----------------------------------------About Profile Photo---------------------------------//
    //--------------------------------------------------------------------------------------------//
    // Choose an image from Gallery
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    // Show simple message using SnackBar
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
                        showMessage("Image Saved in Database...");
                        ImgView_photo.setImageURI(selectedImageUri);
                    }

                    // Reading from Database after 3 seconds just to show the message
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (loadImageFromDB()) {
                                showMessage("Image Loaded from Database...");
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
            initDB();
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
            initDB();
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
    private boolean isValidPhoneNum(String editTextPhone){
        if(editTextPhone.length()!=10 && editTextPhone.matches("\\d+")){
            showMessage("InValid Phone Number");
            return false;
        }
        return true;
    }
    //Database : save the change to database
    public void save(View v) {
        String strPhone = editTextPhone.getText().toString();
        String strName = editTextName.getText().toString();
        String strAddr = editTextAddress.getText().toString();
        String birthday;
        initDB();
        Cursor cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);

        int year = pickBirthday.getYear();
        int month = pickBirthday.getMonth()+1;
        int date = pickBirthday.getDayOfMonth();
        birthday =  year + "-" + month + "-" + date;

        if(isValidPhoneNum(strPhone)==true){
            dbHelper.editProfileData(strName, strPhone ,strAddr, birthday);
            closeDB();
            alertSuccess();
        }
    }
    //cancel edit and go back to profile page
    public void cancel(View v){
        finish();
//        Intent ProfileIntent = new Intent(AddProfileActivity.this, ProfileActivity.class);
//        startActivity(ProfileIntent);
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
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
                Intent ProfileIntent = new Intent(AddProfileActivity.this, ProfileActivity.class);
                startActivity(ProfileIntent);
            }
        }, 2 * 1000);
    }
}
