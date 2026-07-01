package com.example.bookify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookify.adapter.BookingAdapter;
import com.example.bookify.data.Booking;
import com.example.bookify.data.DatabaseHelper;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        DatabaseHelper db = new DatabaseHelper(this);
        List<Booking> bookings = userId != -1 ? db.getUserBookings(userId) : List.of();

        TextView tvEmpty = findViewById(R.id.tv_empty);
        RecyclerView rv  = findViewById(R.id.rv_bookings);

        if (bookings.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new BookingAdapter(bookings));
        }
    }
}