package com.project.chengwei.project_v2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ContactAddActivity extends AppCompatActivity {

    EditText edtName, edtPhone;
    Button btnCamera, btnChoose, btn_add;
    ImageView imageView;
    String uriString;
    Intent cropIntent;
    Uri uri, uri_crop;
    ByteArrayOutputStream bytearrayoutputstream;
    FileOutputStream fileoutputstream;
    File file;
    Bitmap bitmap;
    String addName, addPhone;
    private Toolbar myToolbar;
    private TextView toolbar_title;

    final int REQUEST_EXTERNAL_STORAGE = 999;
    final int REQUEST_IMAGE_CAPTURE = 99;
    final int REQUEST_CROP_IMAGE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);

        findViews();
        setToolbar();

        //sqLiteDBHelper = new SQLiteDBHelper(this, "PersonDB.sqlite", null, 1);
        //sqLiteDBHelper.queryData("CREATE TABLE IF NOT EXISTS PERSON(Id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, image TEXT)");

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

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //檢查輸入的值是否為空白
                addName = edtName.getText().toString().trim();
                addPhone = edtPhone.getText().toString().trim();

                if(addName.equals("") || addPhone.equals("") || bitmap == null){
                    if(addName.equals("") || addPhone.equals("")){
                        Toast.makeText(ContactAddActivity.this,"請填寫欄位",Toast.LENGTH_SHORT).show();
                    }
                    if(bitmap == null){
                        Toast.makeText(ContactAddActivity.this,"請選擇照片",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    //儲存剪裁後的照片到外部空間
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
                        Toast.makeText(ContactAddActivity.this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    //儲存名字,電話號碼,照片進去資料庫
                    try{
                        ContactListActivity.sqLiteDBHelper.insertContactData(
                                addName,
                                addPhone,
                                uriString
                        );
                        Toast.makeText(getApplicationContext(), "Added successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(ContactAddActivity.this , ContactListActivity.class);
                        startActivity(intent);
                        //edtName.setText("");
                        //edtPhone.setText("");
                        //imageView.setImageResource(R.mipmap.ic_launcher);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void findViews(){
        myToolbar = findViewById(R.id.toolbar_home);
        toolbar_title = findViewById(R.id.toolbar_title);
        edtName = findViewById(R.id.name);
        edtPhone = findViewById(R.id.phone);
        btnCamera = findViewById(R.id.cameraBtn);
        btnChoose = findViewById(R.id.chooseBtn);
        btn_add = findViewById(R.id.btn_add);
        imageView = findViewById(R.id.imageView);
    }

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
            imageView.setImageBitmap(bitmap);
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
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setToolbar(){
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar_title.setText("新增聯絡人");
        myToolbar.setNavigationIcon(R.drawable.ic_back_left_white_50dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactAddActivity.this, ContactListActivity.class));
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            startActivity(new Intent(ContactAddActivity.this, ContactListActivity.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------- bottom button ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void cancel(View v){
        finish();
    }
}
