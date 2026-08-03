package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.EventRequestAdapter;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;
import com.example.bookify.data.EventRequest;
import com.example.bookify.data.EventsRepository;
import com.example.bookify.data.PromoterProfile;
import com.example.bookify.data.User;
import com.example.bookify.util.FieldFormatters;
import com.example.bookify.util.NotificationHelper;
import java.util.ArrayList;
import java.util.List;

public class PromoterDashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EventsRepository repo;
    private RecyclerView rvRequests;
    private int userId;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        if (!User.ROLE_PROMOTER.equals(prefs.getString("role", ""))) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_promoter_dashboard);

        db = new DatabaseHelper(this);
        repo = new EventsRepository();
        userId = prefs.getInt("user_id", -1);
        userUid = prefs.getString("firebase_uid", "");
        rvRequests = findViewById(R.id.rv_requests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        PromoterProfile profile = db.getPromoterProfile(userId);
        if (profile != null) {
            ((android.widget.TextView) findViewById(R.id.tv_hall_name)).setText(profile.getHallName());
            ((android.widget.TextView) findViewById(R.id.tv_hall_location)).setText(profile.getLocation());
        }

        findViewById(R.id.tv_logout).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.tv_user_view).setOnClickListener(v ->
                startActivity(new Intent(this, HomeFeedActivity.class)));

        refreshRequests();
    }

    private void refreshRequests() {
        repo.getPendingRequestsForPromoter(userUid)
                .addOnSuccessListener(events -> {
                    List<EventRequest> requests = new ArrayList<>();
                    for (Event event : events) {
                        requests.add(new EventRequest(event, event.getOrganizerName()));
                    }
                    findViewById(R.id.tv_empty).setVisibility(requests.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    rvRequests.setAdapter(new EventRequestAdapter(requests, new EventRequestAdapter.OnRequestActionListener() {
                        @Override
                        public void onAccept(EventRequest request) {
                            showAcceptDialog(request);
                        }

                        @Override
                        public void onReject(EventRequest request) {
                            repo.rejectEventRequest(request.getEvent().getId())
                                    .addOnSuccessListener(v -> {
                                        Toast.makeText(PromoterDashboardActivity.this, R.string.promoter_request_rejected, Toast.LENGTH_SHORT).show();
                                        NotificationHelper.notify(PromoterDashboardActivity.this, request.getEvent().getId().hashCode(),
                                                getString(R.string.promoter_notif_rejected_title),
                                                getString(R.string.promoter_notif_rejected_body, request.getEvent().getTitle()),
                                                new Intent(PromoterDashboardActivity.this, PromoterDashboardActivity.class));
                                        refreshRequests();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(PromoterDashboardActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show());
                        }
                    }));
                })
                .addOnFailureListener(e -> Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show());
    }

    private void showAcceptDialog(EventRequest request) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText etPrice = new EditText(this);
        etPrice.setHint(R.string.promoter_hint_price);
        etPrice.setText(request.getEvent().getPrice());
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPrice);

        EditText etDate = new EditText(this);
        etDate.setHint(R.string.hint_date);
        etDate.setText(request.getEvent().getDate());
        FieldFormatters.attachDatePicker(this, etDate);
        layout.addView(etDate);

        EditText etTime = new EditText(this);
        etTime.setHint(R.string.hint_time);
        etTime.setText(request.getEvent().getTime());
        FieldFormatters.attachTimePicker(this, etTime);
        layout.addView(etTime);

        new AlertDialog.Builder(this)
                .setTitle(R.string.promoter_confirm_title)
                .setView(layout)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    repo.approveEventRequest(
                            request.getEvent().getId(),
                            FieldFormatters.formatPrice(etPrice.getText().toString()),
                            etDate.getText().toString().trim(),
                            etTime.getText().toString().trim(),
                            request.getEvent().isPrivate())
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, R.string.promoter_event_approved, Toast.LENGTH_SHORT).show();
                                NotificationHelper.notify(this, request.getEvent().getId().hashCode(),
                                        getString(R.string.promoter_notif_approved_title),
                                        getString(R.string.promoter_notif_approved_body, request.getEvent().getTitle()),
                                        new Intent(this, PromoterDashboardActivity.class));
                                refreshRequests();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
