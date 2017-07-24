package com.project.chengwei.project_v2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ContactModifyActivity extends AppCompatActivity {

    EditText modify_name, modify_phone;
    ImageView modify_imageView;
    Button chooseBtn, modifyBtn, backBtn, deleteBtn;

    String uriString;

    final int REQUEST_CODE_GALLERY = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_modify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        modify_name = (EditText) findViewById(R.id.name);
        modify_phone = (EditText) findViewById(R.id.phone);
        modify_imageView = (ImageView) findViewById(R.id.imageView);
        chooseBtn = (Button) findViewById(R.id.chooseBtn);
        modifyBtn = (Button) findViewById(R.id.enterBtn);
        backBtn = (Button) findViewById(R.id.backBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

        //接收從PopUp傳過來的資料
        final int getId = getIntent().getIntExtra("ID",0);
        String getName = getIntent().getStringExtra("NAME");
        String getPhone = getIntent().getStringExtra("PHONE");
        String getImage = getIntent().getStringExtra("IMAGE");
        uriString = getImage;

        //先把還沒更改的資料全部放上去
        modify_name.setText(getName);
        modify_phone.setText(getPhone);
        modify_imageView.setImageURI(Uri.parse(getImage));

        //按選擇照片的按鈕
        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(
                        ContactModifyActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });

        //按確認的按鈕
        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    ContactListActivity.sqLiteDBHelper.updateContactData(
                            modify_name.getText().toString().trim(),
                            modify_phone.getText().toString().trim(),
                            uriString,
                            getId
                    );
                    Toast.makeText(getApplicationContext(), "Updated successfully!", Toast.LENGTH_SHORT).show();
                    modify_name.setText("");
                    modify_phone.setText("");
                    modify_imageView.setImageResource(R.mipmap.ic_launcher);
                }
                catch (Exception e){
                    Log.e("Update error", e.getMessage());
                }
            }
        });

        //按返回的按鈕
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ContactModifyActivity.this , ContactListActivity.class);
                startActivity(intent);
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

    @Override
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            uriString = uri.toString();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                modify_imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}

