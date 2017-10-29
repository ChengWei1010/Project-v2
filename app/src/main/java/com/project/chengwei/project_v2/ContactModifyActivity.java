package com.project.chengwei.project_v2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ContactModifyActivity extends AppCompatActivity {
    static final String KEY =  "com.<your_app_name>";
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    Toolbar myToolbar;
    EditText modify_name, modify_phone;
    TextView toolbar_title;
    ImageView modify_imageView;
    Button cameraBtn, chooseBtn, modifyBtn, deleteBtn;
    String uriString, uriString_crop;
    Intent cropIntent;
    Uri uri, uri_crop;
    ByteArrayOutputStream bytearrayoutputstream;
    FileOutputStream fileoutputstream;
    File file;
    Bitmap bitmap;
    String modifyName, modifyPhone;

    final int REQUEST_EXTERNAL_STORAGE = 999;
    final int REQUEST_IMAGE_CAPTURE = 99;
    final int REQUEST_CROP_IMAGE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_modify);

        findViews();
        setToolbar();

        //接收從PopUp傳過來的資料
        final int getId = getIntent().getIntExtra("ID",0);
        String getName = getIntent().getStringExtra("NAME");
        String getPhone = getIntent().getStringExtra("PHONE");
        String getImage = getIntent().getStringExtra("IMAGE");
        uriString = getImage;

        //先把還沒更改的資料全部放上去
        modify_name.setText(getName);
        modify_phone.setText(getPhone);
//        modify_imageView.setImageURI(Uri.parse(getImage));
        Glide.with(this)
                .load(getImage)
                .error(R.drawable.ic_family)//load失敗的Drawable
                .into(modify_imageView);

        //按拍攝照片的按鈕
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camIntent,REQUEST_IMAGE_CAPTURE);
            }
        });

        //按選擇照片的按鈕
        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE);
            }
        });

        //按確認的按鈕
        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //檢查輸入的值是否為空白
                modifyName = modify_name.getText().toString().trim();
                modifyPhone = modify_phone.getText().toString().trim();

                if(modifyName.equals("") || modifyPhone.equals("")){
                    Toast.makeText(ContactModifyActivity.this,"請填寫欄位",Toast.LENGTH_SHORT).show();
                }else{
                    if(uriString_crop != null){
                        try{
                            file.createNewFile();
                            fileoutputstream = new FileOutputStream(file);
                            fileoutputstream.write(bytearrayoutputstream.toByteArray());
                            fileoutputstream.close();
                            Toast.makeText(ContactModifyActivity.this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        uriString = uriString_crop;
                    }

                    //儲存姓名,電話,照片,Id進去資料庫
                    try{
                        ContactListActivity.sqLiteDBHelper.updateContactData(
                                modifyName,
                                modifyPhone,
                                uriString,
                                getId
                        );
                        Toast.makeText(getApplicationContext(), "Updated successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ContactModifyActivity.this, ContactListActivity.class));
                        finish();
                    }
                    catch (Exception e){
                        Log.e("Update error", e.getMessage());
                    }
                }
            }
        });

        //按刪除的按鈕
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDelete(getId);
            }
        });
    }

    private void showDialogDelete(final int idPerson){
        final AlertDialog.Builder dialogDelete = new AlertDialog.Builder(ContactModifyActivity.this);

        dialogDelete.setTitle("Warning!!");
        dialogDelete.setMessage("Are you sure you want to delete this?");
        dialogDelete.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    ContactListActivity.sqLiteDBHelper.deleteContactData(idPerson);
                    Toast.makeText(getApplicationContext(), "Delete successfully!!!",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(ContactModifyActivity.this , ContactListActivity.class);
                    startActivity(intent);
                } catch (Exception e){
                    Log.e("error", e.getMessage());
                }
            }
        });

        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDelete.show();
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //選擇相簿裡的照片
        if(requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK && data != null){
            uri = data.getData();
            cropImage();
            /*
            //抓照片的uri並顯示出來
            Uri uri = data.getData();
            uriString = uri.toString();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                modify_imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
        }
        //相機拍的照片
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null){
            uri = data.getData();
            cropImage();
            /*
            //抓照片的uri並顯示出來
            Uri uri = data.getData();
            uriString = uri.toString();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                modify_imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
        }
        //裁剪照片後顯示
        else if(requestCode == REQUEST_CROP_IMAGE && resultCode == RESULT_OK && data != null){
            bytearrayoutputstream = new ByteArrayOutputStream();

            Bundle bundle = data.getExtras();
            bitmap = bundle.getParcelable("data");
            modify_imageView.setImageBitmap(bitmap);

            //儲存剪裁後的照片到外部空間
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearrayoutputstream);

            file = new File(Environment.getExternalStorageDirectory(),
                    "crop_image"+String.valueOf(System.currentTimeMillis())+".jpg");

            uri_crop = Uri.fromFile(file);
            uriString_crop = uri_crop.toString();
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
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        myToolbar = findViewById(R.id.toolbar_home);
        modify_name = findViewById(R.id.name);
        modify_phone = findViewById(R.id.phone);
        modify_imageView = findViewById(R.id.imageView);
        cameraBtn = findViewById(R.id.cameraBtn);
        chooseBtn = findViewById(R.id.chooseBtn);
        modifyBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        toolbar_title = findViewById(R.id.toolbar_title);
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setToolbar(){
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setNavigationIcon(R.drawable.ic_back_left_white_50dp);
        toolbar_title.setText("編輯聯絡人");
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                startActivity(new Intent(ContactModifyActivity.this, ContactListActivity.class));
                finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences ----------------------------------------//
    //--------------------------------------------------------------------------------------------//

    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
    }
}


