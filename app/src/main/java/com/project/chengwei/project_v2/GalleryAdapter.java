package com.project.chengwei.project_v2;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Angela on 2017/9/12.
 */

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private LayoutInflater inflater;

    private ArrayList<String> memberList;
    private ArrayList<String> storagePathList;
    private ArrayList<String> imagePathList;

    private TextView textView;
    private ImageView imageView;
    private ImageButton btnVideo, btnDownload;

    private DownloadManager downloadManager;
    private Uri uri;

    public GalleryAdapter(Context context, int layout, ArrayList<String> memberList, ArrayList<String> storagePathList, ArrayList<String> imagePathList){
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.layout = layout;
        this.memberList = memberList;
        this.storagePathList = storagePathList;
        this.imagePathList = imagePathList;
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

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        view = inflater.inflate(layout, null);

        imageView = view.findViewById(R.id.imgMember);
        textView = view.findViewById(R.id.txtMember);
        btnVideo = view.findViewById(R.id.btnVideo);
        btnDownload = view.findViewById(R.id.btnDownload);

        //成員照片
//        String imageString = "https://firebasestorage.googleapis.com/v0/b/elderlyproject-46505.appspot.com/o/images%2F1765%2FIYG7ZEw3lOcoauzUhVOx36Kf5T72.jpg?alt=media&token=d7b1b95e-a813-41fb-bac5-dd6bc4d3396f";
        String imageString = imagePathList.get(position);
        Glide.with(context).load(imageString).into(imageView);
        //成員名字
        textView.setText(memberList.get(position));
        //播放影片按鈕
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context , VideoPopUpActivity.class);
                intent.putExtra("URI", storagePathList.get(position));
                context.startActivity(intent);
            }
        });

        uri = Uri.parse(storagePathList.get(position));

        //影片下載按鈕
        btnDownload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                String date = formatter.format(curDate);

                downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("/videoDownload", memberList.get(position) + "/" + date + ".mp4");
                request.setTitle(memberList.get(position) + ":" + date);
                downloadManager.enqueue(request);
            }
        });


//        View row = view;
//        GalleryAdapter.ViewHolder holder = new GalleryAdapter.ViewHolder();
//
//        if(row == null){
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            row = inflater.inflate(layout, null);
//
//            holder.imgMember = (ImageView) row.findViewById(R.id.imgMember);
//            holder.txtMember = (TextView) row.findViewById(R.id.txtMember);
//            //holder.video = (VideoView) row.findViewById(R.id.videoView);
//            //holder.btnDownload = (ImageButton) row.findViewById(R.id.btnDownload);
//
//            row.setTag(holder);
//        }else{
//            holder = (GalleryAdapter.ViewHolder) row.getTag();
//        }
//
//        uri = Uri.parse(storagePathList.get(position));
//
//        //顯示成員照片
//        holder.imgMember.setImageResource(R.drawable.ic_elder);
//        //顯示成員名字
//        holder.txtMember.setText(memberList.get(position));
//        //顯示影片
////        vidControl = new MediaController(context);
////        vidControl.setAnchorView(holder.video);
////        holder.video.setMediaController(vidControl);
////        holder.video.setVideoURI(uri);
////        //顯示下載按鈕
////        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
////            public void onClick(View v) {
////                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
////                Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
////                String date = formatter.format(curDate);
////
////                downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
////                DownloadManager.Request request = new DownloadManager.Request(uri);
////                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
////                request.setDestinationInExternalPublicDir("/videoDownload", memberList.get(position) + "/" + date + ".mp4");
////                request.setTitle(memberList.get(position) + ":" + date);
////                downloadManager.enqueue(request);
////            }
////        });
//
        return view;
    }
}
