package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BookifyLifecycle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: App is starting");
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: App is now visible to the user");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: App is in the foreground — user can interact");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: App is partially hidden");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: App is no longer visible");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: App is closing");
    }
}
