package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.MyEventRequestAdapter;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import java.util.List;

public class MyEventRequestsActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event_requests);

        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        List<Event> events = db.getEventsByOrganizer(userId);
        findViewById(R.id.tv_empty).setVisibility(events.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        RecyclerView rv = findViewById(R.id.rv_my_requests);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new MyEventRequestAdapter(events, this::shareInvite));
    }

    private void shareInvite(Event event) {
        String message = "You're invited to " + event.getTitle() + "! 🎉\n"
                + "📅 " + event.getDate() + (event.getTime() != null && !event.getTime().isEmpty() ? " · " + event.getTime() : "") + "\n"
                + "📍 " + event.getLocation() + "\n\n"
                + "Tap to view your invite:\n"
                + "bookify://event/" + event.getAccessCode() + "\n\n"
                + "Don't have Bookify installed? Enter this code in the app's Private Event screen instead:\n"
                + event.getAccessCode();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "Share invite"));
    }
}
