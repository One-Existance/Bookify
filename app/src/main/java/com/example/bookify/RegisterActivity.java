package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookify.data.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "BookifyLifecycle";

    private DatabaseHelper db;
    private TextInputEditText editFullName, editEmail, editPhone, editPassword;
    private TextView textError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "RegisterActivity onCreate: Register screen is starting");
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        editFullName = findViewById(R.id.editFullName);
        editEmail    = findViewById(R.id.editEmail);
        editPhone    = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);
        textError    = findViewById(R.id.textError);

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> handleRegister());

        findViewById(R.id.textLogin).setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = text(editFullName);
        String email    = text(editEmail);
        String phone    = text(editPhone);
        String password = text(editPassword);

        if (fullName.isEmpty()) { showError(getString(R.string.error_enter_full_name)); return; }
        if (email.isEmpty())    { showError(getString(R.string.error_enter_email)); return; }
        if (phone.isEmpty())    { showError(getString(R.string.error_enter_phone)); return; }
        if (password.isEmpty()) { showError(getString(R.string.error_enter_password)); return; }

        boolean success = db.registerUser(fullName, email, password, phone);
        if (success) {
            Intent intent = new Intent(this, HomeFeedActivity.class);
            intent.putExtra("user_name", fullName);
            startActivity(intent);
            finish();
        } else {
            showError(getString(R.string.error_email_registered));
        }
    }

    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void showError(String message) {
        textError.setText(message);
        textError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "RegisterActivity onStart: Register screen is now visible to the user");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "RegisterActivity onResume: Register screen is in the foreground — user can interact");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "RegisterActivity onPause: Register screen is partially hidden");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "RegisterActivity onStop: Register screen is no longer visible");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        Log.d(TAG, "RegisterActivity onDestroy: Register screen destroyed — database closed");
    }
}
