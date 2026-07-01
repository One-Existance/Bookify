package com.example.bookify.data;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private boolean isAdmin;

    public User(int id, String fullName, String email, String phone, boolean isAdmin) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin;
    }

    public int getId()         { return id; }
    public String getFullName(){ return fullName; }
    public String getEmail()   { return email; }
    public String getPhone()   { return phone; }
    public boolean isAdmin()   { return isAdmin; }
}