package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;

public class PrivateEventActivity extends AppCompatActivity {

    private EditText etAccessCode;
    private TextView tvError;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_event);

        db = new DatabaseHelper(this);
        etAccessCode = findViewById(R.id.et_access_code);
        tvError = findViewById(R.id.tv_error);

        findViewById(R.id.btn_access).setOnClickListener(v -> accessEvent());

        // Bottom nav
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeFeedActivity.class));
            finish();
        });
        findViewById(R.id.nav_tickets).setOnClickListener(v -> {
            startActivity(new Intent(this, MyTicketsActivity.class));
            finish();
        });
        findViewById(R.id.nav_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void accessEvent() {
        String code = etAccessCode.getText() != null ? etAccessCode.getText().toString().trim() : "";
        if (TextUtils.isEmpty(code)) {
            showError("Please enter an access code.");
            return;
        }

        Event event = db.getEventByAccessCode(code.toUpperCase());
        if (event == null) {
            showError("Invalid or expired code.");
            return;
        }

        tvError.setVisibility(View.GONE);
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
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
