package com.project.chengwei.project_v2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactPopUpActivity extends Activity {

    ImageView imageView;
    TextView name, phone;
    Button callBtn, modifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.6));

        //接收PersonList傳過來的資料
        final int getId = getIntent().getIntExtra("ID",0);
        final String getName = getIntent().getStringExtra("NAME");
        final String getPhone = getIntent().getStringExtra("PHONE");
        final String getImage = getIntent().getStringExtra("IMAGE");

        //顯示照片
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(getImage));

        //顯示姓名
        name = (TextView) findViewById(R.id.name);
        name.setText(getName);

        //顯示電話號碼
        phone = (TextView) findViewById(R.id.phone);
        phone.setText(getPhone);

        //撥打電話
        callBtn = (Button) findViewById(R.id.callBtn);
        callBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent call = new Intent(Intent.ACTION_CALL, Uri.EMPTY.parse("tel:" + getPhone));
                startActivity(call);
            }
        });

        //跳到修改那頁
        modifyBtn = (Button) findViewById(R.id.enterBtn);
        modifyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ContactPopUpActivity.this,ContactModifyActivity.class);
                intent.putExtra("ID", getId);
                intent.putExtra("NAME", getName);
                intent.putExtra("PHONE", getPhone);
                intent.putExtra("IMAGE", getImage);
                startActivity(intent);
            }
        });
    }
}
