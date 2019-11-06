package com.example.app.models;

public class Item {

    private String name;
    private String expdate;
    private String userID;
    private String storagelocation;

    public Item(){}

    public Item(String name, String expdate, String userID, String storagelocation) {
        this.name = name;
        this.expdate = expdate;
        this.userID = userID;
        this.storagelocation = storagelocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpdate() {
        return expdate;
    }

    public void setExpdate(String expdate) {
        this.expdate = expdate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStoragelocation() {
        return storagelocation;
    }

    public void setStoragelocation(String storagelocation) {
        this.storagelocation = storagelocation;
    }
}
