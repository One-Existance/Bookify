package com.example.bookify.data

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val phone: String
)

data class Event(
    val id: Int,
    val title: String,
    val location: String,
    val date: String,
    val time: String,
    val category: String,
    val price: String,
    val slotsLeft: Int,
    val description: String,
    val isPrivate: Boolean,
    val inviteCode: String?,
    val cardColorHex: String
)

data class Booking(
    val id: Int,
    val ticketNo: String,
    val status: String,
    val eventTitle: String,
    val eventDate: String,
    val eventTime: String,
    val eventCategory: String,
    val cardColorHex: String
)
