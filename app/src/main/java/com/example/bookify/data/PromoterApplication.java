package com.example.bookify.data;

public class PromoterApplication {
    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private int id;
    private int userId;
    private String applicantName;
    private String applicantEmail;
    private String hallName;
    private String location;
    private String description;
    private String status;
    private Double latitude;
    private Double longitude;

    public PromoterApplication(int id, int userId, String applicantName, String applicantEmail,
                                String hallName, String location, String description, String status,
                                Double latitude, Double longitude) {
        this.id = id;
        this.userId = userId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.hallName = hallName;
        this.location = location;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId()               { return id; }
    public int getUserId()           { return userId; }
    public String getApplicantName() { return applicantName; }
    public String getApplicantEmail(){ return applicantEmail; }
    public String getHallName()      { return hallName; }
    public String getLocation()      { return location; }
    public String getDescription()   { return description; }
    public String getStatus()        { return status; }
    public Double getLatitude()      { return latitude; }
    public Double getLongitude()     { return longitude; }
    public boolean hasCoordinates()  { return latitude != null && longitude != null; }
}
