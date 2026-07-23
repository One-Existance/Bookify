package com.example.bookify.data;

public class User {
    public static final String ROLE_USER     = "USER";
    public static final String ROLE_PROMOTER = "PROMOTER";
    public static final String ROLE_ADMIN    = "ADMIN";

    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String role;

    public User(int id, String fullName, String email, String phone, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public int getId()          { return id; }
    public String getFullName() { return fullName; }
    public String getEmail()    { return email; }
    public String getPhone()    { return phone; }
    public String getRole()     { return role; }

    public boolean isAdmin()    { return ROLE_ADMIN.equals(role); }
    public boolean isPromoter() { return ROLE_PROMOTER.equals(role); }
    public boolean isUser()     { return ROLE_USER.equals(role); }
}
