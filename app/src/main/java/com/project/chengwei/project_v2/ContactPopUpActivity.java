package com.project.chengwei.project_v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactPopUpActivity extends Activity {

        ImageView imageView;
        RoundImage roundedImage;
        TextView name, phone;
        Button callBtn, modifyBtn;
        Bitmap bitmap;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_contact_pop_up);

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            int width = dm.widthPixels;
            int height = dm.heightPixels;

            getWindow().setLayout((int)(width*.9),(int)(height*.75));

            //接收PersonList傳過來的資料
            final int getId = getIntent().getIntExtra("ID",0);
            final String getName = getIntent().getStringExtra("NAME");
            final String getPhone = getIntent().getStringExtra("PHONE");
            final String getImage = getIntent().getStringExtra("IMAGE");

            //顯示照片
            imageView = findViewById(R.id.imageView);
            imageView.setImageURI(Uri.parse(getImage));

            //以下4行是顯示圓形照片
        /*BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if(drawable == null){//如果存在手機的照片被刪除，顯示drawable的圖片
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("android.resource://com.example.angela.project_elderly/drawable/cat"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{//顯示照片
            bitmap = drawable.getBitmap();
        }*/

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            bitmap = drawable.getBitmap();
            roundedImage = new RoundImage(bitmap);
            imageView.setImageDrawable(roundedImage);

            //顯示姓名
            name = (TextView) findViewById(R.id.name);
            name.setText(getName);

            //顯示電話號碼
            phone = findViewById(R.id.phone);
            phone.setText(getPhone);

            //撥打電話
            callBtn = findViewById(R.id.callBtn);
            callBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent call = new Intent(Intent.ACTION_CALL, Uri.EMPTY.parse("tel:" + getPhone));
                    startActivity(call);
                }
            });

            //跳到修改那頁
            modifyBtn =  findViewById(R.id.editBtn);
            modifyBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(ContactPopUpActivity.this,ContactModifyActivity.class);
                    intent.putExtra("ID", getId);
                    intent.putExtra("NAME", getName);
                    intent.putExtra("PHONE", getPhone);
                    intent.putExtra("IMAGE", getImage);
                    startActivity(intent);
                    finish();
                }
            });
        }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- Set Window Size -------------------------------------//
    //--------------------------------------------------------------------------------------------//

}
