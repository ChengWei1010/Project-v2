package com.project.chengwei.project_v2;

public class CalendarDetails {

    private String date;
    private String time;
    private String title;
    private String content;
    private String eventId;

    public CalendarDetails(){}

    public void setDate(String date){this.date = date; }

    public void setTime(String time){
        this.time = time;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setContent(String content){ this.content = content; }

    public void seteventId(String eventId){ this.eventId = eventId; }


    public String getDate(){
        return date;
    }

    public String getTime(){
        return time;
    }

    public String getTitle(){
        return title;
    }

    public String getContent(){
        return content;
    }

    public String geteventId(){ return eventId; }

}
