package com.project.chengwei.project_v2;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    static final String KEY =  "com.<your_app_name>";
    static final String GROUP_NUM =  "0000";
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private Toolbar myToolbar;
    private ImageButton btn_phone, btn_video, btn_sos, btn_guide_ok,toolbar_guide,btn_record,btn_tool;
    private Button btn_sendTime;
    private FrameLayout help_guide;
    private TextClock textClock;
    private String myId, myGroup, myName, myPhone;
    private TextView toolbar_title;
    private ArrayList<String> countList;
    private pl.droidsonroids.gif.GifTextView notificationGif;
    private DatabaseReference refHour,refMinute,refSaveTime, mDatabaseRef;
    private FirebaseData firebaseData;
    private int firebaseVideo = 0 ,mSQLiteVideo = 0, hour = 0, minute = 0;
    private Boolean hasNewVideo=false;
    private Date formattedDate;

    private DrawerLayout drawer;
    private TextView textViewName,textViewPhone,textViewAddress,textViewBirthday,textViewRoom,text_group_name;
    private ImageView profileImg,ic_one;
    private ImageButton btn_editProfile;

    RoundImage roundedImage;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //if (Build.VERSION.SDK_INT >= 23) {
        //}
        findViews();
        initDB();
        setToolbar();
        setListeners();
        countFirebaseVideo();
        getFirebaseSendTime();
        closeDB();
    }

    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        drawer = findViewById(R.id.drawer_layout);
        myToolbar = findViewById(R.id.toolbar_with_guide);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_guide = findViewById(R.id.toolbar_btn_guide);
        btn_phone = findViewById(R.id.btn_phone);
        btn_video = findViewById(R.id.btn_video);
        btn_tool = findViewById(R.id.btn_tool);
//        btn_map = findViewById(R.id.btn_map);
//        btn_magnifier = findViewById(R.id.btn_magnifier);
        btn_sos = findViewById(R.id.btn_sos);
        btn_guide_ok = findViewById(R.id.btn_guide_ok);
        help_guide = findViewById(R.id.help_guide);
        btn_sendTime = findViewById(R.id.btn_sendTime);
        btn_record = findViewById(R.id.btn_record);

    //profile drawer
        text_group_name = findViewById(R.id.text_group_name);
        textViewName = findViewById(R.id.textViewName);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewBirthday = findViewById(R.id.textViewBirthday);
        textViewRoom = findViewById(R.id.textViewRoom);
        profileImg = findViewById(R.id.profileImg);
        btn_editProfile = findViewById(R.id.btn_editProfile);
        notificationGif = findViewById(R.id.notificationGif);
        notificationGif.setZ(999);
        notificationGif.setBackgroundResource(R.drawable.gif_notification);

//        Drawable drawable;
//        Resources res = this.getResources();
//        if(isElder()){
//            drawable = res.getDrawable(R.drawable.ic_elder, getTheme());
//            profileImg.setImageDrawable(drawable);
//        }
//        else{
//            drawable = res.getDrawable(R.drawable.ic_family, getTheme());
//            profileImg.setImageDrawable(drawable);
//        }
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------- OnClick Listeners ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setListeners(){
        btn_phone.setOnClickListener(ImageBtnListener);
        btn_video.setOnClickListener(ImageBtnListener);
        btn_record.setOnClickListener(ImageBtnListener);
        btn_guide_ok.setOnClickListener(ImageBtnListener);
        btn_editProfile.setOnClickListener(ImageBtnListener);
        // SOS Button
        btn_sos.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SosActivity.class);
                intent.putExtra("myGroup",myGroup);
                intent.putExtra("myPhone",myPhone);
                startActivity(intent);
                return true;
            }
        });
        // SendTime Button
        btn_sendTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSendTime();
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ ImageBtnListener --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private android.view.View.OnClickListener ImageBtnListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_phone:
                    //Toast.makeText(HomeActivity.this, "phone !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeActivity.this, ContactListActivity.class));
                    finish();
                    break;
                case R.id.btn_video:
                    //startActivity(new Intent(HomeActivity.this, WatchVideoActivity.class));
                    //ic_one.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(getApplicationContext(),VideoElderActivity.class);
                    intent.putExtra("myGroup",myGroup);
                    intent.putExtra("myId", myId);
                    startActivity(intent);
                    finish();
                    break;
