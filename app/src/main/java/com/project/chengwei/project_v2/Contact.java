package com.project.chengwei.project_v2;

/**
 * Created by chengwei on 2017/7/24.
 */

public class Contact {
    private int id;
    private String name;
    private String phone;
    private String image;

    public Contact(String name, String phone, String image, int id) {
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

