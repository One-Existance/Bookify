package com.example.bookify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookify.data.DatabaseHelper;
import com.example.bookify.data.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "BookifyLifecycle";

    private DatabaseHelper db;
    private TextInputEditText editEmail, editPassword;
    private TextView textError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LoginActivity onCreate: Login screen is starting");
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        editEmail    = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        textError    = findViewById(R.id.textError);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> handleLogin());

        findViewById(R.id.textSignUp).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email    = text(editEmail);
        String password = text(editPassword);

        if (email.isEmpty())    { showError(getString(R.string.error_enter_email)); return; }
        if (password.isEmpty()) { showError(getString(R.string.error_enter_login_password)); return; }

        User user = db.loginUser(email, password);
        if (user != null) {
            Intent intent = new Intent(this, HomeFeedActivity.class);
            intent.putExtra("user_name", user.fullName);
            startActivity(intent);
        } else {
            showError(getString(R.string.error_invalid_credentials));
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
        Log.d(TAG, "LoginActivity onStart: Login screen is now visible to the user");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LoginActivity onResume: Login screen is in the foreground — user can interact");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LoginActivity onPause: Login screen is partially hidden");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LoginActivity onStop: Login screen is no longer visible");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        Log.d(TAG, "LoginActivity onDestroy: Login screen destroyed — database closed");
    }
}
