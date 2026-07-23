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

    private static final String DB_NAME    = "bookify.db";
    private static final int    DB_VERSION = 8;

    // Local-only test account: bypasses Firebase Auth entirely (see LoginActivity).
    public static final String TEST_USER_EMAIL = "normal@gmail.com";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "firebase_uid TEXT UNIQUE," +
                "phone TEXT," +
                "role TEXT NOT NULL DEFAULT 'USER')");

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
                "organizer_id INTEGER," +
                "promoter_id INTEGER," +
                "status TEXT DEFAULT 'PUBLISHED'," +
                "access_code TEXT," +
                "FOREIGN KEY(organizer_id) REFERENCES users(id)," +
                "FOREIGN KEY(promoter_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "event_id INTEGER NOT NULL," +
                "ticket_number TEXT NOT NULL," +
                "status TEXT DEFAULT 'PENDING'," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(event_id) REFERENCES events(id))");

        db.execSQL("CREATE TABLE promoter_applications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "hall_name TEXT NOT NULL," +
                "location TEXT NOT NULL," +
                "description TEXT," +
                "status TEXT DEFAULT 'PENDING'," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        seedEvents(db);
        seedAdmin(db);
        seedTestUser(db);
    }

    private void seedAdmin(SQLiteDatabase db) {
        // firebase_uid is left null; linked to the Firebase Auth account on first login
        // (create admin@gmail.com / 123456 in the Firebase console under Authentication > Users)
        ContentValues cv = new ContentValues();
        cv.put("full_name", "Admin User");
        cv.put("email", "admin@gmail.com");
        cv.put("phone", "0700000000");
        cv.put("role", User.ROLE_ADMIN);
        db.insert("users", null, cv);
    }

    private void seedTestUser(SQLiteDatabase db) {
        // Local-only account, never touches Firebase — LoginActivity special-cases this
        // email and logs straight in against this row, so login always works offline.
        ContentValues cv = new ContentValues();
        cv.put("full_name", "Normal User");
        cv.put("email", TEST_USER_EMAIL);
        cv.put("phone", "0695880700");
        cv.put("role", User.ROLE_USER);
        db.insert("users", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS promoter_applications");
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
        cv.put("image_url", ""); // Default empty
        cv.put("status", Event.STATUS_PUBLISHED);
        db.insert("events", null, cv);
    }

    // ── User methods ──────────────────────────────────────────────────────────
    // Credentials (email/password) are verified by Firebase Auth. This table only
    // holds the local profile (name, phone, role) linked by firebase_uid.

    public long registerLocalProfile(String fullName, String email, String firebaseUid, String phone) {
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        cv.put("email", email);
        cv.put("firebase_uid", firebaseUid);
        cv.put("phone", phone);
        cv.put("role", User.ROLE_USER);
        return getWritableDatabase().insert("users", null, cv);
    }

    public User getUserByFirebaseUid(String uid) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, full_name, email, phone, role FROM users WHERE firebase_uid=?",
                new String[]{uid});
        User user = readUser(c);
        c.close();
        return user;
    }

    public User getUserByEmail(String email) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, full_name, email, phone, role FROM users WHERE email=?",
                new String[]{email});
        User user = readUser(c);
        c.close();
        return user;
    }

    public User getUserById(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, full_name, email, phone, role FROM users WHERE id=?",
                new String[]{String.valueOf(userId)});
        User user = readUser(c);
        c.close();
        return user;
    }

    public void linkFirebaseUid(int localId, String uid) {
        ContentValues cv = new ContentValues();
        cv.put("firebase_uid", uid);
        getWritableDatabase().update("users", cv, "id=?", new String[]{String.valueOf(localId)});
    }

    private User readUser(Cursor c) {
        if (c.moveToFirst()) {
            return new User(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
        }
        return null;
    }

    // ── Promoter application methods ────────────────────────────────────────────

    public long submitPromoterApplication(int userId, String hallName, String location, String description) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("hall_name", hallName);
        cv.put("location", location);
        cv.put("description", description);
        cv.put("status", PromoterApplication.STATUS_PENDING);
        return getWritableDatabase().insert("promoter_applications", null, cv);
    }

    public PromoterApplication getLatestPromoterApplication(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT pa.id, pa.user_id, u.full_name, u.email, pa.hall_name, pa.location, pa.description, pa.status " +
                "FROM promoter_applications pa JOIN users u ON pa.user_id = u.id " +
                "WHERE pa.user_id=? ORDER BY pa.id DESC LIMIT 1",
                new String[]{String.valueOf(userId)});
        PromoterApplication app = null;
        if (c.moveToFirst()) app = readPromoterApplication(c);
        c.close();
        return app;
    }

    public List<PromoterApplication> getPendingPromoterApplications() {
        List<PromoterApplication> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT pa.id, pa.user_id, u.full_name, u.email, pa.hall_name, pa.location, pa.description, pa.status " +
                "FROM promoter_applications pa JOIN users u ON pa.user_id = u.id " +
                "WHERE pa.status=?",
                new String[]{PromoterApplication.STATUS_PENDING});
        while (c.moveToNext()) list.add(readPromoterApplication(c));
        c.close();
        return list;
    }

    private PromoterApplication readPromoterApplication(Cursor c) {
        return new PromoterApplication(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3),
                c.getString(4), c.getString(5), c.getString(6), c.getString(7));
    }

    public void approvePromoterApplication(int applicationId, int userId) {
        ContentValues appCv = new ContentValues();
        appCv.put("status", PromoterApplication.STATUS_APPROVED);
        getWritableDatabase().update("promoter_applications", appCv, "id=?",
                new String[]{String.valueOf(applicationId)});

        ContentValues userCv = new ContentValues();
        userCv.put("role", User.ROLE_PROMOTER);
        getWritableDatabase().update("users", userCv, "id=?", new String[]{String.valueOf(userId)});
    }

    public void rejectPromoterApplication(int applicationId) {
        ContentValues cv = new ContentValues();
        cv.put("status", PromoterApplication.STATUS_REJECTED);
        getWritableDatabase().update("promoter_applications", cv, "id=?",
                new String[]{String.valueOf(applicationId)});
    }

    public List<PromoterProfile> getApprovedPromoters() {
        List<PromoterProfile> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT u.id, u.full_name, u.email, pa.hall_name, pa.location, pa.description " +
                "FROM promoter_applications pa JOIN users u ON pa.user_id = u.id " +
                "WHERE pa.status=?",
                new String[]{PromoterApplication.STATUS_APPROVED});
        while (c.moveToNext()) {
            list.add(new PromoterProfile(c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    public PromoterProfile getPromoterProfile(int promoterUserId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT u.id, u.full_name, u.email, pa.hall_name, pa.location, pa.description " +
                "FROM promoter_applications pa JOIN users u ON pa.user_id = u.id " +
                "WHERE pa.status=? AND u.id=? ORDER BY pa.id DESC LIMIT 1",
                new String[]{PromoterApplication.STATUS_APPROVED, String.valueOf(promoterUserId)});
        PromoterProfile profile = null;
        if (c.moveToFirst()) {
            profile = new PromoterProfile(c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5));
        }
        c.close();
        return profile;
    }

    // ── Event methods ─────────────────────────────────────────────────────────

    private static final String EVENT_COLUMNS =
            "id, title, location, date, category, price, is_private, image_url, time, slots, description, " +
            "organizer_id, promoter_id, status, access_code";

    public List<Event> getAllEvents() {
        return queryEvents("SELECT " + EVENT_COLUMNS + " FROM events WHERE is_private=0 AND status=?",
                new String[]{Event.STATUS_PUBLISHED});
    }

    public List<Event> searchEvents(String query) {
        String like = "%" + query + "%";
        return queryEvents(
                "SELECT " + EVENT_COLUMNS + " FROM events WHERE is_private=0 AND status=? AND (title LIKE ? OR location LIKE ? OR category LIKE ?)",
                new String[]{Event.STATUS_PUBLISHED, like, like, like});
    }

    public List<Event> getAllEventsForAdmin() {
        return queryEvents("SELECT " + EVENT_COLUMNS + " FROM events ORDER BY id DESC", null);
    }

    public long addEvent(String title, String location, String date, String category,
                         String price, boolean isPrivate, String imageUrl,
                         String time, String slots, String description) {
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
        cv.put("status", Event.STATUS_PUBLISHED);
        return getWritableDatabase().insert("events", null, cv);
    }

    public long requestEvent(String title, String location, String date, String category,
                              String price, boolean isPrivate, String imageUrl,
                              String time, String slots, String description,
                              int organizerId, int promoterId) {
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
        cv.put("organizer_id", organizerId);
        cv.put("promoter_id", promoterId);
        cv.put("status", Event.STATUS_PENDING);
        return getWritableDatabase().insert("events", null, cv);
    }

    public List<EventRequest> getPendingEventRequestsForPromoter(int promoterId) {
        List<EventRequest> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + prefixed("e", EVENT_COLUMNS) + ", u.full_name " +
                "FROM events e JOIN users u ON e.organizer_id = u.id " +
                "WHERE e.promoter_id=? AND e.status=? ORDER BY e.id DESC",
                new String[]{String.valueOf(promoterId), Event.STATUS_PENDING});
        while (c.moveToNext()) {
            Event event = readEvent(c);
            String organizerName = c.getString(15);
            list.add(new EventRequest(event, organizerName));
        }
        c.close();
        return list;
    }

    public void approveEventRequest(int eventId, String finalPrice, String finalDate, String finalTime, boolean isPrivate) {
        ContentValues cv = new ContentValues();
        cv.put("price", finalPrice);
        cv.put("date", finalDate);
        cv.put("time", finalTime);
        cv.put("status", Event.STATUS_PUBLISHED);
        if (isPrivate) {
            cv.put("access_code", generateAccessCode());
        }
        getWritableDatabase().update("events", cv, "id=?", new String[]{String.valueOf(eventId)});
    }

    public void rejectEventRequest(int eventId) {
        ContentValues cv = new ContentValues();
        cv.put("status", Event.STATUS_REJECTED);
        getWritableDatabase().update("events", cv, "id=?", new String[]{String.valueOf(eventId)});
    }

    public List<Event> getEventsByOrganizer(int organizerId) {
        return queryEvents("SELECT " + EVENT_COLUMNS + " FROM events WHERE organizer_id=? ORDER BY id DESC",
                new String[]{String.valueOf(organizerId)});
    }

    public Event getEventByAccessCode(String code) {
        List<Event> events = queryEvents(
                "SELECT " + EVENT_COLUMNS + " FROM events WHERE access_code=? AND status=? AND is_private=1",
                new String[]{code, Event.STATUS_PUBLISHED});
        return events.isEmpty() ? null : events.get(0);
    }

    private String generateAccessCode() {
        Random rand = new Random();
        return "PRIV-" + String.format("%04d", rand.nextInt(9000) + 1000)
                + "-" + String.format("%04d", rand.nextInt(9000) + 1000);
    }

    public void deleteEvent(int eventId) {
        getWritableDatabase().delete("events", "id=?", new String[]{String.valueOf(eventId)});
    }

    private String prefixed(String alias, String columns) {
        StringBuilder sb = new StringBuilder();
        String[] cols = columns.split(", ");
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(alias).append(".").append(cols[i]);
        }
        return sb.toString();
    }

    private List<Event> queryEvents(String sql, String[] args) {
        List<Event> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(sql, args);
        while (c.moveToNext()) {
            list.add(readEvent(c));
        }
        c.close();
        return list;
    }

    private Event readEvent(Cursor c) {
        return new Event(
                c.getInt(0), c.getString(1), c.getString(2),
                c.getString(3), c.getString(4), c.getString(5),
                c.getInt(6) == 1, c.getString(7),
                c.getString(8), c.getString(9), c.getString(10),
                c.isNull(11) ? -1 : c.getInt(11),
                c.isNull(12) ? -1 : c.getInt(12),
                c.getString(13), c.getString(14));
    }

    // ── Booking methods ───────────────────────────────────────────────────────

    public int getTicketCount(int eventId, boolean completedOnly) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE event_id=?";
        if (completedOnly) sql += " AND status='COMPLETED'";
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(eventId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public boolean isAlreadyBooked(int userId, int eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM bookings WHERE user_id=? AND event_id=?",
                new String[]{String.valueOf(userId), String.valueOf(eventId)});
        boolean booked = c.moveToFirst();
        c.close();
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
        if (c.moveToFirst()) ticket = c.getString(0);
        c.close();
        return ticket;
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT b.ticket_number, e.title, e.date, e.category, e.price, e.image_url, b.status " +
                "FROM bookings b JOIN events e ON b.event_id = e.id WHERE b.user_id=? AND b.status='COMPLETED'",
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            list.add(new Booking(c.getString(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5), c.getString(6)));
        }
        c.close();
        return list;
    }
}
