package com.example.bookify.data;

public class Event {
    private int id;
    private String title;
    private String location;
    private String date;
    private String category;
    private String price;
    private boolean isPrivate;

    public Event(int id, String title, String location, String date,
                 String category, String price, boolean isPrivate) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.category = category;
        this.price = price;
        this.isPrivate = isPrivate;
    }

    public int getId()          { return id; }
    public String getTitle()    { return title; }
    public String getLocation() { return location; }
    public String getDate()     { return date; }
    public String getCategory() { return category; }
    public String getPrice()    { return price; }
    public boolean isPrivate()  { return isPrivate; }
}