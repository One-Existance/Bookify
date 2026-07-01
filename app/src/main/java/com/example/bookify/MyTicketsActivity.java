package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.BookingAdapter;
import com.example.bookify.data.Booking;
import com.example.bookify.data.DatabaseHelper;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        DatabaseHelper db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        RecyclerView rv = findViewById(R.id.rv_tickets);
        TextView tvNoTickets = findViewById(R.id.tv_no_tickets);

        List<Booking> tickets = db.getUserBookings(userId);

        if (tickets.isEmpty()) {
            tvNoTickets.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvNoTickets.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new BookingAdapter(tickets));
        }

        // Tab switching (dummy logic)
        TextView tabUpcoming = findViewById(R.id.tab_upcoming);
        TextView tabPast     = findViewById(R.id.tab_past);

        tabPast.setOnClickListener(v ->
                Toast.makeText(this, "No past tickets", Toast.LENGTH_SHORT).show());

        // Private bookings link
        findViewById(R.id.btn_private).setOnClickListener(v ->
                startActivity(new Intent(this, PrivateEventActivity.class)));

        // Bottom nav
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeFeedActivity.class));
            finish();
        });
        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
