package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.EventAdapter;
import com.example.bookify.data.EventsRepository;
import com.example.bookify.util.AuthGate;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class ExploreActivity extends AppCompatActivity {

    private EventsRepository repo;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        repo = new EventsRepository();

        RecyclerView rvEvents = findViewById(R.id.rv_explore_events);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(new ArrayList<>());
        adapter.setOnEventClickListener(event -> {
            if (!AuthGate.isLoggedIn(this)) {
                AuthGate.promptLogin(this, event);
                return;
            }
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
        rvEvents.setAdapter(adapter);
        repo.getAllEvents()
                .addOnSuccessListener(events -> adapter.updateData(events))
                .addOnFailureListener(e -> android.widget.Toast.makeText(this, R.string.error_generic, android.widget.Toast.LENGTH_SHORT).show());

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