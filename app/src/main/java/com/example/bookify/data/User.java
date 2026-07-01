package com.example.bookify.data;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String phone;

    public User(int id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public int getId()         { return id; }
    public String getFullName(){ return fullName; }
    public String getEmail()   { return email; }
    public String getPhone()   { return phone; }
}