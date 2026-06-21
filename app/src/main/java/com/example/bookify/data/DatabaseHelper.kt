package com.example.bookify.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "bookify.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fullName TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                phone TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                location TEXT NOT NULL,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                category TEXT NOT NULL,
                price TEXT NOT NULL,
                slotsLeft INTEGER NOT NULL,
                description TEXT NOT NULL,
                isPrivate INTEGER NOT NULL DEFAULT 0,
                inviteCode TEXT,
                cardColorHex TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE bookings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                eventId INTEGER NOT NULL,
                ticketNo TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'upcoming'
            )
        """)
        seedEvents(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS bookings")
        db.execSQL("DROP TABLE IF EXISTS events")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    private fun seedEvents(db: SQLiteDatabase) {
        val events = listOf(
            ContentValues().apply {
                put("title", "Sauti Sol Live in DSM")
                put("location", "Mlimani City Ground")
                put("date", "Jun 20, 2025")
                put("time", "7:00 PM")
                put("category", "Concert")
                put("price", "Tsh 15,000")
                put("slotsLeft", 48)
                put("description", "Join us for an unforgettable night with Sauti Sol performing their greatest hits live in Dar es Salaam.")
                put("isPrivate", 0)
                put("cardColorHex", "251F5C")
            },
            ContentValues().apply {
                put("title", "DSM Tech Conference 2025")
                put("location", "Julius Nyerere CC")
                put("date", "Jun 25, 2025")
                put("time", "9:00 AM")
                put("category", "Conference")
                put("price", "Tsh 30,000")
                put("slotsLeft", 120)
                put("description", "Tanzania's premier technology conference bringing together innovators, developers, and entrepreneurs.")
                put("isPrivate", 0)
                put("cardColorHex", "1A3D2B")
            },
            ContentValues().apply {
                put("title", "DIT Alumni Gala 2025")
                put("location", "DIT Main Hall")
                put("date", "Jul 5, 2025")
                put("time", "6:00 PM")
                put("category", "Gala")
                put("price", "Tsh 20,000")
                put("slotsLeft", 60)
                put("description", "Annual DIT Alumni Gala celebrating excellence and achievements of our graduates.")
                put("isPrivate", 1)
                put("inviteCode", "DIT2025")
                put("cardColorHex", "3D1A5C")
            },
            ContentValues().apply {
                put("title", "Startup Mixer DSM")
                put("location", "Kariakoo Hub")
                put("date", "Jun 28, 2025")
                put("time", "5:00 PM")
                put("category", "Networking")
                put("price", "Tsh 10,000")
                put("slotsLeft", 30)
                put("description", "Meet the brightest startup founders and investors in Dar es Salaam at this exclusive mixer.")
                put("isPrivate", 1)
                put("inviteCode", "STARTUP28")
                put("cardColorHex", "1A3D2B")
            }
        )
        events.forEach { db.insert("events", null, it) }
    }

    fun registerUser(fullName: String, email: String, password: String, phone: String): Boolean {
        return try {
            val cv = ContentValues().apply {
                put("fullName", fullName)
                put("email", email)
                put("password", password)
                put("phone", phone)
            }
            writableDatabase.insert("users", null, cv) != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun loginUser(email: String, password: String): User? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM users WHERE email=? AND password=?", arrayOf(email, password)
        )
        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun getAllPublicEvents(): List<Event> {
        val list = mutableListOf<Event>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM events WHERE isPrivate=0", null
        )
        while (cursor.moveToNext()) list.add(cursorToEvent(cursor))
        cursor.close()
        return list
    }

    fun getEventById(id: Int): Event? {
        val cursor = readableDatabase.rawQuery("SELECT * FROM events WHERE id=?", arrayOf(id.toString()))
        val event = if (cursor.moveToFirst()) cursorToEvent(cursor) else null
        cursor.close()
        return event
    }

    fun getEventByInviteCode(code: String): Event? {
        val cursor = readableDatabase.rawQuery("SELECT * FROM events WHERE inviteCode=?", arrayOf(code))
        val event = if (cursor.moveToFirst()) cursorToEvent(cursor) else null
        cursor.close()
        return event
    }

    fun bookEvent(userId: Int, eventId: Int): String? {
        val ticketNo = "BKF-${System.currentTimeMillis() % 10000000}".let {
            "BKF-2025-${(1000..9999).random()}"
        }
        val cv = ContentValues().apply {
            put("userId", userId)
            put("eventId", eventId)
            put("ticketNo", ticketNo)
            put("status", "upcoming")
        }
        val id = writableDatabase.insert("bookings", null, cv)
        if (id != -1L) {
            writableDatabase.execSQL("UPDATE events SET slotsLeft = slotsLeft - 1 WHERE id=?", arrayOf(eventId))
        }
        return if (id != -1L) ticketNo else null
    }

    fun getUserBookings(userId: Int): List<Booking> {
        val list = mutableListOf<Booking>()
        val cursor = readableDatabase.rawQuery("""
            SELECT b.id, b.ticketNo, b.status, e.title, e.date, e.time, e.category, e.cardColorHex
            FROM bookings b JOIN events e ON b.eventId = e.id
            WHERE b.userId=?
            ORDER BY b.id DESC
        """, arrayOf(userId.toString()))
        while (cursor.moveToNext()) {
            list.add(Booking(
                id = cursor.getInt(0),
                ticketNo = cursor.getString(1),
                status = cursor.getString(2),
                eventTitle = cursor.getString(3),
                eventDate = cursor.getString(4),
                eventTime = cursor.getString(5),
                eventCategory = cursor.getString(6),
                cardColorHex = cursor.getString(7)
            ))
        }
        cursor.close()
        return list
    }

    fun isAlreadyBooked(userId: Int, eventId: Int): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM bookings WHERE userId=? AND eventId=?",
            arrayOf(userId.toString(), eventId.toString())
        )
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    private fun cursorToEvent(cursor: android.database.Cursor) = Event(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
        location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
        date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
        time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
        price = cursor.getString(cursor.getColumnIndexOrThrow("price")),
        slotsLeft = cursor.getInt(cursor.getColumnIndexOrThrow("slotsLeft")),
        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
        isPrivate = cursor.getInt(cursor.getColumnIndexOrThrow("isPrivate")) == 1,
        inviteCode = cursor.getString(cursor.getColumnIndexOrThrow("inviteCode")),
        cardColorHex = cursor.getString(cursor.getColumnIndexOrThrow("cardColorHex"))
    )
}
