package com.project.chengwei.project_v2;

/**
 * Created by Angela on 2017/8/30.
 */

public class MemberData {
    private String mGroup;
    private String mId;
    private String mImage;
    private String mName;
    private String mPhone;
    private String TimeHour;
    private String TimeMinute;

    private MemberData(){}

    public MemberData(String mGroup, String mId, String mImage, String mName, String mPhone, String TimeHour, String TimeMinute){
        this.mGroup = mGroup;
        this.mId = mId;
        this.mImage = mImage;
        this.mName = mName;
        this.mPhone = mPhone;
        this.TimeHour = TimeHour;
        this.TimeMinute = TimeMinute;
    }

    public String getmGroup(){
        return mGroup;
    }

    public String getmId(){
        return mId;
    }

    public String getmImage(){
        return mImage;
    }

    public String getmName(){
        return mName;
    }

    public String getmPhone(){
        return mPhone;
    }

    public String getTimeHour(){
        return TimeHour;
    }

    public String getTimeMinute(){
        return TimeMinute;
    }
}
