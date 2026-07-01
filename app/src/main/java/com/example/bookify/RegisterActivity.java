package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etPhone;
    private TextView tvError;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db          = new DatabaseHelper(this);
        etFullName  = findViewById(R.id.et_full_name);
        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        etPhone     = findViewById(R.id.et_phone);
        tvError     = findViewById(R.id.tv_error);

        ((Button) findViewById(R.id.btn_register)).setOnClickListener(v -> attemptRegister());

        ((TextView) findViewById(R.id.tv_login_link)).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String phone    = etPhone.getText()    != null ? etPhone.getText().toString().trim()    : "";

        if (TextUtils.isEmpty(fullName)) { showError("Please enter your full name."); return; }
        if (TextUtils.isEmpty(email))    { showError("Please enter your email.");     return; }
        if (TextUtils.isEmpty(password)) { showError("Please enter a password.");     return; }
        if (password.length() < 6)       { showError("Password must be at least 6 characters."); return; }

        if (db.emailExists(email)) { showError("An account with this email already exists."); return; }

        long userId = db.registerUser(fullName, email, password, phone);
        if (userId > 0) {
            getSharedPreferences("bookify_session", MODE_PRIVATE).edit()
                    .putInt("user_id", (int) userId)
                    .putString("user_name", fullName)
                    .putString("user_email", email)
                    .apply();

            Intent intent = new Intent(this, HomeFeedActivity.class);
            intent.putExtra("user_name", fullName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}