package com.project.chengwei.project_v2;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class CalendarActivity extends ListActivity {
    private TextView content,toolbar_title;
    private Toolbar myToolbar;
    private ImageButton toolbar_btn_guide,toolbar_btn_add;
    private String myGroup, myId;
    private DatabaseReference mDatabaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //取得房號
        myId = getIntent().getExtras().get("myId").toString();
        myGroup = getIntent().getExtras().get("myGroup").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup);

        findViews();
        setToolbar();

        //listView = (ListView) findViewById(R.id.list);
        String[] values = new String[] {
                "阿公生日", "姿韶生日", "家庭聚餐",
                "出遊日", "爸爸看牙醫", "家福森七七", };

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values);

        // Assign adapter to List
        setListAdapter(adapter);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);

        // ListView Clicked item index
        int itemPosition = position;

        // ListView Clicked item value
        String itemValue = (String) l.getItemAtPosition(position);

        content.setText("Click : \n  Position :"+itemPosition+"  \n  ListItem : " +itemValue);

    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void findViews(){
        content = findViewById(R.id.output);
        toolbar_title = findViewById(R.id.toolbar_title);
        myToolbar = findViewById(R.id.toolbar_with_add);
        toolbar_btn_guide = findViewById(R.id.toolbar_btn_guide);
        toolbar_btn_add = findViewById(R.id.toolbar_btn_add);
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            startActivity(new Intent(CalendarActivity.this, HomeActivity.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setToolbar(){
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);
        toolbar_title.setText("家庭日曆");

        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CalendarActivity.this, HomeActivity.class));
                finish();
            }
        });

        toolbar_btn_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CalendarActivity.this, GuidePageViewer.class));
            }
        });
        toolbar_btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CalendarAddActivity.class);
                intent.putExtra("myGroup",myGroup);
                intent.putExtra("myId", myId);
                startActivity(intent);
                finish();
            }
        });
    }
}
