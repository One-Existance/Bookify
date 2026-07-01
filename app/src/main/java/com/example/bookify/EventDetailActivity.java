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
        String time     = getIntent().getStringExtra("event_time");
        String slots    = getIntent().getStringExtra("event_slots");
        String about    = getIntent().getStringExtra("event_about");
        String image    = getIntent().getStringExtra("event_image");

        if (title    != null) ((TextView) findViewById(R.id.tv_event_title)).setText(title);
        if (price    != null) ((TextView) findViewById(R.id.tv_price)).setText(price);
        if (date     != null) ((TextView) findViewById(R.id.tv_date)).setText(date);
        if (time     != null) ((TextView) findViewById(R.id.tv_time)).setText(time);
        if (slots    != null) ((TextView) findViewById(R.id.tv_slots)).setText(slots + " remaining");
        if (about    != null) ((TextView) findViewById(R.id.tv_about)).setText(about);

        if (image != null && !image.isEmpty()) {
            android.widget.ImageView iv = findViewById(R.id.iv_detail_image);
            iv.setVisibility(android.view.View.VISIBLE);
            iv.setImageURI(android.net.Uri.parse(image));
            findViewById(R.id.image_overlay).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.tv_detail_icon).setVisibility(android.view.View.GONE);
        }

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
            if (existing != null) {
                showDialog("Already booked", "Your ticket number:\n" + existing, existing);
            } else {
                // Booking exists but not completed (status is PENDING)
                goToPayment();
            }
            return;
        }

        long result = db.bookEvent(userId, eventId);
        if (result > 0) {
            goToPayment();
        } else {
            showDialog("Booking failed", "Something went wrong. Please try again.", null);
        }
    }

    private void goToPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_title", ((TextView) findViewById(R.id.tv_event_title)).getText().toString());
        intent.putExtra("event_price", ((TextView) findViewById(R.id.tv_price)).getText().toString());
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            String ticketNumber = db.getTicketNumber(userId, eventId);
            
            // Go directly to ticket detail to show QR code
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra("event_title", ((TextView) findViewById(R.id.tv_event_title)).getText().toString());
            intent.putExtra("event_info", getIntent().getStringExtra("event_date"));
            intent.putExtra("ticket_number", ticketNumber);
            startActivity(intent);
            finish();
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