package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class SetUpActivity extends AppCompatActivity {
    static final String KEY =  "com.<your_app_name>";
    static final String GROUP_NUM =  "GROUP_NUM";
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    private SQLiteDBHelper dbHelper;

    private ImageButton btn_elder, btn_family;
    private Button btn_start,btn_create;
    private FrameLayout guide_room;
    private EditText editTextName;
    private EditText edit_group_num1,edit_group_num2,edit_group_num3,edit_group_num4;
    private TextView instruction1,instruction2,instruction3;


    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private DatabaseReference root;

    private String strName,strRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        findViews();
        setUpListeners();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        guide_room = (FrameLayout) findViewById(R.id.guide_room);
        instruction1 = (TextView) findViewById(R.id.instruction1);
        instruction2 = (TextView) findViewById(R.id.instruction2);
        instruction3 = (TextView) findViewById(R.id.instruction3);
        //editTextRoom = (EditText) findViewById(R.id.edit_group_num);
        editTextName =  (EditText) findViewById(R.id.edit_name);
        btn_elder = (ImageButton)findViewById(R.id.btn_elder);
        btn_family = (ImageButton)findViewById(R.id.btn_family);
        btn_start = (Button)findViewById(R.id.btn_start);
        btn_create = (Button)findViewById(R.id.btn_create);
        edit_group_num1  = (EditText) findViewById(R.id.edit_group_num1);
        edit_group_num2  = (EditText) findViewById(R.id.edit_group_num2);
        edit_group_num3  = (EditText) findViewById(R.id.edit_group_num3);
        edit_group_num4  = (EditText) findViewById(R.id.edit_group_num4);
        edit_group_num1.setSelection(0);
        edit_group_num2.setSelection(0);
        edit_group_num3.setSelection(0);
        edit_group_num4.setSelection(0);
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------- Onclick Listeners --------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void setUpListeners(){
        btn_elder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(ELDERLY_MODE, true).commit();
                showSelectRoom();
            }
        });
        btn_family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(KEY, Context.MODE_PRIVATE).edit().putBoolean(ELDERLY_MODE, false).commit();
                //signIn();
                showSelectRoom();
                showCreateRoom();
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strName = editTextName.getText().toString();
                String num1 = edit_group_num1.getText().toString();
                String num2 = edit_group_num2.getText().toString();
                String num3 = edit_group_num3.getText().toString();
                String num4 = edit_group_num4.getText().toString();
                strRoom = num1 + num2 + num3 +num4;

                if(hasName()==true && isValidRoomNum(strRoom)==true) {
                save();
                if (isElder()) {
                    ElderEnter();
                } else {
                    FamilyEnter();
                }
            }
            }
        });

    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
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
                            Toast.makeText(SetUpActivity.this, "login success. "+user.getUid(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("hi", "signInAnonymously:failure", task.getException());
                            Toast.makeText(SetUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }
    public void showSelectRoom(){
        instruction1.setVisibility(FrameLayout.INVISIBLE);
        editTextName.setVisibility(FrameLayout.INVISIBLE);
        guide_room.setVisibility(FrameLayout.VISIBLE);
        btn_elder.setClickable(false);
        btn_family.setClickable(false);
        guideAnimation();
    }
    public void showCreateRoom(){
        instruction3.setVisibility(FrameLayout.VISIBLE);
        btn_create.setVisibility(FrameLayout.VISIBLE);
        guideAnimation();
    }
    public void ElderEnter(){
        Intent intent = new Intent();
        intent.setClass(SetUpActivity.this , HomeActivity.class);
        startActivity(intent);
        finish();
        //get into firebase
        //root = FirebaseDatabase.getInstance().getReference().child(group).child(groupNum);
        //Toast.makeText(SetUpActivity.this, "enter" + groupNum, Toast.LENGTH_SHORT).show();
    }
    public void FamilyEnter(){
        startActivity(new Intent(SetUpActivity.this, FamilyActivity.class));
        finish();
        //root = FirebaseDatabase.getInstance().getReference().child(group).child(groupNum);
        //Toast.makeText(SetUpActivity.this, "enter" + groupNum, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    private void updateUI(FirebaseUser user) {}

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Database -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //Database : initial database
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
    }
    //Database : save the change to database
    public void save() {
        String strPhone = "0123456789";
        String strAddr = "高雄市蓮海路70號";
        String birthday = "2000-01-01";

        initDB();
        Cursor cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        dbHelper.editProfileData(strName, strPhone ,strAddr, birthday, strRoom);
        closeDB();
        alertSuccess();
    }
    private boolean hasName(){
        if(editTextName==null){
            showMessage("請輸入姓名！");
            return false;
        }
        return true;
    }
    private boolean isValidRoomNum(String editTextRoom){
        if(editTextRoom.length()!=4){
            showMessage("請出入正確的群組號碼");
            return false;
        }
        return true;
    }
    // Show simple message using SnackBar
    void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
    //Database : close database
    private void closeDB(){
        dbHelper.close();
    }
    public void alertSuccess() {
//        Toast toast = Toast.makeText(this, "saved!", Toast.LENGTH_SHORT);
//        toast.show();
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                finish();
//            }
//        }, 2 * 1000);
    }

    //--------------------------------------------------------------------------------------------//
    //------------------------------ setAnimationListener ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void guideAnimation() {
        //AlphaAnimation(float fromAlpha, float toAlpha)
        //fromAlpha 起始透明度(Alpha)值
        //toAlpha 最後透明度(Alpha)值
        // 1.0f~0.0f
        //Animation am = new AlphaAnimation(1.0f, 0.0f);
        //setDuration (long durationMillis) 設定動畫開始到結束的執行時間
        //am.setDuration(2000);
        //setRepeatCount (int repeatCount) 設定重複次數 -1為無限次數 0
        //am.setRepeatCount(-1);
        //將動畫參數設定到圖片並開始執行動畫
        //btn_start.startAnimation(am);

//        final TranslateAnimation run = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
//        run.setDuration(2000);


//        btn_start.startAnimation(run);
//        run.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation arg0) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation arg0) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//                // TODO Auto-generated method stub
//                run.setFillAfter(true);
//            }
//        });
    }
}

