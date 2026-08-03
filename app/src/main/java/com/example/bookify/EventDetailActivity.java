package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import com.example.bookify.data.EventsRepository;
import com.example.bookify.util.NotificationHelper;

public class EventDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EventsRepository repo;
    private String eventId;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = new DatabaseHelper(this);
        repo = new EventsRepository();

        eventId = getIntent().getStringExtra("event_id");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_book).setOnClickListener(v -> bookTicket());

        if (eventId == null) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEvent();
    }

    private void loadEvent() {
        repo.getEventById(eventId)
                .addOnSuccessListener(loaded -> {
                    if (loaded == null) {
                        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    event = loaded;
                    bindEvent(loaded);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void bindEvent(Event event) {
        findViewById(R.id.progress_loading).setVisibility(android.view.View.GONE);
        findViewById(R.id.scroll_content).setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.bottom_bar).setVisibility(android.view.View.VISIBLE);

        ((TextView) findViewById(R.id.tv_event_title)).setText(event.getTitle());
        ((TextView) findViewById(R.id.tv_price)).setText(event.getPrice());
        ((TextView) findViewById(R.id.tv_date)).setText(event.getDate());
        ((TextView) findViewById(R.id.tv_time)).setText(event.getTime());

        int taken = db.getTicketCount(eventId, true);
        int totalSlots = 0;
        try { totalSlots = Integer.parseInt(event.getSlots()); } catch (Exception ignored) {}
        int remaining = Math.max(0, totalSlots - taken);
        ((TextView) findViewById(R.id.tv_slots)).setText(remaining + " seats remaining");

        ((TextView) findViewById(R.id.tv_about)).setText(event.getDescription());

        String image = event.getImageUrl();
        if (image != null && !image.isEmpty()) {
            android.widget.ImageView iv = findViewById(R.id.iv_detail_image);
            iv.setVisibility(android.view.View.VISIBLE);
            Glide.with(this).load(image).into(iv);
            findViewById(R.id.image_overlay).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.iv_detail_icon).setVisibility(android.view.View.GONE);
        }

        ((TextView) findViewById(R.id.tv_event_location_date)).setText(event.getLocation() + " · " + event.getDate());

        findViewById(R.id.btn_view_map).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("location_name", event.getLocation());
            intent.putExtra("event_lat", event.getLatitude());
            intent.putExtra("event_lng", event.getLongitude());
            startActivity(intent);
        });
    }

    private void bookTicket() {
        if (event == null) return;

        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            showDialog(getString(R.string.event_detail_not_logged_in_title), getString(R.string.event_detail_not_logged_in_message), null);
            return;
        }

        if (db.isAlreadyBooked(userId, eventId)) {
            String existing = db.getTicketNumber(userId, eventId);
            if (existing != null) {
                showDialog(getString(R.string.event_detail_already_booked_title), getString(R.string.event_detail_ticket_number_prefix) + existing, existing);
            } else {
                // Booking exists but not completed (status is PENDING)
                goToPayment();
            }
            return;
        }

        long result = db.bookEvent(userId, eventId, event.getTitle(), event.getDate(),
                event.getCategory(), event.getPrice(), event.getImageUrl());
        if (result > 0) {
            goToPayment();
        } else {
            showDialog(getString(R.string.event_detail_booking_failed_title), getString(R.string.error_generic), null);
        }
    }

    private void goToPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_price", event.getPrice());
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
            String eventTitle = event.getTitle();
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra("event_title", eventTitle);
            intent.putExtra("event_info", event.getDate());
            intent.putExtra("ticket_number", ticketNumber);

            NotificationHelper.notify(this, eventId.hashCode(),
                    "Booking Confirmed",
                    "Your ticket for " + eventTitle + " is ready. Tap to view your QR code.",
                    new Intent(intent));

            startActivity(intent);
            finish();
        }
    }

    private void showDialog(String title, String message, String ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.dialog_close, null);

        if (ticket != null) {
            builder.setPositiveButton(R.string.event_detail_view_my_tickets, (d, w) ->
                    startActivity(new Intent(this, MyTicketsActivity.class)));
        }

        builder.show();
    }
}
