package com.example.bookify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.AdminEventAdapter;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import java.util.List;

public class PromoterActivity extends AppCompatActivity {

    private EditText etTitle, etLocation, etDate, etPrice, etTime, etSlots, etDescription;
    private Spinner spCategory;
    private ImageView ivPreview;
    private TextView tvWelcome, tvTotalEvents, tvTotalRevenue;
    private DatabaseHelper db;
    private RecyclerView rvEvents;
    private AdminEventAdapter adapter;
    private String selectedImageUrl = "";
    private int promoterId;

    private final String[] categories = {"Concert", "Gala", "Sports", "Conference", "Party", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promoter);

        db = new DatabaseHelper(this);
        promoterId = getSharedPreferences("bookify_session", MODE_PRIVATE).getInt("user_id", 0);
        String name = getSharedPreferences("bookify_session", MODE_PRIVATE).getString("user_name", "Promoter");

        etTitle       = findViewById(R.id.et_title);
        etLocation    = findViewById(R.id.et_location);
        etDate        = findViewById(R.id.et_date);
        spCategory    = findViewById(R.id.sp_category);
        etPrice       = findViewById(R.id.et_price);
        etTime        = findViewById(R.id.et_time);
        etSlots       = findViewById(R.id.et_slots);
        etDescription = findViewById(R.id.et_description);
        ivPreview     = findViewById(R.id.iv_event_preview);
        rvEvents      = findViewById(R.id.rv_promoter_events);
        
        tvWelcome      = findViewById(R.id.tv_welcome);
        tvTotalEvents  = findViewById(R.id.tv_total_events);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);

        tvWelcome.setText("Hello, " + name);

        setupCategorySpinner();
        setupRecyclerView();
        updateStats();

        findViewById(R.id.layout_select_image).setOnClickListener(v -> selectImage());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveEvent());

        findViewById(R.id.tv_logout).setOnClickListener(v -> {
            getSharedPreferences("bookify_session", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void updateStats() {
        List<Event> events = db.getEventsByPromoter(promoterId);
        double revenue = 0;
        for (Event e : events) {
            revenue += db.getRevenue(e.getId());
        }

        tvTotalEvents.setText(String.valueOf(events.size()));
        tvTotalRevenue.setText(String.format("Tsh %.1fk", revenue / 1000));
    }

    private void setupRecyclerView() {
        List<Event> events = db.getEventsByPromoter(promoterId);
        adapter = new AdminEventAdapter(events, db, new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onDeleteClick(Event event) {
                db.deleteEvent(event.getId());
                refreshList();
            }

            @Override
            public void onViewClick(Event event) {
                Intent intent = new Intent(PromoterActivity.this, EventDetailActivity.class);
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
        });
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);
    }

    private void refreshList() {
        List<Event> events = db.getEventsByPromoter(promoterId);
        adapter = new AdminEventAdapter(events, db, new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onDeleteClick(Event event) {
                db.deleteEvent(event.getId());
                refreshList();
            }

            @Override
            public void onViewClick(Event event) {
                Intent intent = new Intent(PromoterActivity.this, EventDetailActivity.class);
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
        });
        rvEvents.setAdapter(adapter);
        updateStats();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectedImageUrl = imageUri.toString();
                ivPreview.setImageURI(imageUri);
                ivPreview.setAlpha(1.0f);
            }
        }
    }

    private void saveEvent() {
        String title    = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date     = etDate.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String price    = etPrice.getText().toString().trim();
        String time     = etTime.getText().toString().trim();
        String slots    = etSlots.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.addEventWithPromoter(title, location, date, category, "Tsh " + price, false, selectedImageUrl, time, slots, desc, promoterId);
        if (id > 0) {
            Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
            clearFields();
            refreshList();
        } else {
            Toast.makeText(this, "Failed to add event", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etTitle.setText("");
        etLocation.setText("");
        etDate.setText("");
        etPrice.setText("");
        etTime.setText("");
        etSlots.setText("");
        etDescription.setText("");
        selectedImageUrl = "";
        ivPreview.setAlpha(0.3f);
        ivPreview.setImageResource(R.drawable.bg_qr_placeholder);
    }
}
