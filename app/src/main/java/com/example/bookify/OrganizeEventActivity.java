package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.PromoterProfile;
import java.util.ArrayList;
import java.util.List;

public class OrganizeEventActivity extends AppCompatActivity {

    private EditText etTitle, etCategory, etDate, etTime, etPrice, etSlots, etDescription;
    private Spinner spinnerPromoter;
    private SwitchCompat switchPrivate;
    private ImageView ivPreview;
    private DatabaseHelper db;
    private List<PromoterProfile> promoters;
    private String selectedImageUrl = "";
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organize_event);

        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        etTitle       = findViewById(R.id.et_title);
        etCategory    = findViewById(R.id.et_category);
        etDate        = findViewById(R.id.et_date);
        etTime        = findViewById(R.id.et_time);
        etPrice       = findViewById(R.id.et_price);
        etSlots       = findViewById(R.id.et_slots);
        etDescription = findViewById(R.id.et_description);
        spinnerPromoter = findViewById(R.id.spinner_promoter);
        switchPrivate   = findViewById(R.id.switch_private);
        ivPreview       = findViewById(R.id.iv_event_preview);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
        findViewById(R.id.layout_select_image).setOnClickListener(v -> selectImage());
        findViewById(R.id.btn_submit_request).setOnClickListener(v -> submitRequest());

        setupPromoterSpinner();
    }

    private void setupPromoterSpinner() {
        promoters = db.getApprovedPromoters();
        List<String> labels = new ArrayList<>();
        for (PromoterProfile p : promoters) {
            labels.add(p.getHallName() + " — " + p.getLocation());
        }
        if (labels.isEmpty()) {
            labels.add("No promoters available yet");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        spinnerPromoter.setAdapter(adapter);
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

    private void submitRequest() {
        if (promoters.isEmpty()) {
            Toast.makeText(this, "No approved promoters to request yet — check back later", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == -1) {
            Toast.makeText(this, "You must be logged in to organize an event", Toast.LENGTH_SHORT).show();
            return;
        }

        String title    = etTitle.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String date     = etDate.getText().toString().trim();
        String time     = etTime.getText().toString().trim();
        String price    = etPrice.getText().toString().trim();
        String slots    = etSlots.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Please fill title, date and price", Toast.LENGTH_SHORT).show();
            return;
        }

        PromoterProfile promoter = promoters.get(spinnerPromoter.getSelectedItemPosition());
        boolean isPrivate = switchPrivate.isChecked();

        long id = db.requestEvent(title, promoter.getLocation(), date, category, price,
                isPrivate, selectedImageUrl, time, slots, desc, userId, promoter.getUserId());

        if (id > 0) {
            Toast.makeText(this, "Request sent to " + promoter.getHallName() + "!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MyEventRequestsActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
        }
    }
}
