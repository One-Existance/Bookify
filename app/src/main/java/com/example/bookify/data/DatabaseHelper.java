package com.example.bookify.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, "bookify.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fullName TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "phone TEXT NOT NULL)");

        db.execSQL("CREATE TABLE events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "location TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "time TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "price TEXT NOT NULL," +
                "slotsLeft INTEGER NOT NULL," +
                "description TEXT NOT NULL," +
                "isPrivate INTEGER NOT NULL DEFAULT 0," +
                "inviteCode TEXT," +
                "cardColorHex TEXT NOT NULL)");

        db.execSQL("CREATE TABLE bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER NOT NULL," +
                "eventId INTEGER NOT NULL," +
                "ticketNo TEXT NOT NULL," +
                "status TEXT NOT NULL DEFAULT 'upcoming')");

        seedEvents(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bookings");
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private void seedEvents(SQLiteDatabase db) {
        insertEvent(db, "Sauti Sol Live in DSM", "Mlimani City Ground", "Jun 20, 2025", "7:00 PM",
                "Concert", "Tsh 15,000", 48,
                "Join us for an unforgettable night with Sauti Sol performing live in Dar es Salaam.",
                0, null, "251F5C");

        insertEvent(db, "DSM Tech Conference 2025", "Julius Nyerere CC", "Jun 25, 2025", "9:00 AM",
                "Conference", "Tsh 30,000", 120,
                "Tanzania's premier technology conference bringing together innovators and developers.",
                0, null, "1A3D2B");

        insertEvent(db, "DIT Alumni Gala 2025", "DIT Main Hall", "Jul 5, 2025", "6:00 PM",
                "Gala", "Tsh 20,000", 60,
                "Annual DIT Alumni Gala celebrating excellence and achievements of our graduates.",
                1, "DIT2025", "3D1A5C");

        insertEvent(db, "Startup Mixer DSM", "Kariakoo Hub", "Jun 28, 2025", "5:00 PM",
                "Networking", "Tsh 10,000", 30,
                "Meet the brightest startup founders and investors in Dar es Salaam.",
                1, "STARTUP28", "1A3D2B");
    }

    private void insertEvent(SQLiteDatabase db, String title, String location, String date,
                              String time, String category, String price, int slots,
                              String description, int isPrivate, String inviteCode, String colorHex) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("location", location);
        cv.put("date", date);
        cv.put("time", time);
        cv.put("category", category);
        cv.put("price", price);
        cv.put("slotsLeft", slots);
        cv.put("description", description);
        cv.put("isPrivate", isPrivate);
        cv.put("inviteCode", inviteCode);
        cv.put("cardColorHex", colorHex);
        db.insert("events", null, cv);
    }

    public boolean registerUser(String fullName, String email, String password, String phone) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("fullName", fullName);
            cv.put("email", email);
            cv.put("password", password);
            cv.put("phone", phone);
            return getWritableDatabase().insert("users", null, cv) != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public User loginUser(String email, String password) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM users WHERE email=? AND password=?",
                new String[]{email, password});
        if (cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    public List<Event> getAllPublicEvents() {
        List<Event> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM events WHERE isPrivate=0", null);
        while (cursor.moveToNext()) list.add(cursorToEvent(cursor));
        cursor.close();
        return list;
    }

    private Event cursorToEvent(Cursor cursor) {
        return new Event(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("location")),
                cursor.getString(cursor.getColumnIndexOrThrow("date")),
                cursor.getString(cursor.getColumnIndexOrThrow("time")),
                cursor.getString(cursor.getColumnIndexOrThrow("category")),
                cursor.getString(cursor.getColumnIndexOrThrow("price")),
                cursor.getInt(cursor.getColumnIndexOrThrow("slotsLeft")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getInt(cursor.getColumnIndexOrThrow("isPrivate")) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow("inviteCode")),
                cursor.getString(cursor.getColumnIndexOrThrow("cardColorHex")));
    }
}
