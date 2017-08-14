package com.project.chengwei.project_v2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {
    private Toolbar myToolbar;
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
                //
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
        myToolbar = (Toolbar) findViewById(R.id.toolbar_home);
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
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startActivity(new Intent(ContactListActivity.this, HomeActivity.class));
            finish();
            }
        });
    }
    //--------------------------------------------------------------------------------------------//
    //----------------------------------- Options Item -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addContact:
                Toast.makeText(ContactListActivity.this,"add",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ContactListActivity.this, ContactAddActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
