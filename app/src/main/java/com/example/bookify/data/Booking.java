package com.example.bookify.data;

public class Booking {
    public int id;
    public String ticketNo;
    public String status;
    public String eventTitle;
    public String eventDate;
    public String eventTime;
    public String eventCategory;
    public String cardColorHex;

    public Booking(int id, String ticketNo, String status, String eventTitle,
                   String eventDate, String eventTime, String eventCategory, String cardColorHex) {
        this.id = id;
        this.ticketNo = ticketNo;
        this.status = status;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventCategory = eventCategory;
        this.cardColorHex = cardColorHex;
    }
}
