package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfile;
    private TextView tvInitials;
    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        String name  = prefs.getString("user_name",  getIntent().getStringExtra("user_name"));
        String email = prefs.getString("user_email", "");
        if (name == null) name = "Guest";

        ivProfile = findViewById(R.id.iv_profile_image);
        tvInitials = findViewById(R.id.tv_avatar_initials);
        
        ((TextView) findViewById(R.id.tv_name)).setText(name);
        ((TextView) findViewById(R.id.tv_email)).setText(email);
        tvInitials.setText(getInitials(name));

        loadProfileImage();

        findViewById(R.id.layout_profile_image).setOnClickListener(v -> selectImage());

        // My Bookings
        findViewById(R.id.row_my_bookings).setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class)));

        // Settings
        findViewById(R.id.row_settings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // Logout
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
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

    private void loadProfileImage() {
        String img = db.getProfileImage(userId);
        if (img != null && !img.isEmpty()) {
            try {
                ivProfile.setImageURI(Uri.parse(img));
                tvInitials.setVisibility(View.GONE);
            } catch (Exception e) {
                tvInitials.setVisibility(View.VISIBLE);
            }
        } else {
            tvInitials.setVisibility(View.VISIBLE);
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 300);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                db.updateProfileImage(userId, imageUri.toString());
                ivProfile.setImageURI(imageUri);
                tvInitials.setVisibility(View.GONE);
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
