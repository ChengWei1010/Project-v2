package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

public class GroupMemberActivity extends AppCompatActivity {
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    static final String KEY =  "com.<your_app_name>";
    private Toolbar myToolbar;
//    private ListView listView;
//    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> memberList;
    private MemberData memberData;
    private String groupNum;
    private GridView gridView;
    private MemberListAdapter adapter = null;

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);

        mAuth = FirebaseAuth.getInstance();
        if (Build.VERSION.SDK_INT >= 23) {
            signIn();
        }

        groupNum = getIntent().getExtras().get("groupNum").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(groupNum);

        findViews();
        setToolbar();
        listMember();
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
                            Toast.makeText(GroupMemberActivity.this, "請檢查網路連線",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews() {
        myToolbar = findViewById(R.id.toolbar_home);
//        listView = (ListView) findViewById(R.id.listView);
        gridView = findViewById(R.id.gridView);
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setToolbar(){
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("房間: " + groupNum);
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isElder()) {
                    startActivity(new Intent(GroupMemberActivity.this, HomeActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(GroupMemberActivity.this, FamilyActivity.class));
                    finish();
                }
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(isElder()) {
                startActivity(new Intent(GroupMemberActivity.this, HomeActivity.class));
                finish();
            }else{
                startActivity(new Intent(GroupMemberActivity.this, FamilyActivity.class));
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------------- Check --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public boolean isElder() {
        return getSharedPreferences(KEY, Context.MODE_PRIVATE).getBoolean(ELDERLY_MODE, true);
    }

    //--------------------------------------------------------------------------------------------//
    //-------------------------- List members in the same room-----------------------------//
    //--------------------------------------------------------------------------------------------//
    public void listMember(){
        memberList = new ArrayList<>();

        adapter = new MemberListAdapter(GroupMemberActivity.this, R.layout.member_item,memberList);
        gridView.setNumColumns(3);
        gridView.setAdapter(adapter);

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

//                listAdapter = new ArrayAdapter(GroupMemberActivity.this,android.R.layout.simple_list_item_1,memberList.toArray(new String[memberList.size()]));
//                listView.setAdapter(listAdapter);
//                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        String name = memberList.get(position);
//                        Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
//                    }
//                });
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(GroupMemberActivity.this, "成為: " + memberList.get(position), Toast.LENGTH_SHORT).show();
                    }
                });
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