//                case R.id.btn_map:
//                    if (hasValidAddress()){
//                        startActivity(new Intent(HomeActivity.this, NavigationActivity.class));
//                        finish();
//                        break;
//                    } else{
//                        startActivity(new Intent(HomeActivity.this, NavigationPopUpActivity.class));
//                        //Toast.makeText(HomeActivity.this, "set address !", Toast.LENGTH_SHORT).show();
//                        break;
//                    }
                case R.id.btn_record:
                    Intent intent_record = new Intent(getApplicationContext(), VideoFamilyActivity.class);
                    intent_record.putExtra("mName",myName);
                    intent_record.putExtra("groupNum",myGroup);
                    intent_record.putExtra("mId",myId);
                    intent_record.putExtra("hour", hour);
                    intent_record.putExtra("minute", minute);
                    startActivity(intent_record);
                    finish();
                    break;
                case R.id.btn_tool:
                    Toast.makeText(HomeActivity.this, "btn_tool !", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_guide_ok:
                    closeGuide();
                    break;
                case R.id.btn_editProfile:
                    startActivity(new Intent(HomeActivity.this, ProfileAddActivity.class));
                    finish();
                    break;
            }
        }
    };

    private void SosActivity(){
        Intent homeIntent = new Intent(HomeActivity.this, SosActivity.class);
        startActivity(homeIntent);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------- Set firebase send time--------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void getFirebaseSendTime(){
        refHour = FirebaseDatabase.getInstance().getReference("groups").child(myGroup).child("members").child(myId);
        refMinute = FirebaseDatabase.getInstance().getReference("groups").child(myGroup).child("members").child(myId);

        refHour.child("TimeHour").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hour = Integer.parseInt(dataSnapshot.getValue(String.class));
                btn_sendTime.setText(hour + ":" + minute);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        refMinute.child("TimeMinute").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                minute = Integer.parseInt(dataSnapshot.getValue(String.class));
                btn_sendTime.setText(hour + ":" + minute);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }
    private void setSendTime(){
        //抓現在時間
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        new TimePickerDialog(HomeActivity.this, new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
                hour = hourOfDay;
                minute = minutes;
                Toast.makeText(HomeActivity.this, "Send Time is: "+ hour + ":" + minute, Toast.LENGTH_SHORT).show();
                btn_sendTime.setText(hour + ":" + minute);
                saveTimeToFirebase(Integer.toString(hour),Integer.toString(minute));
            }
        }, hour, minute, false).show();
    }
    private void saveTimeToFirebase(String hour, String minute){
        refSaveTime = FirebaseDatabase.getInstance().getReference("groups").child(myGroup).child("members").child(myId);
        refSaveTime.child("TimeHour").setValue(hour);
        refSaveTime.child("TimeMinute").setValue(minute);
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------- Firebase Video -----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void countFirebaseVideo(){
        //取得當天日期
        Date currentDate = new Date();
        DateFormat dateFormat= new SimpleDateFormat("yyyy年MM月dd日");
        try{
            formattedDate = dateFormat.parse(dateFormat.format(currentDate));
            //Log.d("today",formattedDate.toString());
        }catch(ParseException parseEx){
            parseEx.printStackTrace();
        }

        countList = new ArrayList<>();
        countList.clear();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup);
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
                        firebaseVideo++;
                    }

//                    String count = firebaseData.getStoragePath();
//                    countList.add(count);
                }
                Log.d("home page firebaseVnum", String.valueOf(firebaseVideo));
                if(firebaseVideo != mSQLiteVideo){
                    hasNewVideo = true;
                    Log.d("hello", "have new video");
                }
                if(hasNewVideo) {
                    notificationGif.setVisibility(View.VISIBLE);
                    Log.d("hasNewVideo?", "yes");
                }else{
                    notificationGif.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setToolbar(){
        toolbar_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGuide();
            }
        });

        setSupportActionBar(myToolbar);
        toolbar_title.setText("您好，" + myName);
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
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        myId = cursor.getString(cursor.getColumnIndex("uid"));
        myGroup = cursor.getString(cursor.getColumnIndex("room"));
        myName = cursor.getString(cursor.getColumnIndex("name"));
        myPhone = cursor.getString(cursor.getColumnIndex("phone"));
        mSQLiteVideo = cursor.getInt(cursor.getColumnIndex("notification"));

        text_group_name.setText(myGroup);
        textViewName.setText( myName );
        textViewPhone.setText( myPhone );
        textViewAddress.setText( cursor.getString(cursor.getColumnIndex("address")) );
        textViewBirthday.setText( cursor.getString(cursor.getColumnIndex("birthday")) );
        textViewRoom.setText( cursor.getString(cursor.getColumnIndex("room")) );
        // Load image from Database
        try {
            //initDB();
            //byte[] bytes = dbHelper.retrieveImageFromDB();
            //Log.d("byte load from DB",bytes.toString());
            String imageString = dbHelper.retrieveImageFromDB();
            Log.d("String load from DB",imageString);
            dbHelper.close();
            // Show Image from DB in ImageView
            //profileImg.setImageBitmap(Utils.getImage(bytes));
            profileImg.setImageURI(Uri.parse(imageString));

            BitmapDrawable drawable = (BitmapDrawable) profileImg.getDrawable();
            bitmap = drawable.getBitmap();
            roundedImage = new RoundImage(bitmap);
            profileImg.setImageDrawable(roundedImage);

        } catch (Exception e) {
            //Log.e(TAG, "<loadImageFromDB> Error : " + e.getLocalizedMessage());
            dbHelper.close();
        }
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- Valid Address ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private boolean hasValidAddress(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        String address = cursor.getString(cursor.getColumnIndex("address"));
        if((address.contains("路")||address.contains("街")) && address.contains("號")){
            return true;
        }else {
            return false;
        }
    }

    //Database : close database
    private void closeDB(){
        dbHelper.close();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- help and Guide --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void openGuide(){
        help_guide.setVisibility(View.VISIBLE);
        btn_sos.setClickable(false);
        btn_phone.setClickable(false);
        btn_video.setClickable(false);
        textClock.setVisibility(View.INVISIBLE);

        //notification_num.setVisibility(View.INVISIBLE);
        //ic_one.setVisibility(View.INVISIBLE);
    }
    private void closeGuide(){
        help_guide.setVisibility(View.INVISIBLE);
        textClock.setVisibility(View.VISIBLE);
        btn_sos.setClickable(true);
        btn_phone.setClickable(true);
        btn_video.setClickable(true);
        btn_sendTime.setClickable(true);
        btn_tool.setClickable(true);

        //notification_num.setVisibility(View.VISIBLE);
        //ic_one.setVisibility(View.VISIBLE);
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
    }
}
