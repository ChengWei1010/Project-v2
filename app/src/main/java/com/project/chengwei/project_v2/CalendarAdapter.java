package com.project.chengwei.project_v2;

/**
 * Created by chengwei on 2017/10/27.
 */
import android.app.DownloadManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CalendarAdapter extends BaseAdapter{

    private ImageButton notification;
    private TextView date, title;
    private LayoutInflater myInflater;
    ArrayList<String> dateList = null;
    ArrayList<String> titleList = null;

    public CalendarAdapter(Context ctxt, ArrayList<String> dateList, ArrayList<String> titleList){

        myInflater = LayoutInflater.from(ctxt);
        this.dateList = dateList;
        this.titleList = titleList;
    }

    @Override
    public int getCount() {return dateList.size();}

    @Override
    public String getItem(int position) {return dateList.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(final int position,View convertView,ViewGroup parent) {
        //自訂類別，表達個別listItem中的view物件集合。
        //ViewTag viewTag;

        if (convertView == null) {
            //取得listItem容器 view
            convertView = myInflater.inflate(R.layout.calendar_items, null);

            date = convertView.findViewById(R.id.Calendar_date);
            title = convertView.findViewById(R.id.Calendar_title);
            notification = convertView.findViewById(R.id.Calendar_notification);

            date.setText(dateList.get(position));
            title.setText(titleList.get(position));

            notification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notification.setBackgroundResource(R.drawable.notification_1);
                }
            });

        }
            return convertView;


    //自訂類別，表達個別listItem中的view物件集合。
//    class ViewTag{
//        TextView title;
//        ImageView notification;
//
//        public ViewTag(TextView title, ImageButton notification){
//            this.title = title;
//            this.notification = notification;
//        }
    }
}
