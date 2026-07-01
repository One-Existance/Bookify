package com.example.bookify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("bookify_settings", MODE_PRIVATE);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        SwitchCompat switchNotifications = findViewById(R.id.switch_notifications);
        SwitchCompat switchEmailReminders = findViewById(R.id.switch_email_reminders);

        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        switchEmailReminders.setChecked(prefs.getBoolean("email_reminders", true));

        switchNotifications.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("notifications", checked).apply());

        switchEmailReminders.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("email_reminders", checked).apply());
    }
}