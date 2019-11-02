package com.example.app.models;

public class Item {

    private String name;
    private String expdate;
    private String userID;

    public Item(){}

    public Item(String name, String expdate, String userID) {
        this.name = name;
        this.expdate = expdate;
        this.userID = userID;
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
}
