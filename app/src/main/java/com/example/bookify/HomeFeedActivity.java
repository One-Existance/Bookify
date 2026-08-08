package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.EventAdapter;
import com.example.bookify.data.EventsRepository;
import com.example.bookify.data.User;
import com.example.bookify.util.AuthGate;
import com.example.bookify.util.NotificationHelper;
import java.util.ArrayList;

public class HomeFeedActivity extends AppCompatActivity {

    private EventsRepository repo;
    private EventAdapter eventAdapter;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_feed);

        repo = new EventsRepository();

        NotificationHelper.requestPermissionIfNeeded(this, 1001);

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
        RecyclerView rvEvents = findViewById(R.id.rv_events);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>());
        eventAdapter.setOnEventClickListener(event -> {
            if (!AuthGate.isLoggedIn(this)) {
                AuthGate.promptLogin(this, event);
                return;
            }
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
        rvEvents.setAdapter(eventAdapter);
        loadEvents();

        setupCategoryChips();
        setupBottomNav();
        setupRoleFab(prefs.getString("role", User.ROLE_USER));

        findViewById(R.id.btn_refresh).setOnClickListener(v -> {
            v.animate().rotationBy(360f).setDuration(400).start();
            loadEvents();
        });
    }

    private void loadEvents() {
        repo.getAllEvents()
                .addOnSuccessListener(events -> eventAdapter.updateData(events))
                .addOnFailureListener(e -> android.widget.Toast.makeText(this, R.string.error_generic, android.widget.Toast.LENGTH_SHORT).show());
    }

    private void setupRoleFab(String role) {
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab_action);
        fab.setVisibility(android.view.View.VISIBLE);
        fab.setOnClickListener(v -> {
            if (!AuthGate.isLoggedIn(this)) {
                AuthGate.promptLogin(this);
                return;
            }
            if (User.ROLE_ADMIN.equals(role)) {
                startActivity(new Intent(this, AdminActivity.class));
            } else if (User.ROLE_PROMOTER.equals(role)) {
                startActivity(new Intent(this, PromoterDashboardActivity.class));
            } else {
                startActivity(new Intent(this, OrganizeEventActivity.class));
            }
        });
    }

    private void setupCategoryChips() {
        TextView chipAll        = findViewById(R.id.chip_all);
        TextView chipConcerts   = findViewById(R.id.chip_concerts);
        TextView chipSports     = findViewById(R.id.chip_sports);
        TextView chipConference = findViewById(R.id.chip_conference);

        chipAll.setOnClickListener(v -> {
            eventAdapter.filter(null);
            setChipSelected(chipAll, chipConcerts, chipSports, chipConference);
        });
        chipConcerts.setOnClickListener(v -> {
            eventAdapter.filter("Concert");
            setChipSelected(chipConcerts, chipAll, chipSports, chipConference);
        });
        chipSports.setOnClickListener(v -> {
            eventAdapter.filter("Sports");
            setChipSelected(chipSports, chipAll, chipConcerts, chipConference);
        });
        chipConference.setOnClickListener(v -> {
            eventAdapter.filter("Conference");
            setChipSelected(chipConference, chipAll, chipConcerts, chipSports);
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

    private void setChipSelected(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(getResources().getColor(R.color.white, getTheme()));
        int mutedColor = resolveThemeColor(R.attr.colorAppTextMuted);
        for (TextView chip : others) {
            chip.setBackgroundResource(R.drawable.bg_chip_default);
            chip.setTextColor(mutedColor);
        }
    }

    private int resolveThemeColor(int attrResId) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}