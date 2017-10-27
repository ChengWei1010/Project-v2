package com.project.chengwei.project_v2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Angela on 2017/9/9.
 */

public class MemberListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<String> memberList;

    public MemberListAdapter(Context context, int layout, ArrayList<String> memberList){
        this.context = context;
        this.layout = layout;
        this.memberList = memberList;
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        TextView txtMember;
        ImageView imgMember;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);

            holder.txtMember = row.findViewById(R.id.txtMember);
            holder.imgMember = row.findViewById(R.id.imgMember);

            row.setTag(holder);
        }else{
            holder = (ViewHolder) row.getTag();
        }

        holder.txtMember.setText(memberList.get(position));
        holder.imgMember.setImageResource(R.drawable.ic_elder);

        return row;
    }
}
