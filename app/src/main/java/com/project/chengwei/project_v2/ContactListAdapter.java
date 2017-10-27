package com.project.chengwei.project_v2;

/**
 * Created by chengwei on 2017/7/24.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class ContactListAdapter extends BaseAdapter{

    private Context context;
    private int layout;
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
        TextView txtName;
        ProgressBar progressBar;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);
            holder.txtName = row.findViewById(R.id.txtName);
            //holder.txtPhone = (TextView) row.findViewById(R.id.txtPhone);
            holder.imgPerson = row.findViewById(R.id.imgPerson);
            holder.progressBar = row.findViewById(R.id.progressBar);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }

        Contact person = personsList.get(position);
        holder.txtName.setText(person.getName());
        //holder.txtPhone.setText(person.getPhone());
        /*holder.imgPerson.setImageURI(Uri.parse(person.getImage()));*/

        Glide.with(context)
                .load(person.getImage())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        //holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        //holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                //.placeholder(R.drawable.ic_cake)//loading時候的Drawable
                .error(R.drawable.ic_family)//load失敗的Drawable
                .into(holder.imgPerson);

        return row;
    }
}

