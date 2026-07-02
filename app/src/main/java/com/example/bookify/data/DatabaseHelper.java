package com.example.bookify.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "bookify_final.db";
    private static final int    DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users Table
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "phone TEXT," +
                "role INTEGER DEFAULT 0," + 
                "is_verified INTEGER DEFAULT 0," +
                "profile_image TEXT)");

        // Events Table
        db.execSQL("CREATE TABLE events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "location TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "price TEXT NOT NULL," +
                "is_private INTEGER DEFAULT 0," +
                "image_url TEXT," +
                "time TEXT," +
                "slots TEXT," +
                "description TEXT," +
                "promoter_id INTEGER DEFAULT 0)");

        // Bookings Table
        db.execSQL("CREATE TABLE bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "event_id INTEGER NOT NULL," +
                "ticket_number TEXT NOT NULL," +
                "status TEXT DEFAULT 'PENDING'," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(event_id) REFERENCES events(id))");

        seedEvents(db);
        seedAdmin(db);
    }

    private void seedAdmin(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("full_name", "Admin User");
        cv.put("email", "admin@gmail.com");
        cv.put("password", "123456");
        cv.put("phone", "0700000000");
        cv.put("role", 1);
        cv.put("is_verified", 1);
        db.insert("users", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bookings");
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private void seedEvents(SQLiteDatabase db) {
        insertEvent(db, "Sauti Sol Live in DSM",    "Mlimani City Ground",   "Jul 20, 2025", "Concert",    "Tsh 15,000", false);
        insertEvent(db, "DSM Tech Conference 2025", "Julius Nyerere CC",     "Jul 25, 2025", "Conference", "Tsh 30,000", false);
        insertEvent(db, "Kariakoo Marathon 2025",   "Kariakoo, Dar es Salaam","Aug 2, 2025", "Sports",     "Tsh 5,000",  false);
        insertEvent(db, "Private Rooftop Party",    "Masaki, Dar es Salaam", "Aug 10, 2025", "Concert",    "Tsh 50,000", true);
    }

    private void insertEvent(SQLiteDatabase db, String title, String location,
                             String date, String category, String price, boolean isPrivate) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("location", location);
        cv.put("date", date);
        cv.put("category", category);
        cv.put("price", price);
        cv.put("is_private", isPrivate ? 1 : 0);
        cv.put("image_url", ""); 
        db.insert("events", null, cv);
    }

    // ── User methods ──────────────────────────────────────────────────────────

    public long registerUser(String fullName, String email, String password, String phone) {
        return registerUserWithRole(fullName, email, password, phone, 0, 1);
    }

    public long registerUserWithRole(String fullName, String email, String password, String phone, int role, int verified) {
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        cv.put("email", email);
        cv.put("password", password);
        cv.put("phone", phone);
        cv.put("role", role);
        cv.put("is_verified", verified);
        return getWritableDatabase().insert("users", null, cv);
    }

    public User loginUser(String email, String password) {
        String query = "SELECT id, full_name, email, phone, role, is_verified FROM users WHERE email = ? AND password = ?";
        Cursor c = getReadableDatabase().rawQuery(query, new String[]{email, password});
        
        if (c != null && c.moveToFirst()) {
            User user = new User(
                c.getInt(0), 
                c.getString(1), 
                c.getString(2), 
                c.getString(3), 
                c.getInt(4), 
                c.getInt(5) == 1
            );
            c.close();
            return user;
        }
        if (c != null) c.close();
        return null;
    }

    public boolean emailExists(String email) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM users WHERE email=?", new String[]{email});
        boolean exists = c != null && c.moveToFirst();
        if (c != null) c.close();
        return exists;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT id, full_name, email, phone, role, is_verified FROM users", null);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(new User(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5) == 1));
            }
            c.close();
        }
        return list;
    }

    public void verifyPromoter(int userId) {
        ContentValues cv = new ContentValues();
        cv.put("is_verified", 1);
        getWritableDatabase().update("users", cv, "id=?", new String[]{String.valueOf(userId)});
    }

    public void updateProfileImage(int userId, String imageUrl) {
        ContentValues cv = new ContentValues();
        cv.put("profile_image", imageUrl);
        getWritableDatabase().update("users", cv, "id=?", new String[]{String.valueOf(userId)});
    }

    public String getProfileImage(int userId) {
        Cursor c = getReadableDatabase().rawQuery("SELECT profile_image FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        String img = null;
        if (c != null) {
            if (c.moveToFirst()) img = c.getString(0);
            c.close();
        }
        return img;
    }

    // ── Event methods ─────────────────────────────────────────────────────────

    public List<Event> getAllEvents() {
        return queryEvents("SELECT id, title, location, date, category, price, is_private, image_url, time, slots, description, promoter_id FROM events WHERE is_private=0", null);
    }

    public List<Event> getEventsByPromoter(int promoterId) {
        return queryEvents("SELECT id, title, location, date, category, price, is_private, image_url, time, slots, description, promoter_id FROM events WHERE promoter_id=?", new String[]{String.valueOf(promoterId)});
    }

    public List<Event> searchEvents(String query) {
        String like = "%" + query + "%";
        return queryEvents(
                "SELECT id, title, location, date, category, price, is_private, image_url, time, slots, description, promoter_id FROM events WHERE is_private=0 AND (title LIKE ? OR location LIKE ? OR category LIKE ?)",
                new String[]{like, like, like});
    }

    public long addEvent(String title, String location, String date, String category,
                         String price, boolean isPrivate, String imageUrl,
                         String time, String slots, String description) {
        return addEventWithPromoter(title, location, date, category, price, isPrivate, imageUrl, time, slots, description, 0);
    }

    public long addEventWithPromoter(String title, String location, String date, String category,
                         String price, boolean isPrivate, String imageUrl,
                         String time, String slots, String description, int promoterId) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("location", location);
        cv.put("date", date);
        cv.put("category", category);
        cv.put("price", price);
        cv.put("is_private", isPrivate ? 1 : 0);
        cv.put("image_url", imageUrl);
        cv.put("time", time);
        cv.put("slots", slots);
        cv.put("description", description);
        cv.put("promoter_id", promoterId);
        return getWritableDatabase().insert("events", null, cv);
    }

    public void deleteEvent(int eventId) {
        getWritableDatabase().delete("events", "id=?", new String[]{String.valueOf(eventId)});
    }

    private List<Event> queryEvents(String sql, String[] args) {
        List<Event> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(sql, args);
        if (c != null) {
            while (c.moveToNext()) {
                list.add(new Event(
                        c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5),
                        c.getInt(6) == 1, c.getString(7),
                        c.getString(8), c.getString(9), c.getString(10), c.getInt(11)));
            }
            c.close();
        }
        return list;
    }

    // ── Booking methods ───────────────────────────────────────────────────────

    public int getTicketCount(int eventId, boolean completedOnly) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE event_id=?";
        if (completedOnly) sql += " AND status='COMPLETED'";
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(eventId)});
        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
        }
        return count;
    }

    public double getRevenue(int eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT e.price FROM events e JOIN bookings b ON e.id = b.event_id WHERE e.id=? AND b.status='COMPLETED'",
                new String[]{String.valueOf(eventId)});
        double total = 0;
        if (c != null) {
            while (c.moveToNext()) {
                String priceStr = c.getString(0).replaceAll("[^0-9]", "");
                if (!priceStr.isEmpty()) {
                    total += Double.parseDouble(priceStr);
                }
            }
            c.close();
        }
        return total;
    }

    public boolean isAlreadyBooked(int userId, int eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM bookings WHERE user_id=? AND event_id=?",
                new String[]{String.valueOf(userId), String.valueOf(eventId)});
        boolean booked = c != null && c.moveToFirst();
        if (c != null) c.close();
        return booked;
    }

    public long bookEvent(int userId, int eventId) {
        Random rand = new Random();
        String ticket = "BKF-" + String.format("%04d", rand.nextInt(9000) + 1000)
                      + "-" + String.format("%04d", rand.nextInt(9000) + 1000);
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("event_id", eventId);
        cv.put("ticket_number", ticket);
        cv.put("status", "PENDING");
        return getWritableDatabase().insert("bookings", null, cv);
    }

    public void completePayment(int userId, int eventId) {
        ContentValues cv = new ContentValues();
        cv.put("status", "COMPLETED");
        getWritableDatabase().update("bookings", cv, "user_id=? AND event_id=?",
                new String[]{String.valueOf(userId), String.valueOf(eventId)});
    }

    public String getTicketNumber(int userId, int eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT ticket_number FROM bookings WHERE user_id=? AND event_id=? AND status='COMPLETED'",
                new String[]{String.valueOf(userId), String.valueOf(eventId)});
        String ticket = null;
        if (c != null) {
            if (c.moveToFirst()) ticket = c.getString(0);
            c.close();
        }
        return ticket;
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT b.ticket_number, e.title, e.date, e.category, e.price, e.image_url, b.status " +
                "FROM bookings b JOIN events e ON b.event_id = e.id WHERE b.user_id=? AND b.status='COMPLETED'",
                new String[]{String.valueOf(userId)});
        if (c != null) {
            while (c.moveToNext()) {
                list.add(new Booking(c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getString(6)));
            }
            c.close();
        }
        return list;
    }
}
