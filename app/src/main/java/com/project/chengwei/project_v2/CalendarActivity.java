package com.project.chengwei.project_v2;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class CalendarActivity extends ListActivity {
    private TextView content,toolbar_title;
    private Toolbar myToolbar;
    private ImageButton toolbar_btn_guide,toolbar_btn_add;
    private String myGroup, myId;
    private DatabaseReference mDatabaseRef;
    private ListView listView;
    CalendarDetails calendarDetails = new CalendarDetails();
    private ArrayList<String> calendarTitleListData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //取得房號
        myId = getIntent().getExtras().get("myId").toString();
        myGroup = getIntent().getExtras().get("myGroup").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup).child("calendar");

        listView = findViewById(android.R.id.list);
        findViews();
        setToolbar();
        DateFormat df = new SimpleDateFormat("yyyy" + "/" + "MM" + "/" + "dd");

        searchFunction();

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
    private void searchFunction() {
        DateFormat df = new SimpleDateFormat("yyyy" + "/" + "MM" + "/" + "dd");
        final String formattedDate = df.format(new java.util.Date());
        final String formattedDateCutYear = formattedDate.substring(0, 4);
        final String formattedDateCutMon = formattedDate.substring(5, 7);
        //Log.d("Date", formattedDate);
        Log.d("DateCutYear", formattedDateCutYear);
        Log.d("DateCutMon", formattedDateCutMon);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                int comparedMon = Integer.parseInt(formattedDateCutMon);
                int comparedYear = Integer.parseInt(formattedDateCutYear);


                calendarTitleListData.clear();
                for (DataSnapshot child : children) { // shake hands with each of them.
                    calendarDetails = child.getValue(CalendarDetails.class);
                    int yearINT = Integer.parseInt(calendarDetails.getDate().substring(0, 4));
                    int monINT = Integer.parseInt(calendarDetails.getDate().substring(5, 7));

                    if (yearINT == comparedYear) {
                        Log.d("ComparedYear: ", String.valueOf(comparedYear));
                        if (monINT == comparedMon) {
                            Log.d("ComparedMon: ", String.valueOf(comparedMon));
                            calendarTitleListData.add(calendarDetails.getTitle());
                        }
                    }
                }
                for (int i = 0; i < calendarTitleListData.size(); i++) {
                    Log.d("Jafu", "Calendar Title= " + calendarTitleListData.get(i));
                    //Toast.makeText(MainActivity.this, "Calendar Title= " +calendarTitleListData.get(i), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
