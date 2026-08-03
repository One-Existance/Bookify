package com.example.bookify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.EventsRepository;

public class PrivateEventActivity extends AppCompatActivity {

    private EditText etAccessCode;
    private TextView tvError;
    private Button btnAccess;
    private EventsRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_event);

        repo = new EventsRepository();
        etAccessCode = findViewById(R.id.et_access_code);
        tvError = findViewById(R.id.tv_error);
        btnAccess = findViewById(R.id.btn_access);

        btnAccess.setOnClickListener(v -> accessEvent());

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

        handleDeepLink(getIntent());
    }

    /** Handles invite links of the form bookify://event/<ACCESS_CODE> by pre-filling
     *  and auto-submitting the code, so tapping a shared link jumps straight into
     *  the private event's details instead of requiring manual entry. */
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data == null) return;

        String code = data.getLastPathSegment();
        if (TextUtils.isEmpty(code)) return;

        etAccessCode.setText(code);
        accessEvent();
    }

    private void accessEvent() {
        String code = etAccessCode.getText() != null ? etAccessCode.getText().toString().trim() : "";
        if (TextUtils.isEmpty(code)) {
            showError(getString(R.string.private_event_error_empty_code));
            return;
        }

        tvError.setVisibility(View.GONE);
        setLoading(true);

        repo.getEventByAccessCode(code.toUpperCase())
                .addOnSuccessListener(event -> {
                    setLoading(false);
                    if (event == null) {
                        showError(getString(R.string.private_event_error_invalid_code));
                        return;
                    }
                    Intent intent = new Intent(this, EventDetailActivity.class);
                    intent.putExtra("event_id", event.getId());
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(getString(R.string.private_event_error_network));
                });
    }

    private void setLoading(boolean loading) {
        btnAccess.setEnabled(!loading);
        btnAccess.setText(loading ? R.string.private_event_checking : R.string.private_event_access_button);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
