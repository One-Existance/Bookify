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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvError;
    private DatabaseHelper db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db        = new DatabaseHelper(this);
        auth      = FirebaseAuth.getInstance();
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

        if (email.equalsIgnoreCase(DatabaseHelper.TEST_USER_EMAIL)) {
            // Local-only test account: skip Firebase entirely so login always works,
            // even offline or before the Firebase project's Auth is fully set up.
            User testUser = db.getUserByEmail(DatabaseHelper.TEST_USER_EMAIL);
            if (testUser != null) {
                routeAfterLogin(testUser);
            } else {
                showError("Test account missing — reinstall the app to reseed the database.");
            }
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> onFirebaseLoginSuccess(result.getUser()))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException
                            || e instanceof FirebaseAuthInvalidCredentialsException) {
                        showError("Invalid email or password.");
                    } else {
                        showError("Login failed: " + e.getMessage());
                    }
                });
    }

    private void onFirebaseLoginSuccess(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        User user = db.getUserByFirebaseUid(uid);
        if (user == null) {
            // First time this account is seen on this device: link by email
            // (covers the seeded admin row) or create a fresh local profile.
            user = db.getUserByEmail(email);
            if (user != null) {
                db.linkFirebaseUid(user.getId(), uid);
            } else {
                String displayName = firebaseUser.getDisplayName();
                String fullName = TextUtils.isEmpty(displayName) ? email : displayName;
                long localId = db.registerLocalProfile(fullName, email, uid, "");
                user = new User((int) localId, fullName, email, "", User.ROLE_USER);
            }
        }

        routeAfterLogin(user);
    }

    private void routeAfterLogin(User user) {
        saveSession(user.getId(), user.getFullName(), user.getEmail(), user.getRole());

        Intent intent;
        if (user.isAdmin()) {
            intent = new Intent(this, AdminActivity.class);
        } else if (user.isPromoter()) {
            intent = new Intent(this, PromoterDashboardActivity.class);
        } else {
            intent = new Intent(this, HomeFeedActivity.class);
            intent.putExtra("user_name", user.getFullName());
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void saveSession(int userId, String name, String email, String role) {
        getSharedPreferences("bookify_session", MODE_PRIVATE).edit()
                .putInt("user_id", userId)
                .putString("user_name", name)
                .putString("user_email", email)
                .putString("role", role)
                .apply();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
