package com.example.app.models;


import com.google.firebase.Timestamp;

public class Item {

    private String name;
    private String expdate;
    private String userID;
    private String storagelocation;
    private Timestamp timestamp;

    public Item(){}

    public Item(String name, String expdate, String userID, String storagelocation, Timestamp timestamp) {
        this.name = name;
        this.expdate = expdate;
        this.userID = userID;
        this.storagelocation = storagelocation;
        this.timestamp = timestamp;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
