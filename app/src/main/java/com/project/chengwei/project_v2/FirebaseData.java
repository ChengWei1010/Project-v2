package com.project.chengwei.project_v2;

/**
 * Created by Angela on 2017/8/23.
 */

public class FirebaseData {
    private String date;
    private String mId;
    private String member;
    private String storagePath;

    private FirebaseData(){}

    public FirebaseData(String date, String mId, String member, String storagePath){
        this.date = date;
        this.mId = mId;
        this.member = member;
        this.storagePath = storagePath;
    }

    public String getDate(){
        return date;
    }

    public String getmId(){
        return mId;
    }

    public String getMember(){
        return member;
    }

    public String getStoragePath(){
        return storagePath;
    }
}
