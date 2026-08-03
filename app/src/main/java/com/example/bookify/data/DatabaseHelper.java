package com.example.bookify.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Local-only data: users, bookings/tickets, and promoter applications. Events themselves live in
 * Cloud Firestore (see EventsRepository) so they sync across devices — this class no longer
 * stores or queries events directly. Bookings carry a denormalized snapshot of the event fields
 * they need (title/date/category/price/image) so ticket/check-in/revenue lookups stay local and
 * synchronous without joining across the Firestore/SQLite boundary.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "bookify.db";
    private static final int    DB_VERSION = 11;

    // Local-only test account: bypasses Firebase Auth entirely (see LoginActivity).
    public static final String TEST_USER_EMAIL = "normal@gmail.com";
    // Sentinel firebase_uid for the local test account, so it can still own/organize events
    // (Firestore's organizerId needs some stable identifier, and this account never authenticates
    // with Firebase to get a real one). Single-device only, same as the account always was.
    public static final String TEST_USER_UID = "LOCAL_TEST_USER_UID";

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

        db.execSQL("CREATE TABLE bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "event_id TEXT NOT NULL," +
                "ticket_number TEXT NOT NULL," +
                "status TEXT DEFAULT 'PENDING'," +
                "checked_in INTEGER DEFAULT 0," +
                "event_title TEXT," +
                "event_date TEXT," +
                "event_category TEXT," +
                "event_price TEXT," +
                "event_image_url TEXT," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE promoter_applications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "hall_name TEXT NOT NULL," +
                "location TEXT NOT NULL," +
                "description TEXT," +
                "status TEXT DEFAULT 'PENDING'," +
                "latitude REAL," +
                "longitude REAL," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

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
        cv.put("firebase_uid", TEST_USER_UID);
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
                "SELECT id, firebase_uid, full_name, email, phone, role FROM users WHERE firebase_uid=?",
                new String[]{uid});
        User user = readUser(c);
        c.close();
        return user;
    }

    public User getUserByEmail(String email) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, firebase_uid, full_name, email, phone, role FROM users WHERE email=?",
                new String[]{email});
        User user = readUser(c);
        c.close();
        return user;
    }

    public User getUserById(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, firebase_uid, full_name, email, phone, role FROM users WHERE id=?",
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
            return new User(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5));
        }
        return null;
    }

    // ── Promoter application methods ────────────────────────────────────────────

    public long submitPromoterApplication(int userId, String hallName, String location, String description,
                                           Double latitude, Double longitude) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("hall_name", hallName);
        cv.put("location", location);
        cv.put("description", description);
        cv.put("status", PromoterApplication.STATUS_PENDING);
        if (latitude != null && longitude != null) {
            cv.put("latitude", latitude);
            cv.put("longitude", longitude);
        }
        return getWritableDatabase().insert("promoter_applications", null, cv);
    }

    public PromoterApplication getLatestPromoterApplication(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT pa.id, pa.user_id, u.full_name, u.email, pa.hall_name, pa.location, pa.description, pa.status, pa.latitude, pa.longitude " +
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
                "SELECT pa.id, pa.user_id, u.full_name, u.email, pa.hall_name, pa.location, pa.description, pa.status, pa.latitude, pa.longitude " +
                "FROM promoter_applications pa JOIN users u ON pa.user_id = u.id " +
                "WHERE pa.status=?",
                new String[]{PromoterApplication.STATUS_PENDING});
        while (c.moveToNext()) list.add(readPromoterApplication(c));
        c.close();
        return list;
    }

    private PromoterApplication readPromoterApplication(Cursor c) {
        return new PromoterApplication(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3),
                c.getString(4), c.getString(5), c.getString(6), c.getString(7),
                c.isNull(8) ? null : c.getDouble(8), c.isNull(9) ? null : c.getDouble(9));
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
                "SELECT u.id, u.firebase_uid, u.full_name, u.email, pa.hall_name, pa.location, pa.description, pa.latitude, pa.longitude " +
                "FROM promoter_applications pa JOIN users u ON pa.user_id = u.id " +
                "WHERE pa.status=?",
                new String[]{PromoterApplication.STATUS_APPROVED});
        while (c.moveToNext()) {
            list.add(new PromoterProfile(c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5), c.getString(6),
                    c.isNull(7) ? null : c.getDouble(7), c.isNull(8) ? null : c.getDouble(8)));
        }
        c.close();
        return list;
    }

    public PromoterProfile getPromoterProfile(int promoterUserId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT u.id, u.firebase_uid, u.full_name, u.email, pa.hall_name, pa.location, pa.description, pa.latitude, pa.longitude " +
                "FROM promoter_applications pa JOIN users u ON pa.user_id = u.id " +
                "WHERE pa.status=? AND u.id=? ORDER BY pa.id DESC LIMIT 1",
                new String[]{PromoterApplication.STATUS_APPROVED, String.valueOf(promoterUserId)});
        PromoterProfile profile = null;
        if (c.moveToFirst()) {
            profile = new PromoterProfile(c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5), c.getString(6),
                    c.isNull(7) ? null : c.getDouble(7), c.isNull(8) ? null : c.getDouble(8));
        }
        c.close();
        return profile;
    }

    // ── Booking methods ───────────────────────────────────────────────────────
    // eventId is now the Firestore document id (String). Event display fields are
    // denormalized onto each booking row at booking time so these stay local/synchronous.

    public int getTicketCount(String eventId, boolean completedOnly) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE event_id=?";
        if (completedOnly) sql += " AND status='COMPLETED'";
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{eventId});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public boolean isAlreadyBooked(int userId, String eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM bookings WHERE user_id=? AND event_id=?",
                new String[]{String.valueOf(userId), eventId});
        boolean booked = c.moveToFirst();
        c.close();
        return booked;
    }

    public long bookEvent(int userId, String eventId, String eventTitle, String eventDate,
                           String eventCategory, String eventPrice, String eventImageUrl) {
        Random rand = new Random();
        String ticket = "BKF-" + String.format("%04d", rand.nextInt(9000) + 1000)
                      + "-" + String.format("%04d", rand.nextInt(9000) + 1000);
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("event_id", eventId);
        cv.put("ticket_number", ticket);
        cv.put("status", "PENDING");
        cv.put("event_title", eventTitle);
        cv.put("event_date", eventDate);
        cv.put("event_category", eventCategory);
        cv.put("event_price", eventPrice);
        cv.put("event_image_url", eventImageUrl);
        return getWritableDatabase().insert("bookings", null, cv);
    }

    public void completePayment(int userId, String eventId) {
        ContentValues cv = new ContentValues();
        cv.put("status", "COMPLETED");
        getWritableDatabase().update("bookings", cv, "user_id=? AND event_id=?",
                new String[]{String.valueOf(userId), eventId});
    }

    public String getTicketNumber(int userId, String eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT ticket_number FROM bookings WHERE user_id=? AND event_id=? AND status='COMPLETED'",
                new String[]{String.valueOf(userId), eventId});
        String ticket = null;
        if (c.moveToFirst()) ticket = c.getString(0);
        c.close();
        return ticket;
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT ticket_number, event_title, event_date, event_category, event_price, event_image_url, status " +
                "FROM bookings WHERE user_id=? AND status='COMPLETED'",
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            list.add(new Booking(c.getString(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5), c.getString(6)));
        }
        c.close();
        return list;
    }

    // ── Admin methods ────────────────────────────────────────────────────────

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, firebase_uid, full_name, email, phone, role FROM users ORDER BY id DESC", null);
        while (c.moveToNext()) {
            list.add(new User(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    public double getRevenue(String eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT event_price FROM bookings WHERE event_id=? AND status='COMPLETED'",
                new String[]{eventId});
        double total = 0;
        while (c.moveToNext()) {
            total += parsePrice(c.getString(0));
        }
        c.close();
        return total;
    }

    private double parsePrice(String price) {
        if (price == null) return 0;
        String digits = price.replaceAll("[^0-9.]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Admin-created promoters skip the application/approval flow, so the account
    // is inserted with the promoter role already set. The Firebase account is
    // created by the caller (AdminActivity) since FirebaseAuth calls are async.
    public long registerUserWithRole(String fullName, String email, String firebaseUid, String phone, String role) {
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        cv.put("email", email);
        cv.put("firebase_uid", firebaseUid);
        cv.put("phone", phone);
        cv.put("role", role);
        return getWritableDatabase().insert("users", null, cv);
    }

    public void verifyPromoter(int userId) {
        ContentValues cv = new ContentValues();
        cv.put("role", User.ROLE_PROMOTER);
        getWritableDatabase().update("users", cv, "id=?", new String[]{String.valueOf(userId)});
    }

    /**
     * Validates a scanned ticket QR against a specific event and, on first scan,
     * marks it checked in. Reusing the same ticket_number a second time (or scanning
     * it at the wrong event) is rejected instead of silently succeeding again.
     */
    public CheckInResult checkInTicket(String ticketNumber, String eventId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT b.status, b.checked_in, b.event_id, u.full_name, b.event_title " +
                        "FROM bookings b " +
                        "JOIN users u ON b.user_id = u.id " +
                        "WHERE b.ticket_number=?",
                new String[]{ticketNumber});

        if (!c.moveToFirst()) {
            c.close();
            return new CheckInResult(CheckInResult.Status.INVALID_TICKET, null, null);
        }

        String status = c.getString(0);
        boolean checkedIn = c.getInt(1) != 0;
        String bookingEventId = c.getString(2);
        String attendeeName = c.getString(3);
        String eventTitle = c.getString(4);
        c.close();

        if (!bookingEventId.equals(eventId)) {
            return new CheckInResult(CheckInResult.Status.WRONG_EVENT, attendeeName, eventTitle);
        }
        if (!"COMPLETED".equals(status)) {
            return new CheckInResult(CheckInResult.Status.UNPAID, attendeeName, eventTitle);
        }
        if (checkedIn) {
            return new CheckInResult(CheckInResult.Status.ALREADY_CHECKED_IN, attendeeName, eventTitle);
        }

        ContentValues cv = new ContentValues();
        cv.put("checked_in", 1);
        getWritableDatabase().update("bookings", cv, "ticket_number=?", new String[]{ticketNumber});
        return new CheckInResult(CheckInResult.Status.CHECKED_IN, attendeeName, eventTitle);
    }

    public boolean updateUserName(int userId, String fullName) {
        ContentValues cv = new ContentValues();
        cv.put("full_name", fullName);
        int rows = getWritableDatabase().update("users", cv, "id=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }
}
