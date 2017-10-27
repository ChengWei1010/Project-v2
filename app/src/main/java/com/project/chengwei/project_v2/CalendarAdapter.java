package com.project.chengwei.project_v2;

/**
 * Created by chengwei on 2017/10/27.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class CalendarAdapter extends BaseAdapter{

     private LayoutInflater myInflater;
        ArrayList<String> list = null;

        public CalendarAdapter(Context ctxt, ArrayList<String> list){
            myInflater = LayoutInflater.from(ctxt);
            this.list = list;
        }

        @Override
        public int getCount() {return list.size();}

        @Override
        public String getItem(int position) {return list.get(position);}

        @Override
        public long getItemId(int position) {return position;}

        @Override
        public View getView(int position,View convertView,ViewGroup parent) {
            //自訂類別，表達個別listItem中的view物件集合。
            ViewTag viewTag;

            if(convertView == null){
                //取得listItem容器 view
                convertView = myInflater.inflate(R.layout.calendar_items, null);

                //建構listItem內容view
                viewTag = new ViewTag(
                        (TextView) convertView.findViewById(R.id.Calendar_title),
                        (ImageView) convertView.findViewById(R.id.Calendar_notification)
                );

                //設置容器內容
                convertView.setTag(viewTag);
            }
            else{
                viewTag = (ViewTag) convertView.getTag();
            }

            //設定內容圖案
//            switch(position){
//                case CalendarAdapter.MyListView_camera:
//                    viewTag.icon.setBackgroundResource(R.drawable.ic_launcher_camera);
//                    break;
//                case CalendarAdapter.MyListView_album:
//                    viewTag.icon.setBackgroundResource(R.drawable.ic_launcher_gallery);
//                    break;
//                case CalendarAdapter.MyListView_map:
//                    viewTag.icon.setBackgroundResource(R.drawable.ic_launcher_maps);
//                    break;
//            }
            //設定內容文字
            viewTag.title.setText(list.get(position));

            return convertView;
        }

        //自訂類別，表達個別listItem中的view物件集合。
        class ViewTag{
            TextView title;
            ImageView notification;

            public ViewTag(TextView title, ImageView notification){
                this.title = title;
                this.notification = notification;
            }
        }
    }
