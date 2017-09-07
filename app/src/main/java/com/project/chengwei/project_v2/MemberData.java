package com.project.chengwei.project_v2;

/**
 * Created by Angela on 2017/8/30.
 */

public class MemberData {
    private String mGroup;
    private String mId;
    private String mName;
    private String mStatus;

    private MemberData(){}

    public MemberData(String mGroup, String mId, String mName, String mStatus){
        this.mGroup = mGroup;
        this.mId = mId;
        this.mName = mName;
        this.mStatus = mStatus;
    }

    public String getmGroup(){
        return mGroup;
    }

    public String getmId(){return mId;}

    public String getmName(){
        return mName;
    }

    public String getmStatus(){
        return mStatus;
    }
}
