package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetUpActivity extends AppCompatActivity {
    private SharedPreferences settings;
    private static final String elderlyMode = "ELDERLY_MODE";
    private static final String data = "DATA";

    private ImageButton btn_elder;
    private ImageButton btn_family;
    private Button btn_start;
    private FrameLayout setup_guide;
    private EditText edit_name;
    private EditText edit_group_num;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private DatabaseReference root;
    static final String KEY_IS_FIRST_TIME =  "com.<your_app_name>.first_time";
    static final String KEY =  "com.<your_app_name>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        findViews();
        setUpFirst();
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        setup_guide = (FrameLayout) findViewById(R.id.setup_guide);
        edit_group_num = (EditText) findViewById(R.id.edit_group_num);
        edit_name =  (EditText) findViewById(R.id.edit_name);
        btn_elder = (ImageButton)findViewById(R.id.btn_elder);
        btn_family = (ImageButton)findViewById(R.id.btn_family);
        btn_start = (Button)findViewById(R.id.btn_start);
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------- SetUp first time ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void setUpFirst(){
        btn_elder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings = getSharedPreferences(data,0);
                settings.edit()
                        .putString("NAME", edit_name.getText().toString())
                        .putBoolean("ELDERLY_MODE",true)
                        .commit();
                //signIn();
                showSelectRoom();
            }
        });
        btn_family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                showSelectRoom();
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isElder()){
                    ElderEnter(edit_group_num.getText().toString());
                }
                else{
                    FamilyEnter(edit_group_num.getText().toString());
                }
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //------------------------------------ CheckPreferences ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        settings = getSharedPreferences(data,0);
        return settings.getBoolean(elderlyMode,false);
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
        setup_guide.setVisibility(FrameLayout.VISIBLE);
    }
    public void ElderEnter(String groupNum){
        //String group = "Group";
        Intent homeIntent = new Intent(getApplicationContext(),HomeActivity.class);
        homeIntent.putExtra("groupNum",groupNum);
        finish();
        startActivity(homeIntent);

        //root = FirebaseDatabase.getInstance().getReference().child(group).child(groupNum);
        //Toast.makeText(SetUpActivity.this, "enter" + groupNum, Toast.LENGTH_SHORT).show();
    }
    public void FamilyEnter(String groupNum){
        //String group = "Group";
        Intent homeIntent = new Intent(getApplicationContext(),FamilyActivity.class);
        homeIntent.putExtra("groupNum",groupNum);
        finish();
        startActivity(homeIntent);

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
    private void updateUI(FirebaseUser user) {

    }


}

