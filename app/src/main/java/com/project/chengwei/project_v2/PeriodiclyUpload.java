package com.project.chengwei.project_v2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Angela on 2017/8/28.
 */

public class PeriodiclyUpload extends BroadcastReceiver{

    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private String date, groupNum, mId, mName, storagePath;

    @Override
    public void onReceive(final Context context, Intent intent) {

        //取得目前時間
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        date = formatter.format(curDate);

        groupNum = intent.getExtras().get("groupNum").toString();
        mId = intent.getExtras().get("mId").toString();
        mName = intent.getExtras().get("member").toString();
        storagePath = intent.getExtras().get("storagePath").toString();

        File tmpFile = new File(storagePath);
        Uri filePath = Uri.fromFile(tmpFile);

        //Create file metadata including the content type
        //If you do not provide a contentType and Cloud Storage cannot infer a default from the file extension, Cloud Storage uses application/octet-stream.
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("video/mp4")
                .build();

        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("影片上傳")
                .setSmallIcon(R.drawable.upload)
                .setPriority(Notification.PRIORITY_HIGH);

        if (filePath != null) {
            mStorage = FirebaseStorage.getInstance();
            mStorageRef = mStorage.getReference();
            StorageReference ref = mStorageRef.child("videos").child(groupNum).child(filePath.getLastPathSegment());
            //Storage upload
            ref.putFile(filePath, metadata)
                    //上傳完畢
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //取得下載的url
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            //Database Upload
                            mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(groupNum).child("mVideo");

                            Map<String, Object> videoData = new HashMap<>();
                            videoData.put("mId", mId);
                            videoData.put("member", mName);
                            videoData.put("date", date);
                            videoData.put("storagePath", downloadUri.toString());
                            mDatabaseRef.push().setValue(videoData);

                            //獲取電源管理器對象
                            PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
                            if(!pm.isInteractive()){
                                //獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
                                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
                                //點亮屏幕
                                wl.acquire();
                                uploadDone(context);
                                //要釋放，否則會非常耗電
                                wl.release();
                            }else{
                                uploadDone(context);
                            }

                        }
                    })
                    //上傳失敗
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            mBuilder.setContentText("上傳失敗")
                                    .setProgress(0,0,false);
                            mNotifyManager.notify(100, mBuilder.build());
                        }
                    })
                    //上傳中
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            mBuilder.setProgress(100, ((int) progress), false)
                                    .setContentText("上傳中 " + ((int) progress) + "%");
                            mNotifyManager.notify(100, mBuilder.build());
                        }
                    });
        }
    }

    //上傳完畢
    public void uploadDone(Context context){
        Intent repeating_intent = new Intent(context, HomeActivity.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);

        //setVibrate時使用(振動頻率)
        long[] vibratepattern={400,500};

        mBuilder.setProgress(0,0,false)
                // Removes the progress bar↑
                .setContentIntent(pendingIntent)
                .setVibrate(vibratepattern)
                .setSmallIcon(R.drawable.upload_done)
                .setContentText("上傳完畢")
                .setAutoCancel(true);
        mNotifyManager.notify(100, mBuilder.build());
    }
}
