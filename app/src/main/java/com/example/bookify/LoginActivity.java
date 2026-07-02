package com.example.bookify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView ivPasswordToggle;
    private TextView tvError;
    private boolean isPasswordVisible = false;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db               = new DatabaseHelper(this);
        etEmail          = findViewById(R.id.et_email);
        etPassword       = findViewById(R.id.et_password);
        ivPasswordToggle = findViewById(R.id.iv_password_toggle);
        tvError          = findViewById(R.id.tv_error);

        ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());

        findViewById(R.id.btn_login).setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tv_signup_link).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivPasswordToggle.setImageResource(android.R.drawable.ic_menu_view); // Use a closed eye icon if you have one
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivPasswordToggle.setImageResource(android.R.drawable.ic_menu_view);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email))    { showError("Please enter your email.");    return; }
        if (TextUtils.isEmpty(password)) { showError("Please enter your password."); return; }

        try {
            User user = db.loginUser(email, password);
            if (user != null) {
                if (user.isPromoter() && !user.isVerified()) {
                    showError("Your promoter account is pending verification.");
                    return;
                }

                saveSession(user.getId(), user.getFullName(), user.getEmail(), user.getRole());

                Intent intent;
                if (user.isAdmin()) {
                    intent = new Intent(this, AdminActivity.class);
                } else if (user.isPromoter()) {
                    intent = new Intent(this, PromoterActivity.class);
                } else {
                    intent = new Intent(this, HomeFeedActivity.class);
                    intent.putExtra("user_name", user.getFullName());
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                showError("Invalid email or password.");
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSession(int userId, String name, String email, int role) {
        getSharedPreferences("bookify_session", MODE_PRIVATE).edit()
                .putInt("user_id", userId)
                .putString("user_name", name)
                .putString("user_email", email)
                .putInt("user_role", role)
                .putBoolean("is_admin", role == 1) // Keep for backward compatibility
                .apply();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}