package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class VideoElderActivity extends AppCompatActivity {
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    static final String KEY =  "com.<your_app_name>";

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;


    private Button btn_list;

    private String myGroup;
    private ImageButton showBtn,toolbar_guide;
    private Toolbar myToolbar;
    private Date formattedDate;
    private TextView toolbar_title;

    private Gallery gallery;
    private GalleryAdapter galleryAdapter = null;

    private FirebaseData firebaseData;
    private MemberData memberData;

    private ArrayList<String> memberList, groupMemberList, storagePathList, imagePathList, mIdVideoList;
    private ArrayList<MemberData> memberDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_elder);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //取得房號
        myGroup = getIntent().getExtras().get("myGroup").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup);

        findViews();
        showVideo();
        setToolbar();

        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listMember();
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        gallery = (Gallery)findViewById(R.id.gallery);
        btn_list = (Button) findViewById(R.id.btnList);
        myToolbar = findViewById(R.id.toolbar_with_guide);
        toolbar_title = findViewById(R.id.toolbar_title);
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
        toolbar_title.setText("觀看今日影片");
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
        memberList = new ArrayList<>();
        storagePathList = new ArrayList<>();
        imagePathList = new ArrayList<>();
        memberDataList = new ArrayList<>();
        mIdVideoList = new ArrayList<>();

        gallery.setUnselectedAlpha((float) 0.5);
        gallery.setSpacing(10);

        //取得當天日期
        Date currentDate = new Date();
        DateFormat dateFormat= new SimpleDateFormat("yyyy年MM月dd日");
        try{
            formattedDate = dateFormat.parse(dateFormat.format(currentDate));
            //Log.d("today",formattedDate.toString());
        }catch(ParseException parseEx){
            parseEx.printStackTrace();
        }

        //抓group裡面所有member
        mDatabaseRef.child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.'
                for (DataSnapshot child : children) {
                    //抓出成員存到arrayList
                    memberData = child.getValue(MemberData.class);
                    memberDataList.add(memberData);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

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
                        //Log.d("today"," and firebase date is equal");
                        String member = firebaseData.getMember();
                        String mId = firebaseData.getmId();
                        String storagePath = firebaseData.getStoragePath();
                        memberList.add(member);
                        //抓mVideo裡面的mId
                        mIdVideoList.add(mId);
                        storagePathList.add(storagePath);
                    }
                }

                if(memberList.isEmpty()){
                    Toast.makeText(VideoElderActivity.this,"今天還沒有影片喔!",Toast.LENGTH_SHORT).show();
                }else{
                    for(int i=0; i<mIdVideoList.size(); i++){
                        String mIdVideo = mIdVideoList.get(i);

                        for(int j=0; j<memberDataList.size(); j++){
                            String mIdMember = memberDataList.get(j).getmId();
                            if(mIdVideo.equals(mIdMember)){
                                //Log.d("SameMid",mIdVideo);
                                imagePathList.add(memberDataList.get(j).getmImage());
                            }
                        }
                    }

                    galleryAdapter = new GalleryAdapter(VideoElderActivity.this, R.layout.gallery_item,memberList,storagePathList,imagePathList);
                    gallery.setAdapter(galleryAdapter);

                    gallery.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> parent, View arg1, final int position, long id) {
                            Toast.makeText(VideoElderActivity.this, memberList.get(position) ,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------- List members in the same room-----------------------------//
    //--------------------------------------------------------------------------------------------//
    public void listMember(){
        groupMemberList = new ArrayList<>();
        groupMemberList.clear();
        mDatabaseRef.child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.'
                for (DataSnapshot child : children) {
                    //抓出成員存到arrayList
                    memberData = child.getValue(MemberData.class);
                    groupMemberList.add(memberData.getmName());
                }
                //顯示arrayList的所有成員
                new AlertDialog.Builder(VideoElderActivity.this)
                        .setTitle(myGroup + "裡的成員")
                        .setItems(groupMemberList.toArray(new String[groupMemberList.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = groupMemberList.get(which);
                                Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    
    private void setShowBtn() {}


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