package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;

public class EventDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = new DatabaseHelper(this);

        eventId = getIntent().getIntExtra("event_id", -1);
        String title    = getIntent().getStringExtra("event_title");
        String location = getIntent().getStringExtra("event_location");
        String date     = getIntent().getStringExtra("event_date");
        String price    = getIntent().getStringExtra("event_price");

        if (title    != null) ((TextView) findViewById(R.id.tv_event_title)).setText(title);
        if (price    != null) ((TextView) findViewById(R.id.tv_price)).setText(price);
        if (date     != null) ((TextView) findViewById(R.id.tv_date)).setText(date);
        if (location != null && date != null)
            ((TextView) findViewById(R.id.tv_event_location_date)).setText(location + " · " + date);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_book).setOnClickListener(v -> bookTicket());
    }

    private void bookTicket() {
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1 || eventId == -1) {
            showDialog("Not logged in", "Please log in to book tickets.", null);
            return;
        }

        if (db.isAlreadyBooked(userId, eventId)) {
            String existing = db.getTicketNumber(userId, eventId);
            showDialog("Already booked", "Your ticket number:\n" + existing, existing);
            return;
        }

        long result = db.bookEvent(userId, eventId);
        if (result > 0) {
            String ticket = db.getTicketNumber(userId, eventId);
            showDialog("Booking confirmed! 🎉", "Your ticket number:\n" + ticket, ticket);
        } else {
            showDialog("Booking failed", "Something went wrong. Please try again.", null);
        }
    }

    private void showDialog(String title, String message, String ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Close", null);

        if (ticket != null) {
            builder.setPositiveButton("View My Tickets", (d, w) ->
                    startActivity(new Intent(this, MyTicketsActivity.class)));
        }

        builder.show();
    }
}