package com.example.app.models;

import com.google.firebase.Timestamp;

public class Item {

    private String name;
    private String expdate;
    private String userID;
    private String storagelocation;
    private Timestamp expTimestamp;
    private Timestamp createdAt;

    public Item() {}

    public Item(final String name, final String expdate, final String userID, final String storagelocation, final Timestamp expTimestamp,
                final Timestamp createdAt) {
        this.name = name;
        this.expdate = expdate;
        this.userID = userID;
        this.storagelocation = storagelocation;
        this.expTimestamp = expTimestamp;
        this.createdAt = createdAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getExpdate() {
        return expdate;
    }

    public void setExpdate(final String expdate) {
        this.expdate = expdate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }

    public String getStoragelocation() {
        return storagelocation;
    }

    public void setStoragelocation(final String storagelocation) {
        this.storagelocation = storagelocation;
    }

    public Timestamp getExpTimestamp() {
        return expTimestamp;
    }

    public void setExpTimestamp(final Timestamp expTimestamp) {
        this.expTimestamp = expTimestamp;
    }
}
