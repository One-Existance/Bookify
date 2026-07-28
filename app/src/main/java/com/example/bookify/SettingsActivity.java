package com.example.bookify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.os.LocaleListCompat;
import com.example.bookify.util.NotificationHelper;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView tvLanguageValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("bookify_settings", MODE_PRIVATE);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        SwitchCompat switchNotifications = findViewById(R.id.switch_notifications);
        SwitchCompat switchEmailReminders = findViewById(R.id.switch_email_reminders);
        SwitchCompat switchDarkMode = findViewById(R.id.switch_dark_mode);
        tvLanguageValue = findViewById(R.id.tv_language_value);

        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        switchEmailReminders.setChecked(prefs.getBoolean("email_reminders", true));
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode", true));

        switchNotifications.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("notifications", checked).apply();
            if (checked) {
                if (!NotificationHelper.hasPermission(this)) {
                    NotificationHelper.requestPermissionIfNeeded(this, 1001);
                    Toast.makeText(this, R.string.settings_notification_hint, Toast.LENGTH_LONG).show();
                }
            }
        });

        switchEmailReminders.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("email_reminders", checked).apply());

        switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        findViewById(R.id.row_language).setOnClickListener(v -> showLanguageDialog());
        updateLanguageLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLanguageLabel();
    }

    private void showLanguageDialog() {
        String[] labels = {getString(R.string.language_english), getString(R.string.language_swahili)};
        String[] tags = {"en", "sw"};
        int checked = isSwahiliSelected() ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags[which]));
                    dialog.dismiss();
                })
                .show();
    }

    private void updateLanguageLabel() {
        tvLanguageValue.setText(isSwahiliSelected()
                ? getString(R.string.language_swahili)
                : getString(R.string.language_english));
    }

    private boolean isSwahiliSelected() {
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        return !current.isEmpty() && "sw".equals(current.get(0).getLanguage());
    }
}
