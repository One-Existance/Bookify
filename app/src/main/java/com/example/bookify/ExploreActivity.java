package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.EventAdapter;
import com.example.bookify.data.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class ExploreActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        db = new DatabaseHelper(this);

        RecyclerView rvEvents = findViewById(R.id.rv_explore_events);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(db.getAllEvents());
        adapter.setOnEventClickListener(event -> {
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
            intent.putExtra("event_lat",      event.getLatitude());
            intent.putExtra("event_lng",      event.getLongitude());
            startActivity(intent);
        });
        rvEvents.setAdapter(adapter);

        TextInputEditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.search(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Bottom nav
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeFeedActivity.class));
            finish();
        });
        findViewById(R.id.nav_tickets).setOnClickListener(v ->
                startActivity(new Intent(this, MyTicketsActivity.class)));
        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}