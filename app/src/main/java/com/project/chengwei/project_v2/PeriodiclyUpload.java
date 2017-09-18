package com.project.chengwei.project_v2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Angela on 2017/8/28.
 */

public class PeriodiclyUpload extends BroadcastReceiver{

    //private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private String date, groupNum, mId, mName, storagePath;

    final FirebaseDatabase database = FirebaseDatabase.getInstance(); // Get a reference to our posts
    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups"); // From groups in Database
    MemberData memberData;
    private ArrayList<String> memberList_ID = new ArrayList<>();
    private ArrayList<String> memberList_NAME = new ArrayList<>();
    static public FirebaseAuth mAuth;
    FirebaseUser user;
    static String currentUserID_Notification;
    static boolean signal = false;

    @Override
    public void onReceive(final Context context, Intent intent) {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserID_Notification = user.getUid();

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
        Intent repeating_intent = new Intent(context, FamilyActivity.class);
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

        Log.d("TESTING", "GETGROUP..... init");
        Test_getGroupsAllMember();
    }

    private void Test_getGroupsAllMember() {

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups");
        /*
        mDatabaseRef.child(groupNum).child("mVideo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                videoList.clear();
                for (DataSnapshot child : children) { // shake hands with each of them.'

                    videoData = child.getValue(VideoData.class); //抓出成員
                    videoList.add(videoData.getmId()); //存 member.mId 到 arrayList
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        */
        mDatabaseRef.child(groupNum).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                memberList_ID.clear();
                memberList_NAME.clear();
                for (DataSnapshot child : children) { // shake hands with each of them.'

                    memberData = child.getValue(MemberData.class); //抓出成員
                    memberList_ID.add(memberData.getmId()); //存 member.mId 到 arrayList
                    memberList_NAME.add(memberData.getmName());
                }
                //Toast.makeText(SignIn.this, "count: "+ memberList.size(), Toast.LENGTH_SHORT).show();
                //Log.i("Member mId: ", memberList.get(1));
                /*
                for (int i = 0; i<memberList.size(); i++){
                    Log.i("Member mId: ", memberList.get(i));
                }
                */
                if (memberList_ID.size() <= 1){

                }else
                    Log.d("TESTING", "Send init");
                sendNotification();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*
        if (memberList.size() <= 1 || videoList.size() <= 1) {

            //Toast.makeText(SignIn.this, "You'r the only member, no sendNotification", Toast.LENGTH_SHORT).show();
            Log.d("TESTING", "You'r the only member, no sendNotification");
        } else if(memberList.size() > 1 && videoList.size() > 0) {

            //Toast.makeText(SignIn.this, "check send", Toast.LENGTH_SHORT).show();
            Log.d("TESTING", "check sent");
            //CheckUpdate();
            sendNotification();
        }
        else {
            //Toast.makeText(SignIn.this, "memberCount < 2 or no Video , no sendNotification", Toast.LENGTH_SHORT).show();
            Log.d("TESTING", "memberCount < 2 or no Video , no sendNotification");
        }
        */

    }


    private void CheckUpdate() {
        mDatabaseRef.child("mVideo").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String PreviousChild) {

                sendNotification();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                sendNotification();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

        /*----------------------------------------------------------------------------------------*/
        /*-------------------------------------Send method----------------------------------------*/
        /*----------------------------------------------------------------------------------------*/

    public void sendNotification()
    {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String send_mId, send_mName;

                    Log.d("TESTING", "Send start");
                    //Log.d("TESTING", "ThisUser_mId_f: "+ FamilyActivity.currentUserID_Notification);
                    //Log.d("TESTING", "ThisUser_mId_s: "+ SetUpActivity.LoggedIn_User_mId);
                    Log.d("TESTING", "ThisUser_mId_c: "+ currentUserID_Notification);
                    //Logic which Send Notificat_ion different Device Programmatically....
                    for (int i=0; i<memberList_ID.size(); i++) {

                        send_mId = memberList_ID.get(i);
                        send_mName = memberList_NAME.get(i);
                        Log.d("TESTING", "looper_mId: "+ send_mId);

                        //mAuth = FirebaseAuth.getInstance(); // important Call
                        //FirebaseUser user = FamilyActivity.mAuth.getCurrentUser();
                        if (send_mId.equals(currentUserID_Notification)) {
                            continue;
                        }

                        try {
                            String jsonResponse;

                            URL url = new URL("https://onesignal.com/api/v1/notifications");
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setUseCaches(false);
                            con.setDoOutput(true);
                            con.setDoInput(true);

                            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            con.setRequestProperty("Authorization", "Basic ODI3MjUzNDUtMmQ2Mi00ODczLWFmMGMtYmNjOTgxZjJkZDkw");
                            con.setRequestMethod("POST");

                            String strJsonBody = "{"
                                    + "\"app_id\": \"121eb60c-9642-4d9c-9c1c-45f5bf970bbc\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_mId + "\"}],"

                                    + "\"data\": {\"foo\": \"bar\"},"
                                    + "\"contents\": {\"en\": \"message\"},"
                                    + "\"contents\": {\"en\": \"來自一則 "+ send_mName +" 傳的新影片\"}"
                                    + "}";


                            System.out.println("strJsonBody:\n" + strJsonBody);

                            byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                            con.setFixedLengthStreamingMode(sendBytes.length);

                            OutputStream outputStream = con.getOutputStream();
                            outputStream.write(sendBytes);

                            int httpResponse = con.getResponseCode();
                            System.out.println("httpResponse: " + httpResponse);

                            if (httpResponse >= HttpURLConnection.HTTP_OK
                                    && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                                Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                                scanner.close();
                            } else {
                                Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                                scanner.close();
                            }
                            System.out.println("jsonResponse:\n" + jsonResponse);
                            signal = true;

                        } catch(Throwable t){
                            t.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
