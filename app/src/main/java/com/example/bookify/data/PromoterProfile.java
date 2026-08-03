package com.example.bookify.data;

public class PromoterProfile {
    private int userId;
    private String firebaseUid;
    private String fullName;
    private String email;
    private String hallName;
    private String location;
    private String description;
    private Double latitude;
    private Double longitude;

    public PromoterProfile(int userId, String firebaseUid, String fullName, String email,
                            String hallName, String location, String description,
                            Double latitude, Double longitude) {
        this.userId = userId;
        this.firebaseUid = firebaseUid;
        this.fullName = fullName;
        this.email = email;
        this.hallName = hallName;
        this.location = location;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getUserId()         { return userId; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getFullName()    { return fullName; }
    public String getEmail()       { return email; }
    public String getHallName()    { return hallName; }
    public String getLocation()    { return location; }
    public String getDescription() { return description; }
    public Double getLatitude()    { return latitude; }
    public Double getLongitude()   { return longitude; }
    public boolean hasCoordinates(){ return latitude != null && longitude != null; }
}
