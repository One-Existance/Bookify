package com.example.bookify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;

public class BecomePromoterActivity extends AppCompatActivity {

    private EditText etHallName, etLocation, etDescription;
    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_promoter);

        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        etHallName    = findViewById(R.id.et_hall_name);
        etLocation    = findViewById(R.id.et_location);
        etDescription = findViewById(R.id.et_description);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_submit).setOnClickListener(v -> submitApplication());
    }

    private void submitApplication() {
        String hallName = etHallName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(hallName) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Please fill in hall name and location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == -1) {
            Toast.makeText(this, "You must be logged in to apply", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.submitPromoterApplication(userId, hallName, location, description);
        if (id > 0) {
            Toast.makeText(this, "Application submitted! We'll review it soon.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to submit application", Toast.LENGTH_SHORT).show();
        }
    }
}
