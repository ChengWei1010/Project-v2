package com.project.chengwei.project_v2;

import android.content.Context;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.list;

public class FamilyActivity extends AppCompatActivity {
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    static final String KEY =  "com.<your_app_name>";
    private Toolbar myToolbar;
    private FrameLayout left_drawer;
    private ImageButton btn_video;
    private Button btn_time, btn_showMember,btn_watch_video;
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private String mGroup, mName, myId;
    final int RequestCameraCode = 1;
    final int RequestCallCode = 2;
    final int RequestExternalStorageCode = 3;
    final int RequestLocationCode = 4;
    final int RequestPermissionCode = 999;
    private final int REQUEST_PERMISSION = 10;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef, sendTimeRef;
    private int TimeDialogID = 0;
    private int hour, minute;
    private MemberData memberData;
    private ArrayList<String> memberList;

    private DrawerLayout drawer;
    private TextView textViewName, textViewPhone, textViewAddress, textViewBirthday, textViewRoom, toolbar_title;
    private ImageView profileImg;
    private ImageButton btn_editProfile,toolbar_guide;
    RoundImage roundedImage;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_family);
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
            signIn();
        }
        findViews();
        initDB();
        setToolbar();
        setListeners();
        closeDB();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews() {
        drawer = findViewById(R.id.drawer_layout);
        myToolbar = findViewById(R.id.toolbar_with_guide);
        toolbar_guide = findViewById(R.id.toolbar_btn_guide);
        btn_video = findViewById(R.id.btn_video);
        left_drawer = findViewById(R.id.left_drawer);

        btn_watch_video = findViewById(R.id.btn_watch_video);
        btn_time = findViewById(R.id.timeBtn);
        btn_showMember = findViewById(R.id.showMemberBtn);
        toolbar_title = findViewById(R.id.toolbar_title);

        //profile drawer
        textViewName = findViewById(R.id.textViewName);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewBirthday = findViewById(R.id.textViewBirthday);
        textViewRoom = findViewById(R.id.textViewRoom);
        profileImg = findViewById(R.id.profileImg);
        btn_editProfile = findViewById(R.id.btn_editProfile);
        Drawable drawable;
        Resources res = this.getResources();
        if(isElder()){
            drawable = res.getDrawable(R.drawable.ic_elder, getTheme());
            profileImg.setImageDrawable(drawable);
        }
        else{
            drawable = res.getDrawable(R.drawable.ic_family, getTheme());
            profileImg.setImageDrawable(drawable);
        }
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB() {
        dbHelper = new SQLiteDBHelper(getApplicationContext());
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        mName = cursor.getString(cursor.getColumnIndex("name"));
        mGroup = cursor.getString(cursor.getColumnIndex("room"));
        myId = cursor.getString(cursor.getColumnIndex("uid"));

        textViewName.setText( cursor.getString(cursor.getColumnIndex("name")) );
        textViewPhone.setText( cursor.getString(cursor.getColumnIndex("phone")) );
        textViewAddress.setText( cursor.getString(cursor.getColumnIndex("address")) );
        textViewBirthday.setText( cursor.getString(cursor.getColumnIndex("birthday")) );
        textViewRoom.setText( cursor.getString(cursor.getColumnIndex("room")) );

        String imageString = dbHelper.retrieveImageFromDB();
        profileImg.setImageURI(Uri.parse(imageString));

        BitmapDrawable drawable = (BitmapDrawable) profileImg.getDrawable();
        bitmap = drawable.getBitmap();
        roundedImage = new RoundImage(bitmap);
        profileImg.setImageDrawable(roundedImage);
        myId = cursor.getString(cursor.getColumnIndex("uid"));
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(mGroup);
    }

    //Database : close database
    private void closeDB() {
        dbHelper.close();
    }

    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners() {
        btn_video.setOnClickListener(ImageBtnListener);
        btn_editProfile.setOnClickListener(ImageBtnListener);
        btn_watch_video.setOnClickListener(BtnListener);
        btn_time.setOnClickListener(BtnListener);
        btn_showMember.setOnClickListener(BtnListener);
    }

    //--------------------------------------------------------------------------------------------//
    //------------------------------------ ImageBtnListener --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private android.view.View.OnClickListener ImageBtnListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_video:
                    //startActivity(new Intent(HomeActivity.this, WatchVideoActivity.class));
                    Intent intent = new Intent(getApplicationContext(), VideoFamilyActivity.class);
                    intent.putExtra("mName",mName);
                    intent.putExtra("groupNum",mGroup);
                    intent.putExtra("mId",myId);
                    intent.putExtra("hour", hour);
                    intent.putExtra("minute", minute);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.btn_editProfile:
                    //Toast.makeText(FamilyActivity.this, "edit !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FamilyActivity.this, ProfileAddActivity.class));
                    finish();
                    break;
            }
        }
    };

    //------------------------------------ BtnListener --------------------------------------//
    private Button.OnClickListener BtnListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.timeBtn:
                    setSendTime();
                    break;
                case R.id.showMemberBtn:
                    listMember();
                    break;
                case R.id.btn_watch_video:
                    Intent intent = new Intent(getApplicationContext(),VideoElderActivity.class);
                    intent.putExtra("myGroup",mGroup);
                    intent.putExtra("myId", myId);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    //--------------------------------------------------------------------------------------------//
    //------------------------------- Set video send time-----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setSendTime(){
        //抓現在的時間為預設時間
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        // Create a new instance of TimePickerDialog and return it
        new TimePickerDialog(FamilyActivity.this, new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
                hour = hourOfDay;
                minute = minutes;
                Toast.makeText(FamilyActivity.this, "Send Time is: "+ hour + ":" + minute, Toast.LENGTH_SHORT).show();
            }
        }, hour, minute, false).show();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------- List members in the same room-----------------------------//
    //--------------------------------------------------------------------------------------------//
    public void listMember(){
        memberList = new ArrayList<>();
        memberList.clear();
        mDatabaseRef.child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.'
                for (DataSnapshot child : children) {
                    //抓出成員存到arrayList
                    memberData = child.getValue(MemberData.class);
                    memberList.add(memberData.getmName());
                }
                //顯示arrayList的所有成員
                new AlertDialog.Builder(FamilyActivity.this)
                        .setTitle(mGroup + "裡的成員")
                        .setItems(memberList.toArray(new String[memberList.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = memberList.get(which);
                                Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------- Version and Permission ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void checkPermission() {
        int cameraPermission = ActivityCompat.checkSelfPermission(FamilyActivity.this, android.Manifest.permission.CAMERA);
        int readPermission = ActivityCompat.checkSelfPermission(FamilyActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(FamilyActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int callPermission = ActivityCompat.checkSelfPermission(FamilyActivity.this, android.Manifest.permission.CALL_PHONE);
        int locationPermission = ActivityCompat.checkSelfPermission(FamilyActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED ||
                callPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED ||
                locationPermission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            RequestRuntimePermission();
        }
    }

    private void RequestRuntimePermission() {
        //拒絕相機
        if (ActivityCompat.shouldShowRequestPermissionRationale(FamilyActivity.this, android.Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(FamilyActivity.this)
                    .setMessage("此應用程式需要CAMERA功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(FamilyActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    RequestCameraCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(FamilyActivity.this, "Camera Permission Canceled", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕電話
        else if (ActivityCompat.shouldShowRequestPermissionRationale(FamilyActivity.this,
                android.Manifest.permission.CALL_PHONE)) {
            new AlertDialog.Builder(FamilyActivity.this)
                    .setMessage("此應用程式需要CALL_PHONE功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(FamilyActivity.this,
                                    new String[]{android.Manifest.permission.CALL_PHONE},
                                    RequestCallCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(FamilyActivity.this, "Call Permission Canceled", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕讀取及寫入
        else if (ActivityCompat.shouldShowRequestPermissionRationale(FamilyActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(FamilyActivity.this)
                    .setMessage("此應用程式需要READ及WRITE_EXTERNAL_STORAGE功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(FamilyActivity.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    RequestExternalStorageCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(FamilyActivity.this, "External Storage Permission Canceled", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //拒絕位置
        else if (ActivityCompat.shouldShowRequestPermissionRationale(FamilyActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(FamilyActivity.this)
                    .setMessage("此應用程式需要ACCESS_FINE_LOCATION功能，請接受權限要求!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(FamilyActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    RequestCallCode);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(FamilyActivity.this, "Location Permission Canceled", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
        //接受
        else {
            ActivityCompat.requestPermissions(FamilyActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.CALL_PHONE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    RequestPermissionCode);
        }
    }

    //跳出權限要求時，按允許或拒絕
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(FamilyActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FamilyActivity.this, "Permission Canceled", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestCameraCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(FamilyActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FamilyActivity.this, "Camera Permission Canceled", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestCallCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(FamilyActivity.this, "Call Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FamilyActivity.this, "Call Permission Canceled", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestExternalStorageCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(FamilyActivity.this, "External Storage Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FamilyActivity.this, "External Storage Permission Canceled", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case RequestLocationCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(FamilyActivity.this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FamilyActivity.this, "Location Permission Canceled", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    //------------------------------------------- FireBase  --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void signIn() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("hi", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //Toast.makeText(FamilyActivity.this, "login success. " + user.getUid(), Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("hi", "signInAnonymously:failure", task.getException());
                            Toast.makeText(FamilyActivity.this, "請檢查網路連線",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setToolbar() {
        toolbar_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FamilyActivity.this, "guide !", Toast.LENGTH_SHORT).show();
            }
        });
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar_title.setText("您好，" + mName);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setNavigationIcon(R.drawable.ic_person_white);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
    }
}
