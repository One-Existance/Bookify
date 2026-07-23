package com.example.bookify.data;

public class PromoterProfile {
    private int userId;
    private String fullName;
    private String email;
    private String hallName;
    private String location;
    private String description;

    public PromoterProfile(int userId, String fullName, String email,
                            String hallName, String location, String description) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.hallName = hallName;
        this.location = location;
        this.description = description;
    }

    public int getUserId()         { return userId; }
    public String getFullName()    { return fullName; }
    public String getEmail()       { return email; }
    public String getHallName()    { return hallName; }
    public String getLocation()    { return location; }
    public String getDescription() { return description; }
}
