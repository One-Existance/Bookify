package com.example.bookify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.bumptech.glide.Glide;
import com.example.bookify.data.Event;
import com.example.bookify.data.EventsRepository;
import com.example.bookify.util.FieldFormatters;
import com.example.bookify.util.ImageUploadHelper;

import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private EditText etTitle, etLocation, etCategory, etDate, etTime, etPrice, etSlots, etDescription;
    private SwitchCompat switchPrivate;
    private ImageView ivPreview;
    private Button btnSaveChanges;
    private EventsRepository repo;
    private String eventId;
    private Event event;
    private String selectedImageUrl;
    private double pickedLat, pickedLng;
    private boolean editable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        repo = new EventsRepository();
        eventId = getIntent().getStringExtra("event_id");

        etTitle       = findViewById(R.id.et_title);
        etLocation    = findViewById(R.id.et_location);
        etCategory    = findViewById(R.id.et_category);
        etDate        = findViewById(R.id.et_date);
        etTime        = findViewById(R.id.et_time);
        etPrice       = findViewById(R.id.et_price);
        etSlots       = findViewById(R.id.et_slots);
        etDescription = findViewById(R.id.et_description);
        switchPrivate = findViewById(R.id.switch_private);
        ivPreview     = findViewById(R.id.iv_event_preview);

        FieldFormatters.attachDatePicker(this, etDate);
        FieldFormatters.attachTimePicker(this, etTime);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
        findViewById(R.id.layout_select_image).setOnClickListener(v -> selectImage());
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        findViewById(R.id.btn_pick_location).setOnClickListener(v ->
                startActivityForResult(new Intent(this, LocationPickerActivity.class), 300));

        if (eventId == null) {
            Toast.makeText(this, R.string.edit_event_load_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEvent();
    }

    private void loadEvent() {
        repo.getEventById(eventId)
                .addOnSuccessListener(loaded -> {
                    if (loaded == null) {
                        Toast.makeText(this, R.string.edit_event_load_failed, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    event = loaded;
                    selectedImageUrl = loaded.getImageUrl();
                    pickedLat = loaded.getLatitude();
                    pickedLng = loaded.getLongitude();
                    prefillForm(loaded);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.edit_event_load_failed, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void prefillForm(Event event) {
        findViewById(R.id.progress_loading).setVisibility(android.view.View.GONE);
        findViewById(R.id.scroll_content).setVisibility(android.view.View.VISIBLE);

        etTitle.setText(event.getTitle());
        etLocation.setText(event.getLocation());
        etCategory.setText(event.getCategory());
        etDate.setText(event.getDate());
        etTime.setText(event.getTime());
        etPrice.setText(event.getPrice());
        etSlots.setText(event.getSlots());
        etDescription.setText(event.getDescription());
        switchPrivate.setChecked(event.isPrivate());

        if (!TextUtils.isEmpty(event.getImageUrl())) {
            Glide.with(this).load(event.getImageUrl()).into(ivPreview);
            ivPreview.setAlpha(1.0f);
        }

        editable = FieldFormatters.isUpcoming(event.getDate(), event.getTime());
        if (!editable) {
            findViewById(R.id.tv_past_event_notice).setVisibility(android.view.View.VISIBLE);
            btnSaveChanges.setEnabled(false);
            setFieldsEnabled(false);
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        etTitle.setEnabled(enabled);
        etLocation.setEnabled(enabled);
        etCategory.setEnabled(enabled);
        etDate.setEnabled(enabled);
        etTime.setEnabled(enabled);
        etPrice.setEnabled(enabled);
        etSlots.setEnabled(enabled);
        etDescription.setEnabled(enabled);
        switchPrivate.setEnabled(enabled);
        findViewById(R.id.layout_select_image).setEnabled(enabled);
        findViewById(R.id.btn_pick_location).setEnabled(enabled);
    }

    private void selectImage() {
        if (!editable) return;
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
        } else if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            pickedLat = data.getDoubleExtra("latitude", 0);
            pickedLng = data.getDoubleExtra("longitude", 0);
            String address = data.getStringExtra("address");
            if (address != null && !address.isEmpty()) {
                etLocation.setText(address);
            }
        }
    }

    private void saveChanges() {
        if (!editable || event == null) return;

        String title    = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date     = etDate.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String price    = FieldFormatters.formatPrice(etPrice.getText().toString());
        String time     = etTime.getText().toString().trim();
        String slots    = etSlots.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, R.string.organize_fill_required, Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveChanges.setEnabled(false);
        ImageUploadHelper.resolveImageUrl(this, selectedImageUrl)
                .addOnSuccessListener(finalImageUrl -> {
                    Map<String, Object> fields = new HashMap<>();
                    fields.put("title", title);
                    fields.put("location", location);
                    fields.put("date", date);
                    fields.put("time", time);
                    fields.put("category", category);
                    fields.put("price", price);
                    fields.put("slots", slots);
                    fields.put("description", desc);
                    fields.put("imageUrl", finalImageUrl);
                    fields.put("isPrivate", switchPrivate.isChecked());
                    fields.put("latitude", pickedLat);
                    fields.put("longitude", pickedLng);

                    repo.updateEvent(eventId, fields)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, R.string.edit_event_saved_success, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnSaveChanges.setEnabled(true);
                                Toast.makeText(this, R.string.edit_event_save_failed, Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, R.string.edit_event_save_failed, Toast.LENGTH_SHORT).show();
                });
    }
}
