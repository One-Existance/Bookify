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
import com.example.bookify.data.EventRequest;
import com.example.bookify.data.PromoterProfile;
import com.example.bookify.data.User;
import java.util.List;

public class PromoterDashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView rvRequests;
    private int userId;

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
        userId = prefs.getInt("user_id", -1);
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
        List<EventRequest> requests = db.getPendingEventRequestsForPromoter(userId);
        findViewById(R.id.tv_empty).setVisibility(requests.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        rvRequests.setAdapter(new EventRequestAdapter(requests, new EventRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(EventRequest request) {
                showAcceptDialog(request);
            }

            @Override
            public void onReject(EventRequest request) {
                db.rejectEventRequest(request.getEvent().getId());
                Toast.makeText(PromoterDashboardActivity.this, "Request rejected", Toast.LENGTH_SHORT).show();
                refreshRequests();
            }
        }));
    }

    private void showAcceptDialog(EventRequest request) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Price");
        etPrice.setText(request.getEvent().getPrice());
        layout.addView(etPrice);

        EditText etDate = new EditText(this);
        etDate.setHint("Date");
        etDate.setText(request.getEvent().getDate());
        layout.addView(etDate);

        EditText etTime = new EditText(this);
        etTime.setHint("Time");
        etTime.setText(request.getEvent().getTime());
        layout.addView(etTime);

        new AlertDialog.Builder(this)
                .setTitle("Confirm terms & accept")
                .setView(layout)
                .setPositiveButton("Accept", (dialog, which) -> {
                    db.approveEventRequest(
                            request.getEvent().getId(),
                            etPrice.getText().toString().trim(),
                            etDate.getText().toString().trim(),
                            etTime.getText().toString().trim(),
                            request.getEvent().isPrivate());
                    Toast.makeText(this, "Event approved!", Toast.LENGTH_SHORT).show();
                    refreshRequests();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
