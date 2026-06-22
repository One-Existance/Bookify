package com.example.bookify.data;

public class User {
    public int id;
    public String fullName;
    public String email;
    public String phone;

    public User(int id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
}
