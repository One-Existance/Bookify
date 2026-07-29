package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.adapter.AdminEventAdapter;
import com.example.bookify.adapter.PromoterApplicationAdapter;
import com.example.bookify.adapter.UserAdapter;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import com.example.bookify.data.PromoterApplication;
import com.example.bookify.data.User;
import com.example.bookify.util.FieldFormatters;
import com.example.bookify.util.InviteShareHelper;
import com.example.bookify.util.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private EditText etTitle, etLocation, etDate, etCategory, etPrice, etTime, etSlots, etDescription;
    private ImageView ivPreview;
    private TextView tvTotalUsers, tvTotalEvents, tvTotalRevenue;
    private DatabaseHelper db;
    private RecyclerView rvEvents, rvUsers, rvApplications;
    private AdminEventAdapter eventAdapter;
    private UserAdapter userAdapter;
    private PromoterApplicationAdapter appAdapter;
    private String selectedImageUrl = "";
    private double pickedLat = 0, pickedLng = 0;

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
        rvUsers       = findViewById(R.id.rv_users);
        rvApplications = findViewById(R.id.rv_promoter_applications);

        tvTotalUsers   = findViewById(R.id.tv_total_users);
        tvTotalEvents  = findViewById(R.id.tv_total_events);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);

        FieldFormatters.attachDatePicker(this, etDate);
        FieldFormatters.attachTimePicker(this, etTime);

        findViewById(R.id.layout_select_image).setOnClickListener(v -> selectImage());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveEvent());
        findViewById(R.id.btn_add_promoter).setOnClickListener(v -> showAddPromoterDialog());
        findViewById(R.id.btn_pick_location).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, LocationPickerActivity.class), 300);
        });

        findViewById(R.id.tv_logout).setOnClickListener(v -> {
            getSharedPreferences("bookify_session", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.tv_user_view).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeFeedActivity.class));
        });

        setupRecyclerViews();
        updateStats();
    }

    private void setupRecyclerViews() {
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        refreshEventsList();

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        refreshUsersList();

        rvApplications.setLayoutManager(new LinearLayoutManager(this));
        refreshApplicationsList();
    }

    private void updateStats() {
        List<User> users = db.getAllUsers();
        List<Event> events = db.getAllEventsForAdmin();
        double revenue = 0;
        for (Event e : events) {
            revenue += db.getRevenue(e.getId());
        }

        tvTotalUsers.setText(String.valueOf(users.size()));
        tvTotalEvents.setText(String.valueOf(events.size()));
        tvTotalRevenue.setText(String.format("%.1fk", revenue / 1000));
    }

    private void refreshEventsList() {
        List<Event> events = db.getAllEventsForAdmin();
        eventAdapter = new AdminEventAdapter(events, db, new AdminEventAdapter.OnEventActionListener() {
            @Override
            public void onDeleteClick(Event event) {
                db.deleteEvent(event.getId());
                refreshEventsList();
                updateStats();
                Toast.makeText(AdminActivity.this, R.string.admin_event_deleted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewClick(Event event) {
                Intent intent = new Intent(AdminActivity.this, EventDetailActivity.class);
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
            }

            @Override
            public void onShareClick(Event event) {
                InviteShareHelper.shareGeneric(AdminActivity.this, event);
            }

            @Override
            public void onShareWhatsAppClick(Event event) {
                InviteShareHelper.shareViaWhatsApp(AdminActivity.this, event);
            }

            @Override
            public void onScanClick(Event event) {
                Intent intent = new Intent(AdminActivity.this, ScanEntryActivity.class);
                intent.putExtra("event_id", event.getId());
                intent.putExtra("event_access_code", event.getAccessCode());
                intent.putExtra("event_title", event.getTitle());
                startActivity(intent);
            }
        });
        rvEvents.setAdapter(eventAdapter);
    }

    private void refreshUsersList() {
        List<User> users = db.getAllUsers();
        userAdapter = new UserAdapter(users, user -> {
            db.verifyPromoter(user.getId());
            refreshUsersList();
            updateStats();
            Toast.makeText(this, getString(R.string.admin_promoter_verified_fmt, user.getFullName()), Toast.LENGTH_SHORT).show();
        });
        rvUsers.setAdapter(userAdapter);
    }

    private void refreshApplicationsList() {
        List<PromoterApplication> applications = db.getPendingPromoterApplications();
        findViewById(R.id.tv_no_applications).setVisibility(
                applications.isEmpty() ? View.VISIBLE : View.GONE);

        appAdapter = new PromoterApplicationAdapter(applications, new PromoterApplicationAdapter.OnApplicationActionListener() {
            @Override
            public void onApprove(PromoterApplication application) {
                db.approvePromoterApplication(application.getId(), application.getUserId());
                Toast.makeText(AdminActivity.this, getString(R.string.admin_now_promoter_fmt, application.getApplicantName()), Toast.LENGTH_SHORT).show();
                NotificationHelper.notify(AdminActivity.this, application.getId(),
                        getString(R.string.admin_promoter_approved_title),
                        getString(R.string.admin_now_promoter_notif_fmt, application.getApplicantName()),
                        new Intent(AdminActivity.this, AdminActivity.class));
                refreshApplicationsList();
                refreshUsersList();
                updateStats();
            }

            @Override
            public void onReject(PromoterApplication application) {
                db.rejectPromoterApplication(application.getId());
                Toast.makeText(AdminActivity.this, R.string.admin_application_rejected, Toast.LENGTH_SHORT).show();
                NotificationHelper.notify(AdminActivity.this, application.getId(),
                        getString(R.string.admin_application_rejected_title),
                        getString(R.string.admin_application_rejected_notif_fmt, application.getApplicantName()),
                        new Intent(AdminActivity.this, AdminActivity.class));
                refreshApplicationsList();
            }
        });
        rvApplications.setAdapter(appAdapter);
    }

    private void showAddPromoterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_promoter, null);
        EditText etName = dialogView.findViewById(R.id.et_promoter_name);
        EditText etEmail = dialogView.findViewById(R.id.et_promoter_email);
        EditText etPass = dialogView.findViewById(R.id.et_promoter_password);

        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_register_new_promoter_title)
                .setView(dialogView)
                .setPositiveButton(R.string.admin_register_button, (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String pass = etPass.getText().toString().trim();

                    if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(this, R.string.admin_fill_all_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (pass.length() < 6) {
                        Toast.makeText(this, R.string.admin_password_min_length, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Auto-verified for admin registration: bypasses the application/approval flow
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(result -> {
                                FirebaseUser firebaseUser = result.getUser();
                                long id = db.registerUserWithRole(name, email, firebaseUser.getUid(), "", User.ROLE_PROMOTER);
                                if (id > 0) {
                                    Toast.makeText(this, R.string.admin_promoter_registered, Toast.LENGTH_SHORT).show();
                                    refreshUsersList();
                                    updateStats();
                                } else {
                                    Toast.makeText(this, R.string.admin_registration_failed, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
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
                Toast.makeText(this, R.string.admin_image_selected, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            pickedLat = data.getDoubleExtra("latitude", 0);
            pickedLng = data.getDoubleExtra("longitude", 0);
            String address = data.getStringExtra("address");
            if (address != null && !address.isEmpty()) {
                etLocation.setText(address);
            }
            Toast.makeText(this, R.string.admin_location_pinned, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveEvent() {
        String title    = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date     = etDate.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String price    = FieldFormatters.formatPrice(etPrice.getText().toString());
        String time     = etTime.getText().toString().trim();
        String slots    = etSlots.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, R.string.admin_fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.addEvent(title, location, date, category, price, false, selectedImageUrl, time, slots, desc, pickedLat, pickedLng);
        if (id > 0) {
            Toast.makeText(this, R.string.admin_event_posted_success, Toast.LENGTH_SHORT).show();
            clearFields();
            refreshEventsList();
            updateStats();
        } else {
            Toast.makeText(this, R.string.admin_post_event_failed, Toast.LENGTH_SHORT).show();
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
        pickedLat = 0;
        pickedLng = 0;
        ivPreview.setAlpha(0.3f);
        ivPreview.setImageResource(R.drawable.bg_qr_placeholder);
    }
}
