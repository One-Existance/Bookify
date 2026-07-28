package com.example.bookify;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.FirebaseApp;

public class BookifyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences settings = getSharedPreferences("bookify_settings", MODE_PRIVATE);
        boolean darkMode = settings.getBoolean("dark_mode", true);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize Firebase manually to ensure it's ready for all activities
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            // Log if needed or handle cases where google-services.json might be invalid
            android.util.Log.e("BookifyApp", "Firebase initialization failed", e);
        }
    }
}
