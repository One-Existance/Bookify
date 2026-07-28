package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.PromoterApplication;
import com.example.bookify.data.User;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        String role  = prefs.getString("role", User.ROLE_USER);
        int userId   = prefs.getInt("user_id", -1);

        refreshProfileHeader();

        // Edit profile name
        findViewById(R.id.btn_edit_name).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        // My Bookings
        findViewById(R.id.row_my_bookings).setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class)));

        // My Organized Events
        findViewById(R.id.row_my_events).setOnClickListener(v ->
                startActivity(new Intent(this, MyEventRequestsActivity.class)));

        setupPromoterRow(role, userId);

        // Settings
        findViewById(R.id.row_settings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // Logout
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Bottom nav
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeFeedActivity.class));
            finish();
        });
        findViewById(R.id.nav_tickets).setOnClickListener(v ->
                startActivity(new Intent(this, MyTicketsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileHeader();
    }

    private void refreshProfileHeader() {
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        String name  = prefs.getString("user_name",  getIntent().getStringExtra("user_name"));
        String email = prefs.getString("user_email", "");
        if (name == null) name = "Guest";

        ((TextView) findViewById(R.id.tv_name)).setText(name);
        ((TextView) findViewById(R.id.tv_email)).setText(email);
        ((TextView) findViewById(R.id.tv_avatar_initials)).setText(getInitials(name));
    }

    private void setupPromoterRow(String role, int userId) {
        TextView label = findViewById(R.id.tv_promoter_row_label);
        TextView arrow = findViewById(R.id.tv_promoter_row_arrow);

        if (User.ROLE_ADMIN.equals(role)) {
            findViewById(R.id.row_promoter).setVisibility(android.view.View.GONE);
            return;
        }

        if (User.ROLE_PROMOTER.equals(role)) {
            label.setText(R.string.profile_promoter_dashboard);
            arrow.setText("›");
            findViewById(R.id.row_promoter).setOnClickListener(v ->
                    startActivity(new Intent(this, PromoterDashboardActivity.class)));
            return;
        }

        PromoterApplication application = db.getLatestPromoterApplication(userId);
        if (application != null && PromoterApplication.STATUS_PENDING.equals(application.getStatus())) {
            label.setText(R.string.profile_promoter_pending);
            arrow.setText("");
            findViewById(R.id.row_promoter).setOnClickListener(null);
        } else {
            label.setText(R.string.profile_become_promoter);
            arrow.setText("›");
            findViewById(R.id.row_promoter).setOnClickListener(v ->
                    startActivity(new Intent(this, BecomePromoterActivity.class)));
        }
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
