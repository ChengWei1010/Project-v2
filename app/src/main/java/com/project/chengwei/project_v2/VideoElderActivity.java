package com.project.chengwei.project_v2;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.session.MediaController;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoElderActivity extends AppCompatActivity {
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    static final String KEY =  "com.<your_app_name>";

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private String groupNum;
    private Button showBtn;
    private Toolbar myToolbar;

    //這裡是宣告變數
    private VideoView vidView;
    private MediaController vidControl;
    private DownloadManager downloadManager;
    private Uri mUri;
//            "https://firebasestorage.googleapis.com/v0/b/elderlyproject-46505.appspot.com/o/videos%2Ftest.mp4?alt=media&token=973d7ee7-513a-49d4-9f64-c62233228c77";
    //新加的東西
    private FirebaseData firebaseData;
//    private List<Map<String, String>> items = new ArrayList<>();
    private GridView gridView;
    private VideoListAdapter adapter = null;
    private ArrayList<FirebaseData> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_elder);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //取得房號
        groupNum = getIntent().getExtras().get("groupNum").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(groupNum);

//        mStorage = FirebaseStorage.getInstance();
//        mStorageRef = mStorage.getReference();
//        mUri = mStorageRef.child("videos/test.mp4").getDownloadUrl().getResult();
        showBtn = findViewById(R.id.showBtn);
        gridView = findViewById(R.id.gridView);
        setShowBtn();
        findViews();
        setToolbar();
//        String id = UUID.randomUUID().toString();
//        Toast.makeText(WatchVideoActivity.this, id, Toast.LENGTH_SHORT).show();


//        SimpleAdapter adapter = new SimpleAdapter(VideoElderActivity.this, items, R.layout.grid_item,
//                new String[]{"date", "mId", "member", "storagePath"},
//                new int[]{R.id.textView_date, R.id.textView_mId, R.id.textView_member, R.id.textView_storagePath});
//        gridView.setNumColumns(3);
//        gridView.setAdapter(adapter);
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(VideoElderActivity.this, "你選擇了" + items.get(position).get("date") + items.get(position).get("storagePath"), Toast.LENGTH_SHORT).show();
//            }
//        });



    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
//        downloadBtn = findViewById(R.id.downloadBtn);
        myToolbar = findViewById(R.id.toolbar_home);
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            startActivity(new Intent(VideoElderActivity.this, HomeActivity.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setToolbar(){
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isElder()) {
                    startActivity(new Intent(VideoElderActivity.this, HomeActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(VideoElderActivity.this, FamilyActivity.class));
                    finish();
                }
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Download --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    
    private void setShowBtn() {
        showBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                list = new ArrayList<>();
                adapter = new VideoListAdapter(VideoElderActivity.this, R.layout.grid_item,list);
                gridView.setNumColumns(3);
                gridView.setAdapter(adapter);

//                //取得房間最後一筆資訊
//                mDatabaseRef.child("mVideo").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot child : dataSnapshot.getChildren()) {
//                            firebaseData = child.getValue(FirebaseData.class);
//                            String date = firebaseData.getDate();
//                            String mId = firebaseData.getmId();
//                            String member = firebaseData.getMember();
//                            String storagePath = firebaseData.getStoragePath();
//                            list.add(new FirebaseData(date, mId, member, storagePath));
//                        }
//                        adapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
                //取得房間裡所有資訊
                mDatabaseRef.child("mVideo").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get all of the children at this level.
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        // shake hands with each of them.'
                        for (DataSnapshot child : children) {
                            firebaseData = child.getValue(FirebaseData.class);
                            String date = firebaseData.getDate();
                            String mId = firebaseData.getmId();
                            String member = firebaseData.getMember();
                            String storagePath = firebaseData.getStoragePath();
                            list.add(new FirebaseData(date, mId, member, storagePath));
//                    Map<String, String> item = new HashMap<>();
//                    item.put("date", firebaseData.getDate());
//                    item.put("mId", firebaseData.getmId());
//                    item.put("member", firebaseData.getMember());
//                    item.put("storagePath", firebaseData.getStoragePath());
//                    items.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
    }

//    private void setDownloadBtn() {
//        downloadBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//                //這裡的uri就抓你存在聊天室裡msg的downloadUri
//                DownloadManager.Request request = new DownloadManager.Request(mUri);
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                downloadManager.enqueue(request);
//            }
//        });
//    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ FireBase sign In --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void signIn(){
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("hi", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(VideoElderActivity.this, "login success. "+user.getUid(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("hi", "signInAnonymously:failure", task.getException());
                            Toast.makeText(VideoElderActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }
//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
//    }
    private void updateUI(FirebaseUser user) {}
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
        //settings = getSharedPreferences(data,0);
        //return settings.getBoolean(elderlyMode,false);
    }
}

