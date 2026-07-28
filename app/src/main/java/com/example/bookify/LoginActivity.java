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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.User;
import com.example.bookify.util.AuthGate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvError;
    private DatabaseHelper db;
    private FirebaseAuth auth;
    private boolean passwordVisible = false;

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

        ImageView ivPasswordToggle = findViewById(R.id.iv_password_toggle);
        ivPasswordToggle.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            togglePasswordVisibility(etPassword, ivPasswordToggle, passwordVisible);
        });

        findViewById(R.id.tv_signup_link).setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            android.os.Bundle pendingEvent = getIntent().getBundleExtra(AuthGate.PENDING_EVENT_EXTRA);
            if (pendingEvent != null) intent.putExtra(AuthGate.PENDING_EVENT_EXTRA, pendingEvent);
            startActivity(intent);
            finish();
        });
    }

    private void attemptLogin() {
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email))    { showError(getString(R.string.login_error_email_required));    return; }
        if (TextUtils.isEmpty(password)) { showError(getString(R.string.login_error_password_required)); return; }

        if (email.equalsIgnoreCase(DatabaseHelper.TEST_USER_EMAIL)) {
            // ... (keep existing test logic)
            User testUser = db.getUserByEmail(DatabaseHelper.TEST_USER_EMAIL);
            if (testUser != null) {
                routeAfterLogin(testUser);
            } else {
                showError(getString(R.string.login_error_test_account_missing));
            }
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> onFirebaseLoginSuccess(result.getUser()))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException
                            || e instanceof FirebaseAuthInvalidCredentialsException) {
                        showError(getString(R.string.login_error_invalid_credentials));
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

        android.os.Bundle pendingEvent = getIntent().getBundleExtra(AuthGate.PENDING_EVENT_EXTRA);
        if (pendingEvent != null) {
            AuthGate.continueToPendingEvent(this, pendingEvent);
            return;
        }

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

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, boolean visible) {
        android.graphics.Typeface typeface = editText.getTypeface();
        if (visible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye_off);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye);
        }
        editText.setTypeface(typeface);
        editText.setSelection(editText.getText().length());
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
