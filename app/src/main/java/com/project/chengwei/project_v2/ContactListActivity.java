package com.project.chengwei.project_v2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {
        GridView gridView;
        ArrayList<Contact> list;
        ContactListAdapter adapter = null;

        public static SQLiteDBHelper sqLiteDBHelper;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_contact_list);

            //Toolbar
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home);
            setSupportActionBar(myToolbar);
            myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);

            sqLiteDBHelper = new SQLiteDBHelper(getApplicationContext());
            sqLiteDBHelper.queryContactData("CREATE TABLE IF NOT EXISTS PERSON(Id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, image TEXT)");

            //新增聯絡人
            Button addBtn = (Button)findViewById(R.id.addBtn);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(ContactListActivity.this , ContactAddActivity.class);
                    startActivity(intent);
                }
            });

            //initialize list data
            gridView = (GridView) findViewById(R.id.gridView);
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

}
