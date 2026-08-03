package com.example.bookify.data;

public class Event {
    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_REJECTED  = "REJECTED";

    private String id;
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
    private String organizerId;
    private String organizerName;
    private String promoterId;
    private String promoterName;
    private String status;
    private String accessCode;
    private double latitude;
    private double longitude;

    public Event(String id, String title, String location, String date,
                 String category, String price, boolean isPrivate, String imageUrl,
                 String time, String slots, String description,
                 String organizerId, String organizerName, String promoterId, String promoterName,
                 String status, String accessCode,
                 double latitude, double longitude) {
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
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.promoterId = promoterId;
        this.promoterName = promoterName;
        this.status = status;
        this.accessCode = accessCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId()          { return id; }
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
    public String getOrganizerId() { return organizerId; }
    public String getOrganizerName() { return organizerName; }
    public String getPromoterId()  { return promoterId; }
    public String getPromoterName() { return promoterName; }
    public String getStatus()   { return status; }
    public String getAccessCode() { return accessCode; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
