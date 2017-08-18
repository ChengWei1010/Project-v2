package com.project.chengwei.project_v2;

/**
 * Created by chengwei on 2017/7/24.
 */

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactListAdapter extends BaseAdapter {

    private Context context;
    private  int layout;
    private ArrayList<Contact> personsList;

    public ContactListAdapter(Context context, int layout, ArrayList<Contact> personsList) {
        this.context = context;
        this.layout = layout;
        this.personsList = personsList;
    }

    @Override
    public int getCount() {
        return personsList.size();
    }

    @Override
    public Object getItem(int position) {
        return personsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        ImageView imgPerson;
        TextView txtName, txtPhone;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);
            holder.txtName = (TextView) row.findViewById(R.id.txtName);
            //holder.txtPhone = (TextView) row.findViewById(R.id.txtPhone);
            holder.imgPerson = (ImageView) row.findViewById(R.id.imgPerson);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }

        Contact person = personsList.get(position);
        holder.txtName.setText(person.getName());
        //holder.txtPhone.setText(person.getPhone());
        holder.imgPerson.setImageURI(Uri.parse(person.getImage()));

        return row;
    }
}

