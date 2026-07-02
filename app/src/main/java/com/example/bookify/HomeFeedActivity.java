package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.EventAdapter;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import java.util.List;

public class HomeFeedActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EventAdapter eventAdapter;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_feed);

        db = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        userName = prefs.getString("user_name", getIntent().getStringExtra("user_name"));
        if (userName == null) userName = "Guest";
        String firstName = userName.split(" ")[0];

        TextView tvGreeting = findViewById(R.id.tv_greeting);
        TextView tvAvatar   = findViewById(R.id.tv_avatar);
        tvGreeting.setText(firstName + " ✨");
        tvAvatar.setText(getInitials(userName));

        // Avatar → Profile
        tvAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("user_name", userName);
            startActivity(intent);
        });

        // Events RecyclerView
        List<Event> events = db.getAllEvents();
        RecyclerView rvEvents = findViewById(R.id.rv_events);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(events);
        eventAdapter.setOnEventClickListener(event -> {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("event_id",       event.getId());
            intent.putExtra("event_title",    event.getTitle());
            intent.putExtra("event_location", event.getLocation());
            intent.putExtra("event_date",     event.getDate());
            intent.putExtra("event_price",    event.getPrice());
            intent.putExtra("event_time",     event.getTime());
            intent.putExtra("event_slots",    event.getSlots());
            intent.putExtra("event_about",    event.getDescription());
            intent.putExtra("event_image",    event.getImageUrl());
            startActivity(intent);
        });
        rvEvents.setAdapter(eventAdapter);

        setupCategoryChips();
        setupBottomNav();

        int userRole = prefs.getInt("user_role", 0);
        if (userRole == 1 || userRole == 2) { // Admin or Promoter
            findViewById(R.id.fab_admin).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.fab_admin).setOnClickListener(v -> {
                if (userRole == 1) startActivity(new Intent(this, AdminActivity.class));
                else startActivity(new Intent(this, PromoterActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventAdapter != null) {
            eventAdapter.updateData(db.getAllEvents());
        }
    }

    private void setupCategoryChips() {
        TextView chipAll        = findViewById(R.id.chip_all);
        TextView chipConcerts   = findViewById(R.id.chip_concerts);
        TextView chipSports     = findViewById(R.id.chip_sports);
        TextView chipConference = findViewById(R.id.chip_conference);
        TextView chipGala       = findViewById(R.id.chip_gala);
        TextView chipParty      = findViewById(R.id.chip_party);

        TextView[] allChips = {chipAll, chipConcerts, chipSports, chipConference, chipGala, chipParty};

        chipAll.setOnClickListener(v -> {
            eventAdapter.filter(null);
            setChipSelected(chipAll, allChips);
        });
        chipConcerts.setOnClickListener(v -> {
            eventAdapter.filter("Concert");
            setChipSelected(chipConcerts, allChips);
        });
        chipSports.setOnClickListener(v -> {
            eventAdapter.filter("Sports");
            setChipSelected(chipSports, allChips);
        });
        chipConference.setOnClickListener(v -> {
            eventAdapter.filter("Conference");
            setChipSelected(chipConference, allChips);
        });
        chipGala.setOnClickListener(v -> {
            eventAdapter.filter("Gala");
            setChipSelected(chipGala, allChips);
        });
        chipParty.setOnClickListener(v -> {
            eventAdapter.filter("Party");
            setChipSelected(chipParty, allChips);
        });
    }

    private void setupBottomNav() {
        findViewById(R.id.nav_explore).setOnClickListener(v ->
                startActivity(new Intent(this, ExploreActivity.class)));

        findViewById(R.id.nav_tickets).setOnClickListener(v ->
                startActivity(new Intent(this, MyTicketsActivity.class)));

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("user_name", userName);
            startActivity(intent);
        });
    }

    private void setChipSelected(TextView selected, TextView[] allChips) {
        for (TextView chip : allChips) {
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(getResources().getColor(R.color.white, getTheme()));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_default);
                chip.setTextColor(getResources().getColor(R.color.text_muted, getTheme()));
            }
        }
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
