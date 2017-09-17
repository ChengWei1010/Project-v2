package com.project.chengwei.project_v2;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class NavigationPopUpActivity extends AppCompatActivity {
    private Button btn_ok,btn_cancel;
    private EditText editTextAddr;
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_pop_up);

        findViews();
        setListeners();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.9),(int)(height*.75));


    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
    }
    public void save() {
        String address = editTextAddr.getText().toString();
        initDB();
        dbHelper.editAddrData(address);
        dbHelper.close();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- Valid Address ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private boolean isValidAddress(String address){
        if((address.contains("路")||address.contains("街")) && address.contains("號")){
            return true;
        }else {
            return false;
        }
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel = findViewById(R.id.btn_cancel);
        editTextAddr = findViewById(R.id.editTextAddr);
        editTextAddr.setSelectAllOnFocus(true);
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners() {
        btn_ok.setOnClickListener(ImageBtnListener);
        btn_cancel.setOnClickListener(ImageBtnListener);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ ImageBtnListener --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private android.view.View.OnClickListener ImageBtnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_ok:
                    //Toast.makeText(HomeActivity.this, "phone !", Toast.LENGTH_SHORT).show();
                    String address = editTextAddr.getText().toString();
                    if(isValidAddress(address)){
                        save();
                        startActivity(new Intent(NavigationPopUpActivity.this, NavigationActivity.class));
                        finish();
                    }else {
                        Toast.makeText(NavigationPopUpActivity.this, "不正確 !", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_cancel:
                    startActivity(new Intent(NavigationPopUpActivity.this, HomeActivity.class));
                    break;

            }
        }};
}

