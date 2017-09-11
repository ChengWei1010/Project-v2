package com.project.chengwei.project_v2;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.MediaController;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private ImageButton toolbar_guide;
    private Toolbar myToolbar;
    private Date formattedDate;

    private Gallery gallery;
    //private ImageSwitcher imageSwitcher;
    private SimpleAdapter simpleAdapter;
    private VideoView videoView;
    private MediaController vidControl;
    private Uri uri;
    private Button downloadBtn;

    private DownloadManager downloadManager;

    private FirebaseData firebaseData;
//    private GridView gridView;
//    private VideoListAdapter adapter = null;
//    private ArrayList<FirebaseData> list;

    private ArrayList<String> memberList;
    private ArrayList<String> storagePathList;
    private String[] memberText, pathText;
    private List<Map<String, Object>> items;

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
        findViews();
        showVideo();
        setToolbar();

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
        //gridView = findViewById(R.id.gridView);
        //imageSwitcher = (ImageSwitcher)findViewById(R.id.imageSwitcher);
        gallery = (Gallery)findViewById(R.id.gallery);
        videoView = (VideoView) findViewById(R.id.videoView);
        downloadBtn = (Button) findViewById(R.id.downloadBtn);
        myToolbar = findViewById(R.id.toolbar_with_guide);
        toolbar_guide = findViewById(R.id.toolbar_btn_guide);
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
        toolbar_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGuide();
            }
        });

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
    
    private void showVideo() {
//        list = new ArrayList<>();
        items = new ArrayList<>();
        memberList = new ArrayList<>();
        storagePathList = new ArrayList<>();
        gallery.setUnselectedAlpha((float) 0.5);
//        adapter = new VideoListAdapter(VideoElderActivity.this, R.layout.grid_item,list);
//        gridView.setNumColumns(3);
//        gridView.setAdapter(adapter);

//        //取得房間最後一筆資訊
//        mDatabaseRef.child("mVideo").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    firebaseData = child.getValue(FirebaseData.class);
//                    String date = firebaseData.getDate();
//                    String mId = firebaseData.getmId();
//                    String member = firebaseData.getMember();
//                    String storagePath = firebaseData.getStoragePath();
//                    list.add(new FirebaseData(date, mId, member, storagePath));
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
        //取得當天日期
        Date currentDate = new Date();
        DateFormat dateFormat= new SimpleDateFormat("yyyy年MM月dd日");
        try{
            formattedDate = dateFormat.parse(dateFormat.format(currentDate));
            //Log.d("today",formattedDate.toString());
        }catch(ParseException parseEx){
            parseEx.printStackTrace();
        }

        //取得房間裡所有member當日傳的資料
        mDatabaseRef.child("mVideo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.'
                for (DataSnapshot child : children) {
                    firebaseData = child.getValue(FirebaseData.class);

                    //取得firebase存的Date
                    String date = firebaseData.getDate();
                    String subDate = date.substring(0,11);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                    Date firebaseDate = null;
                    try {
                        firebaseDate = sdf.parse(subDate);
                        //Log.d("firebaseDay",firebaseDate.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //比較firebase存的日期跟今天的日期有沒有一樣
                    if(formattedDate.equals(firebaseDate)){
//                        Log.d("today"," and firebase date is equal");
                        String member = firebaseData.getMember();
                        String storagePath = firebaseData.getStoragePath();
                        memberList.add(member);
                        storagePathList.add(storagePath);
//                        list.add(new FirebaseData(date, mId, member, storagePath));
                    }

//                    Map<String, String> item = new HashMap<>();
//                    item.put("date", firebaseData.getDate());
//                    item.put("mId", firebaseData.getmId());
//                    item.put("member", firebaseData.getMember());
//                    item.put("storagePath", firebaseData.getStoragePath());
//                    items.add(item);
                }

                if(memberList.isEmpty()){
                    Toast.makeText(VideoElderActivity.this,"今天還沒有影片喔!",Toast.LENGTH_SHORT).show();
                }else{
                    memberText = memberList.toArray(new String[memberList.size()]);
                    pathText = storagePathList.toArray(new String[storagePathList.size()]);

                    for (int i = 0; i < memberText.length; i++) {
                        Log.d("member",memberText[i]);
                        Map<String, Object> item = new HashMap<String, Object>();
                        item.put("image", R.drawable.ic_elder);
                        item.put("text", memberText[i]);
                        items.add(item);
                    }
                    simpleAdapter = new SimpleAdapter(VideoElderActivity.this,
                            items, R.layout.simple_adapter, new String[]{"image", "text"},
                            new int[]{R.id.image, R.id.text});

                    gallery.setAdapter(simpleAdapter);

                    gallery.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> parent, View arg1, final int position, long id) {

                            videoView.setVisibility(View.VISIBLE);
                            downloadBtn.setVisibility(View.VISIBLE);

                            vidControl = new android.widget.MediaController(VideoElderActivity.this);
                            vidControl.setAnchorView(videoView);
                            videoView.setMediaController(vidControl);
                            uri = Uri.parse(pathText[position]);
                            videoView.setVideoURI(uri);
                            videoView.start();

                            downloadBtn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
                                    Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                                    String date = formatter.format(curDate);

                                    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir("/videoDownload", memberText[position] + "/" + date + ".mp4");
                                    request.setTitle(memberText[position] + ":" + date);
                                    downloadManager.enqueue(request);
                                }
                            });
                        }
                    });

//                adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
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
    //-------------------------------------- help and Guide --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void openGuide(){
        Toast.makeText(VideoElderActivity.this, "guide !", Toast.LENGTH_SHORT).show();
    }
    private void closeGuide(){

    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
        //settings = getSharedPreferences(data,0);
        //return settings.getBoolean(elderlyMode,false);
    }
}

