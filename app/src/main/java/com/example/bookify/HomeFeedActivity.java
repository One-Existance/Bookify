package com.example.bookify;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.adapter.EventAdapter;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFeedActivity extends AppCompatActivity {

    private static final String TAG = "BookifyLifecycle";

    private DatabaseHelper db;
    private EventAdapter adapter;
    private List<Event> allEvents;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "HomeFeedActivity onCreate: Home feed is starting");
        setContentView(R.layout.activity_home_feed);

        db = new DatabaseHelper(this);
        allEvents = db.getAllPublicEvents();

        String userName = getIntent().getStringExtra("user_name");
        setupHeader(userName);
        setupCategoryChips();
        setupRecyclerView();
    }

    private void setupHeader(String userName) {
        TextView textGreeting = findViewById(R.id.textGreeting);
        TextView textUserName = findViewById(R.id.textUserName);
        TextView textAvatar = findViewById(R.id.textAvatar);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) textGreeting.setText(getString(R.string.greeting_morning));
        else if (hour < 17) textGreeting.setText(getString(R.string.greeting_afternoon));
        else textGreeting.setText(getString(R.string.greeting_evening));

        String displayName = (userName != null && !userName.isEmpty())
                ? userName.split(" ")[0] : "Guest";

        textUserName.setText(displayName + " ✨");
        textAvatar.setText(displayName.length() >= 2
                ? displayName.substring(0, 2).toUpperCase()
                : displayName.toUpperCase());
    }

    private void setupCategoryChips() {
        LinearLayout chipGroup = findViewById(R.id.chipGroup);
        chipGroup.removeAllViews();

        List<String> categories = new ArrayList<>();
        categories.add("All");
        for (Event e : allEvents) {
            if (!categories.contains(e.category)) categories.add(e.category);
        }

        int marginDp = (int) (8 * getResources().getDisplayMetrics().density);
        int paddingH = (int) (18 * getResources().getDisplayMetrics().density);
        int paddingV = (int) (8 * getResources().getDisplayMetrics().density);

        for (String category : categories) {
            TextView chip = new TextView(this);
            chip.setText(category);
            chip.setTextColor(Color.WHITE);
            chip.setTextSize(13);
            chip.setPadding(paddingH, paddingV, paddingH, paddingV);
            chip.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(marginDp);
            chip.setLayoutParams(params);

            updateChipStyle(chip, category.equals(selectedCategory));

            chip.setOnClickListener(v -> {
                selectedCategory = category;
                filterEvents();
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    TextView c = (TextView) chipGroup.getChildAt(i);
                    updateChipStyle(c, c.getText().toString().equals(selectedCategory));
                }
            });

            chipGroup.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.bg_filter_chip_selected);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundResource(R.drawable.bg_filter_chip_default);
            chip.setTextColor(Color.parseColor("#9B98B8"));
        }
    }

    private void setupRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recyclerEvents);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(allEvents);
        recycler.setAdapter(adapter);
    }

    private void filterEvents() {
        if (selectedCategory.equals("All")) {
            adapter.updateEvents(allEvents);
        } else {
            List<Event> filtered = new ArrayList<>();
            for (Event e : allEvents) {
                if (e.category.equals(selectedCategory)) filtered.add(e);
            }
            adapter.updateEvents(filtered);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "HomeFeedActivity onStart: Home feed is now visible to the user");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "HomeFeedActivity onResume: Home feed is in the foreground — user can interact");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "HomeFeedActivity onPause: Home feed is partially hidden");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "HomeFeedActivity onStop: Home feed is no longer visible");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        Log.d(TAG, "HomeFeedActivity onDestroy: Home feed destroyed — database closed");
    }
}
