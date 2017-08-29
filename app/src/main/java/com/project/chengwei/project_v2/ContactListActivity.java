package com.project.chengwei.project_v2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {
    static final String KEY =  "com.<your_app_name>";
    static final String ELDERLY_MODE = "ELDERLY_MODE";
    private Toolbar myToolbar;
    private ImageButton toolbar_add_contact, toolbar_fav;
    GridView gridView;
    ArrayList<Contact> list;
    ContactListAdapter adapter = null;
    public static SQLiteDBHelper sqLiteDBHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        findViews();
        setToolbar();
        setListeners();
        sqLiteDBHelper = new SQLiteDBHelper(getApplicationContext());
        sqLiteDBHelper.queryContactData("CREATE TABLE IF NOT EXISTS PERSON(Id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, image TEXT)");


        //initialize list data
        list = new ArrayList<>();
        adapter = new ContactListAdapter(this, R.layout.contact_items, list);
        gridView.setAdapter(adapter);

        // get all data from sqlite
        Cursor cursor = sqLiteDBHelper.getContactData("SELECT * FROM PERSON");
        if(cursor == null){
        }
        else {
            list.clear();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String image = cursor.getString(3);

                list.add(new Contact(name, phone, image, id));
                if(!list.isEmpty()){notNullContacts();}
            }
            adapter.notifyDataSetChanged();
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*
            String number = list.get(position).getPhone();

            Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            startActivity(call);
*/
                int pass_id = list.get(position).getId();
                String pass_name = list.get(position).getName();
                String pass_phone = list.get(position).getPhone();
                String pass_image = list.get(position).getImage();

                Intent intent = new Intent();
                intent.setClass(ContactListActivity.this , ContactPopUpActivity.class);

                intent.putExtra("ID", pass_id);
                intent.putExtra("NAME", pass_name);
                intent.putExtra("PHONE", pass_phone);
                intent.putExtra("IMAGE", pass_image);
                startActivity(intent);
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- initial Views ---------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void findViews(){
        myToolbar = (Toolbar) findViewById(R.id.toolbar_contact);
        toolbar_add_contact = (ImageButton)findViewById(R.id.toolbar_add_contact);
        toolbar_fav = (ImageButton)findViewById(R.id.toolbar_fav);
        gridView = (GridView) findViewById(R.id.gridView);
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------------- setListeners ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //新增聯絡人
    private void setListeners(){
//        addBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(ContactListActivity.this, ContactAddActivity.class));
//                finish();
//            }
//        });
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Toolbar --------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void setToolbar(){
        toolbar_add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactListActivity.this, ContactAddActivity.class));
                finish();
            }
        });
        toolbar_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ContactListActivity.this,"fav",Toast.LENGTH_SHORT).show();
            }
        });

        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isElder()) {
                    startActivity(new Intent(ContactListActivity.this, HomeActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(ContactListActivity.this, FamilyActivity.class));
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
                startActivity(new Intent(ContactListActivity.this, HomeActivity.class));
                finish();
            }else{
                startActivity(new Intent(ContactListActivity.this, FamilyActivity.class));
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
    public void notNullContacts() {
        gridView.setBackgroundColor(Color.WHITE);
        //Toast.makeText(ContactListActivity.this,"notNullContacts",Toast.LENGTH_SHORT).show();
    }
}
