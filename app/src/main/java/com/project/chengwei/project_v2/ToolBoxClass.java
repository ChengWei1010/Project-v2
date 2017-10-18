package com.project.chengwei.project_v2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by chengwei on 2017/10/18.
 */

public class ToolBoxClass extends Dialog implements android.view.View.OnClickListener {
    public Activity c;
    public Dialog d;
    public ImageButton btn_map, btn_magnifier;

    public ToolBoxClass(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alert_toolbox);
        btn_map = findViewById(R.id.btn_map);
        btn_magnifier = findViewById(R.id.btn_magnifier);
        btn_map.setOnClickListener(this);
        btn_magnifier.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_map:
                Intent intent = new Intent();
                intent.setClass(v.getContext(), HomeActivity.class);
                v.getContext().startActivity(intent);
                break;
            case R.id.btn_magnifier:
                Intent intent2 = new Intent();
                intent2.setClass(v.getContext(), HomeActivity.class);
                v.getContext().startActivity(intent2);
                break;
            default:
                break;
        }
        dismiss();
    }
}
