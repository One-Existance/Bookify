package com.example.bookify;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class BookifyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase manually to ensure it's ready for all activities
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            // Log if needed or handle cases where google-services.json might be invalid
            android.util.Log.e("BookifyApp", "Firebase initialization failed", e);
        }
    }
}
