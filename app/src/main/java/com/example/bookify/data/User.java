package com.example.bookify.data;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private int role; // 0: User, 1: Admin, 2: Promoter
    private boolean isVerified; // For promoters

    public User(int id, String fullName, String email, String phone, int role, boolean isVerified) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isVerified = isVerified;
    }

    public int getId()         { return id; }
    public String getFullName(){ return fullName; }
    public String getEmail()   { return email; }
    public String getPhone()   { return phone; }
    public int getRole()       { return role; }
    public boolean isAdmin()   { return role == 1; }
    public boolean isPromoter(){ return role == 2; }
    public boolean isVerified(){ return isVerified; }
}