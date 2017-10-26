package com.project.chengwei.project_v2;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class CalendarIntentService extends IntentService {

    final FirebaseDatabase database = FirebaseDatabase.getInstance(); // Get a reference to our posts
    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Calendar"); // From groups in Database
    public String groupNum = "7309";
    CalendarDetails calendarDetails;
    private ArrayList<String> calendarNumListData = new ArrayList<>();

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO Auto-generated method stub
        Log.i(TAG, "Intent Service started");

        mDatabaseRef.child("calendar"+groupNum).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                calendarNumListData.clear();
                for (DataSnapshot child : children) { // shake hands with each of them.
                    calendarDetails = child.getValue(CalendarDetails.class);
                    calendarNumListData.add(calendarDetails.geteventId()); //抓出並存 calendarNum 到 arrayList
                }
                for (int i=0; i<calendarNumListData.size(); i++) {
                    Log.i(TAG, "Calendar Id= " +calendarNumListData.get(i));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
    }

    public CalendarIntentService() {
        super("MyIntentService");
    }
}
