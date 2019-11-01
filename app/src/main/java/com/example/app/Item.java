package com.example.app;

public class Item {

    private String name;
    private String expdate;

    public Item(){}

    public Item(String name, String expdate) {
        this.name = name;
        this.expdate = expdate;
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
}
