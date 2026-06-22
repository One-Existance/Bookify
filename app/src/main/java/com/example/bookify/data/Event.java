package com.example.bookify.data;

public class Event {
    public int id;
    public String title;
    public String location;
    public String date;
    public String time;
    public String category;
    public String price;
    public int slotsLeft;
    public String description;
    public boolean isPrivate;
    public String inviteCode;
    public String cardColorHex;

    public Event(int id, String title, String location, String date, String time,
                 String category, String price, int slotsLeft, String description,
                 boolean isPrivate, String inviteCode, String cardColorHex) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.time = time;
        this.category = category;
        this.price = price;
        this.slotsLeft = slotsLeft;
        this.description = description;
        this.isPrivate = isPrivate;
        this.inviteCode = inviteCode;
        this.cardColorHex = cardColorHex;
    }
}
