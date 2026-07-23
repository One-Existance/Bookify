package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword, etPhone;
    private DatabaseHelper db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db          = new DatabaseHelper(this);
        auth        = FirebaseAuth.getInstance();
        etFullName  = findViewById(R.id.et_full_name);
        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhone     = findViewById(R.id.et_phone);

        findViewById(R.id.btn_register).setOnClickListener(v -> attemptRegister());

        findViewById(R.id.tv_login_link).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();
        String phone    = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    firebaseUser.updateProfile(
                            new UserProfileChangeRequest.Builder().setDisplayName(fullName).build());

                    long localId = db.registerLocalProfile(fullName, email, firebaseUser.getUid(), phone);
                    if (localId <= 0) {
                        setLoading(false);
                        Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveSession((int) localId, fullName, email);

                    Intent intent = new Intent(this, HomeFeedActivity.class);
                    intent.putExtra("user_name", fullName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    String message;
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        message = "Email already exists";
                    } else if (e instanceof FirebaseAuthWeakPasswordException) {
                        message = "Password is too weak";
                    } else {
                        message = "Registration failed: " + e.getMessage();
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSession(int userId, String name, String email) {
        getSharedPreferences("bookify_session", MODE_PRIVATE).edit()
                .putInt("user_id", userId)
                .putString("user_name", name)
                .putString("user_email", email)
                .putString("role", User.ROLE_USER)
                .apply();
    }

    private void setLoading(boolean loading) {
        findViewById(R.id.btn_register).setEnabled(!loading);
    }
}
