package com.example.bookify.data;

public class Event {
    private int id;
    private String title;
    private String location;
    private String date;
    private String category;
    private String price;
    private boolean isPrivate;
    private String imageUrl;
    private String time;
    private String slots;
    private String description;

    public Event(int id, String title, String location, String date,
                 String category, String price, boolean isPrivate, String imageUrl,
                 String time, String slots, String description) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.category = category;
        this.price = price;
        this.isPrivate = isPrivate;
        this.imageUrl = imageUrl;
        this.time = time;
        this.slots = slots;
        this.description = description;
    }

    public int getId()          { return id; }
    public String getTitle()    { return title; }
    public String getLocation() { return location; }
    public String getDate()     { return date; }
    public String getCategory() { return category; }
    public String getPrice()    { return price; }
    public boolean isPrivate()  { return isPrivate; }
    public String getImageUrl() { return imageUrl; }
    public String getTime()     { return time; }
    public String getSlots()    { return slots; }
    public String getDescription() { return description; }
}

