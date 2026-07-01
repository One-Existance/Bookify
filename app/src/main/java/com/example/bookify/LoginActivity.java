package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvError;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db        = new DatabaseHelper(this);
        etEmail   = findViewById(R.id.et_email);
        etPassword= findViewById(R.id.et_password);
        tvError   = findViewById(R.id.tv_error);

        findViewById(R.id.btn_login).setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tv_signup_link).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email))    { showError("Please enter your email.");    return; }
        if (TextUtils.isEmpty(password)) { showError("Please enter your password."); return; }

        User user = db.loginUser(email, password);
        if (user != null) {
            saveSession(user.getId(), user.getFullName(), user.getEmail(), user.isAdmin());
            
            Intent intent;
            if (user.isAdmin()) {
                intent = new Intent(this, AdminActivity.class);
            } else {
                intent = new Intent(this, HomeFeedActivity.class);
                intent.putExtra("user_name", user.getFullName());
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            showError("Invalid email or password.");
        }
    }

    private void saveSession(int userId, String name, String email, boolean isAdmin) {
        getSharedPreferences("bookify_session", MODE_PRIVATE).edit()
                .putInt("user_id", userId)
                .putString("user_name", name)
                .putString("user_email", email)
                .putBoolean("is_admin", isAdmin)
                .apply();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}