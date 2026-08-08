package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import com.example.bookify.data.EventsRepository;
import com.example.bookify.data.PromoterProfile;
import com.example.bookify.util.FieldFormatters;
import com.example.bookify.util.ImageUploadHelper;
import java.util.ArrayList;
import java.util.List;

public class OrganizeEventActivity extends AppCompatActivity {

    private EditText etTitle, etCategory, etDate, etTime, etPrice, etSlots, etDescription;
    private Spinner spinnerPromoter;
    private TextView tvPromoterLocation;
    private SwitchCompat switchPrivate;
    private ImageView ivPreview;
    private Button btnSubmit;
    private DatabaseHelper db;
    private EventsRepository repo;
    private List<PromoterProfile> promoters;
    private String selectedImageUrl = "";
    private int userId;
    private String userUid;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organize_event);

        db = new DatabaseHelper(this);
        repo = new EventsRepository();
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        userUid = prefs.getString("firebase_uid", "");
        userName = prefs.getString("user_name", "");

        etTitle       = findViewById(R.id.et_title);
        etCategory    = findViewById(R.id.et_category);
        etDate        = findViewById(R.id.et_date);
        etTime        = findViewById(R.id.et_time);
        etPrice       = findViewById(R.id.et_price);
        etSlots       = findViewById(R.id.et_slots);
        etDescription = findViewById(R.id.et_description);
        spinnerPromoter = findViewById(R.id.spinner_promoter);
        tvPromoterLocation = findViewById(R.id.tv_promoter_location);
        switchPrivate   = findViewById(R.id.switch_private);
        ivPreview       = findViewById(R.id.iv_event_preview);

        FieldFormatters.attachDatePicker(this, etDate);
        FieldFormatters.attachTimePicker(this, etTime);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
        findViewById(R.id.layout_select_image).setOnClickListener(v -> selectImage());
        btnSubmit = findViewById(R.id.btn_submit_request);
        btnSubmit.setOnClickListener(v -> submitRequest());

        setupPromoterSpinner();
    }

    private void setupPromoterSpinner() {
        promoters = db.getApprovedPromoters();
        List<String> labels = new ArrayList<>();
        for (PromoterProfile p : promoters) {
            labels.add(p.getHallName() + " — " + p.getLocation());
        }
        if (labels.isEmpty()) {
            labels.add(getString(R.string.organize_no_promoters));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        spinnerPromoter.setAdapter(adapter);

        // The event's location always comes from the chosen promoter's hall - no free-text entry,
        // so it can't drift from where the promoter actually is. Kept in sync as the selection changes.
        spinnerPromoter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tvPromoterLocation.setText(promoters.isEmpty() ? "" : promoters.get(position).getLocation());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvPromoterLocation.setText("");
            }
        });
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
            Toast.makeText(this, R.string.organize_no_promoters_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == -1) {
            Toast.makeText(this, R.string.organize_must_login, Toast.LENGTH_SHORT).show();
            return;
        }

        String title    = etTitle.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String date     = etDate.getText().toString().trim();
        String time     = etTime.getText().toString().trim();
        String price    = FieldFormatters.formatPrice(etPrice.getText().toString());
        String slots    = etSlots.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(price)) {
            Toast.makeText(this, R.string.organize_fill_required, Toast.LENGTH_SHORT).show();
            return;
        }

        PromoterProfile promoter = promoters.get(spinnerPromoter.getSelectedItemPosition());
        boolean isPrivate = switchPrivate.isChecked();

        // Location always comes from the selected promoter's hall - see setupPromoterSpinner().
        double promoterLat = promoter.getLatitude() != null ? promoter.getLatitude() : 0;
        double promoterLng = promoter.getLongitude() != null ? promoter.getLongitude() : 0;

        btnSubmit.setEnabled(false);
        ImageUploadHelper.resolveImageUrl(this, selectedImageUrl)
                .addOnSuccessListener(finalImageUrl -> {
                    Event draft = new Event("", title, promoter.getLocation(), date, category, price, isPrivate,
                            finalImageUrl, time, slots, desc,
                            userUid, userName, promoter.getFirebaseUid(), promoter.getFullName(),
                            Event.STATUS_PENDING, null, promoterLat, promoterLng);

                    repo.requestEvent(draft)
                            .addOnSuccessListener(id -> {
                                Toast.makeText(this, getString(R.string.organize_request_sent, promoter.getHallName()), Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, MyEventRequestsActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnSubmit.setEnabled(true);
                                Toast.makeText(this, R.string.organize_request_failed, Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, R.string.organize_request_failed, Toast.LENGTH_SHORT).show();
                });
    }
}
