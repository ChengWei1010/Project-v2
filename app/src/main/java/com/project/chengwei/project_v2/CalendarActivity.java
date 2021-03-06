package com.project.chengwei.project_v2;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CalendarActivity extends ListActivity {
    private TextView toolbar_title, text_month;
    private Toolbar myToolbar;
    private ImageButton toolbar_btn_guide,toolbar_btn_add;
    private String myGroup, myId;
    private DatabaseReference mDatabaseRef;
    private ListView listView;
    private CalendarAdapter calendarAdapter;
    private CalendarDetails calendarDetails = new CalendarDetails();
    private ArrayList<String> calendarTitleList = new ArrayList<>();
    private ArrayList<String> calendarContentList = new ArrayList<>();
    private ArrayList<String> calendarDateList = new ArrayList<>();
    private ArrayList<String> calendarTimeList = new ArrayList<>();
    private Button btn_previous,btn_next;
    int monthId=0;
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //取得房號
        myId = getIntent().getExtras().get("myId").toString();
        myGroup = getIntent().getExtras().get("myGroup").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup).child("calendar");

        findViews();
        setListeners();
        setToolbar();
        DateFormat df = new SimpleDateFormat("yyyy" + "/" + "MM" + "/" + "dd");

        //設定ListView未取得內容時顯示的view, empty建構在list.xml中。
        //getListView().setEmptyView(progressbar);
        searchFunction(monthId);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        int i = position;
        String itemValue = (String) l.getItemAtPosition(position);
        ImageButton notification = findViewById(R.id.Calendar_notification);
        CalendarClickClass ccc = new CalendarClickClass(CalendarActivity.this, calendarTitleList.get(i), calendarContentList.get(i), calendarDateList.get(i), calendarTimeList.get(i));
        ccc.show();
    }
    private void searchFunction(final int count) {
        clearLists();
        initAdapter();

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
                int comparedMon = Integer.parseInt(formattedDateCutMon)+count;
                int comparedYear = Integer.parseInt(formattedDateCutYear);

                if (comparedMon > 12) {
                    comparedMon = comparedMon % 12;
                    comparedYear = comparedYear + 1;
                }
                for (DataSnapshot child : children) { // shake hands with each of them.
                    calendarDetails = child.getValue(CalendarDetails.class);
                    int yearINT = Integer.parseInt(calendarDetails.getDate().substring(0, 4));
                    int monINT = Integer.parseInt(calendarDetails.getDate().substring(5, 7));

                    if (yearINT == comparedYear) {
                        Log.d("ComparedYear: ", String.valueOf(comparedYear));
                        if (monINT == comparedMon) {
                            Log.d("ComparedMon: ", String.valueOf(comparedMon));
                            calendarTitleList.add(calendarDetails.getTitle());
                            calendarContentList.add(calendarDetails.getContent());
                            calendarTimeList.add(calendarDetails.getTime());
                            calendarDateList.add(calendarDetails.getDate().substring(8, 10));
                        }
                    }
                }
                for (int i = 0; i < calendarTitleList.size(); i++) {
                    Log.d("lily", "Calendar Title= " + calendarTitleList.get(i));
                    //Toast.makeText(MainActivity.this, "Calendar Title= " +calendarTitleListData.get(i), Toast.LENGTH_SHORT).show();
                }
                progressbar.setVisibility(View.INVISIBLE);
                if(calendarTitleList.isEmpty()){
                    Toast.makeText(CalendarActivity.this,"本月無行程!",Toast.LENGTH_SHORT).show();
                }else{
                    progressbar.setVisibility(View.INVISIBLE);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            calendarAdapter.notifyDataSetChanged();
                        }
                    });
                    //setListAdapter(new CalendarAdapter(CalendarActivity.this, calendarTitleList));
                }
                text_month.setText(comparedYear+"/"+comparedMon+"月");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void findViews(){
        toolbar_title = findViewById(R.id.toolbar_title);
        myToolbar = findViewById(R.id.toolbar_with_add);
        toolbar_btn_guide = findViewById(R.id.toolbar_btn_guide);
        toolbar_btn_add = findViewById(R.id.toolbar_btn_add);
        progressbar = findViewById(R.id.progressbar);
        text_month = findViewById(R.id.text_month);
        btn_previous = findViewById(R.id.btn_previous);
        btn_next = findViewById(R.id.btn_next);
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners(){
        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthId--;
                searchFunction(monthId);
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthId++;
                searchFunction(monthId);
            }
        });
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

    private void initAdapter(){
        listView = findViewById(android.R.id.list);
        setListAdapter(calendarAdapter = new CalendarAdapter(CalendarActivity.this, calendarDateList, calendarTitleList));
    }
    private void clearLists(){
        calendarDetails = new CalendarDetails();
        calendarTitleList.clear();
        calendarContentList.clear();
        calendarTimeList.clear();
        calendarDateList.clear();
    }

}
