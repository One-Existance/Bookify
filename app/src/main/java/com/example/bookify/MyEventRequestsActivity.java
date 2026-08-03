package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.MyEventRequestAdapter;
import com.example.bookify.data.Event;
import com.example.bookify.data.EventsRepository;
import com.example.bookify.util.InviteShareHelper;

public class MyEventRequestsActivity extends AppCompatActivity {

    private EventsRepository repo;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event_requests);

        repo = new EventsRepository();

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        rv = findViewById(R.id.rv_my_requests);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reloads on every return (e.g. from EditEventActivity) so edits show immediately.
        loadEvents();
    }

    private void loadEvents() {
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        String userUid = prefs.getString("firebase_uid", "");

        repo.getEventsByOrganizer(userUid)
                .addOnSuccessListener(events -> {
                    findViewById(R.id.tv_empty).setVisibility(events.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    rv.setAdapter(new MyEventRequestAdapter(events,
                            event -> InviteShareHelper.shareGeneric(this, event),
                            event -> InviteShareHelper.shareViaWhatsApp(this, event),
                            this::launchScanner,
                            this::launchEdit));
                })
                .addOnFailureListener(e -> android.widget.Toast.makeText(this, R.string.error_generic, android.widget.Toast.LENGTH_SHORT).show());
    }

    private void launchEdit(Event event) {
        Intent intent = new Intent(this, EditEventActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    private void launchScanner(Event event) {
        Intent intent = new Intent(this, ScanEntryActivity.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_access_code", event.getAccessCode());
        intent.putExtra("event_title", event.getTitle());
        startActivity(intent);
    }
}
