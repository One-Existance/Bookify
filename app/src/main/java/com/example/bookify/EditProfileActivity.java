package com.example.bookify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.data.DatabaseHelper;

public class EditProfileActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        EditText etFullName = findViewById(R.id.et_full_name);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnSave = findViewById(R.id.btn_save);

        etFullName.setText(prefs.getString("user_name", ""));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            if (name.isEmpty()) {
                tvError.setText(R.string.error_name_required);
                tvError.setVisibility(android.view.View.VISIBLE);
                return;
            }
            if (userId == -1) {
                tvError.setText(R.string.error_generic);
                tvError.setVisibility(android.view.View.VISIBLE);
                return;
            }

            db.updateUserName(userId, name);
            prefs.edit().putString("user_name", name).apply();
            finish();
        });
    }
}
