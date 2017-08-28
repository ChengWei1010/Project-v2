package com.project.chengwei.project_v2;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Angela on 2017/8/24.
 */

public class VideoListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<FirebaseData> videoList;
    private DownloadManager downloadManager;
    private MediaController vidControl;

    public VideoListAdapter(Context context, int layout, ArrayList<FirebaseData> videoList){
        this.context = context;
        this.layout = layout;
        this.videoList = videoList;
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int position) {
        return videoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        TextView txtDate, txtMId, txtMember, txtStoragePath;
        VideoView videoView;
        Button downloadBtn;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);

            holder.txtDate = (TextView) row.findViewById(R.id.txtDate);
            holder.txtMId = (TextView) row.findViewById(R.id.txtMId);
            holder.txtMember = (TextView) row.findViewById(R.id.txtMember);
            holder.txtStoragePath = (TextView) row.findViewById(R.id.txtStoragePath);
            holder.videoView = (VideoView) row.findViewById(R.id.videoView);
            holder.downloadBtn = (Button) row.findViewById(R.id.downloadBtn);

            row.setTag(holder);
        }else{
            holder = (ViewHolder) row.getTag();
        }

        final FirebaseData firebaseData = videoList.get(position);
        //顯示日期
        holder.txtDate.setText("日期: " + firebaseData.getDate());
        //顯示mId
        holder.txtMId.setText("MId: " + firebaseData.getmId());
        //顯示member名字
        holder.txtMember.setText("寄件者: " + firebaseData.getMember());
        //顯示影片路徑
        holder.txtStoragePath.setText("影片路徑: " + firebaseData.getStoragePath());
        //顯示影片
        vidControl = new MediaController(context);
        vidControl.setAnchorView(holder.videoView);
        holder.videoView.setMediaController(vidControl);
        final Uri uri = Uri.parse(firebaseData.getStoragePath());
        holder.videoView.setVideoURI(uri);
        //顯示下載按鈕
        holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                String date = formatter.format(curDate);

                downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("/videoDownload", firebaseData.getMember() + "/" + date + ".mp4");
                request.setTitle(firebaseData.getMember() + ":" + date);
                downloadManager.enqueue(request);
            }
        });

        return row;
    }
}
