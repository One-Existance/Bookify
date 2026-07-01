package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MyTicketsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        // Tab switching (no logic yet)
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
