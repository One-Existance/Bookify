package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.PromoterApplication;
import com.example.bookify.data.User;
import com.google.android.material.textfield.TextInputEditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.AdminEventAdapter;
import com.example.bookify.adapter.PromoterApplicationAdapter;
import com.example.bookify.data.Event;
import java.util.List;

import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageView;

public class AdminActivity extends AppCompatActivity {

    private EditText etTitle, etLocation, etDate, etCategory, etPrice, etTime, etSlots, etDescription;
    private ImageView ivPreview;
    private DatabaseHelper db;
    private RecyclerView rvEvents, rvApplications;
    private AdminEventAdapter adapter;
    private String selectedImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sessionCheck = getSharedPreferences("bookify_session", MODE_PRIVATE);
        if (!User.ROLE_ADMIN.equals(sessionCheck.getString("role", ""))) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);

        etTitle       = findViewById(R.id.et_title);
        etLocation    = findViewById(R.id.et_location);
        etDate        = findViewById(R.id.et_date);
        etCategory    = findViewById(R.id.et_category);
        etPrice       = findViewById(R.id.et_price);
        etTime        = findViewById(R.id.et_time);
        etSlots       = findViewById(R.id.et_slots);
        etDescription = findViewById(R.id.et_description);
        ivPreview     = findViewById(R.id.iv_event_preview);
        rvEvents      = findViewById(R.id.rv_admin_events);
        rvApplications = findViewById(R.id.rv_promoter_applications);

        findViewById(R.id.layout_select_image).setOnClickListener(v -> selectImage());

        findViewById(R.id.btn_save).setOnClickListener(v -> saveEvent());

        findViewById(R.id.tv_logout).setOnClickListener(v -> {
            getSharedPreferences("bookify_session", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.tv_user_view).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeFeedActivity.class));
        });

        setupRecyclerView();
        setupApplicationsList();
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
                Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        List<Event> events = db.getAllEventsForAdmin();
        adapter = new AdminEventAdapter(events, event -> {
            db.deleteEvent(event.getId());
            refreshList();
        });
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);
    }

    private void refreshList() {
        List<Event> events = db.getAllEventsForAdmin();
        adapter = new AdminEventAdapter(events, event -> {
            db.deleteEvent(event.getId());
            refreshList();
        });
        rvEvents.setAdapter(adapter);
    }

    private void setupApplicationsList() {
        rvApplications.setLayoutManager(new LinearLayoutManager(this));
        refreshApplications();
    }

    private void refreshApplications() {
        List<PromoterApplication> applications = db.getPendingPromoterApplications();
        findViewById(R.id.tv_no_applications).setVisibility(
                applications.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        rvApplications.setAdapter(new PromoterApplicationAdapter(applications, new PromoterApplicationAdapter.OnApplicationActionListener() {
            @Override
            public void onApprove(PromoterApplication application) {
                db.approvePromoterApplication(application.getId(), application.getUserId());
                Toast.makeText(AdminActivity.this, application.getApplicantName() + " is now a Promoter", Toast.LENGTH_SHORT).show();
                refreshApplications();
            }

            @Override
            public void onReject(PromoterApplication application) {
                db.rejectPromoterApplication(application.getId());
                Toast.makeText(AdminActivity.this, "Application rejected", Toast.LENGTH_SHORT).show();
                refreshApplications();
            }
        }));
    }

    private void saveEvent() {
        String title    = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date     = etDate.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String price    = etPrice.getText().toString().trim();
        String time     = etTime.getText().toString().trim();
        String slots    = etSlots.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill title, location and date", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.addEvent(title, location, date, category, price, false, selectedImageUrl, time, slots, desc);
        if (id > 0) {
            Toast.makeText(this, "Event posted successfully! 🚀", Toast.LENGTH_SHORT).show();
            clearFields();
            refreshList();
        } else {
            Toast.makeText(this, "Failed to post event", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etTitle.setText("");
        etLocation.setText("");
        etDate.setText("");
        etCategory.setText("");
        etPrice.setText("");
        etTime.setText("");
        etSlots.setText("");
        etDescription.setText("");
        selectedImageUrl = "";
        ivPreview.setAlpha(0.4f);
    }
}
