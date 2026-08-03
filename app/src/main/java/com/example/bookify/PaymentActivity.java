package com.example.bookify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookify.BuildConfig;
import com.example.bookify.data.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PaymentActivity extends AppCompatActivity {

    private String apiKey = BuildConfig.MONGIKE_API_KEY;
    private String eventPrice, eventTitle;
    private int eventId, userId;
    private DatabaseHelper db;
    private TextInputEditText etPhone;
    private ProgressBar progressBar;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("bookify_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        eventId    = getIntent().getIntExtra("event_id", -1);
        eventPrice = getIntent().getStringExtra("event_price");
        eventTitle = getIntent().getStringExtra("event_title");

        ((TextView) findViewById(R.id.tv_payment_amount)).setText(eventPrice);
        etPhone = findViewById(R.id.et_phone);
        progressBar = findViewById(R.id.pb_loading);
        btnPay = findViewById(R.id.btn_pay_now);

        btnPay.setOnClickListener(v -> initiatePayment());
    }

    private void initiatePayment() {
        String phoneInput = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phoneInput)) {
            Toast.makeText(this, R.string.payment_toast_enter_phone, Toast.LENGTH_SHORT).show();
            return;
        }

        // Normalize phone number to international format (255...)
        String tempPhone = phoneInput;
        if (tempPhone.startsWith("0")) {
            tempPhone = "255" + tempPhone.substring(1);
        } else if (tempPhone.startsWith("+")) {
            tempPhone = tempPhone.substring(1);
        }
        final String phone = tempPhone;

        // Robust amount parsing: extract only digits
        String amountStr = eventPrice.replaceAll("[^0-9]", "");
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(apiKey)) {
            Toast.makeText(this, "Configuration Error: Mongike API Key is missing. Check local.properties", Toast.LENGTH_LONG).show();
            return;
        }

        btnPay.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Capture provider on UI thread
        String provider = "MPESA";
        android.widget.RadioGroup rg = findViewById(R.id.rg_network);
        int checkedId = rg.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_tigo) provider = "TIGO";
        else if (checkedId == R.id.rb_airtel) provider = "AIRTEL";
        else if (checkedId == R.id.rb_halo) provider = "HALOPESA";
        final String finalProvider = provider;

        new Thread(() -> {
            String transactionId = callMongikeInitiate(phone, amountStr, finalProvider);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                progressBar.setVisibility(View.GONE);
                btnPay.setEnabled(true);
                
                if (transactionId != null) {
                    // Optimistic success: prompt pushed, give ticket immediately
                    db.completePayment(userId, eventId);
                    Toast.makeText(this, R.string.payment_toast_success, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, R.string.payment_toast_could_not_initiate, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private String checkMongikeStatus(String transactionId) {
        try {
            URL url = new URL("https://mongike.com/api/v1/payments/" + transactionId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);

            int code = conn.getResponseCode();
            android.util.Log.d("PaymentActivity", "Status response code: " + code);
            if (code >= 200 && code < 300) {
                java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                android.util.Log.d("PaymentActivity", "Status response body: " + response);
                JSONObject respJson = new JSONObject(response);
                
                // Mongike status might be in a 'status' field or inside 'data.status'
                if (respJson.has("data")) {
                    return respJson.getJSONObject("data").optString("status");
                } else {
                    return respJson.optString("status");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    private String callMongikeInitiate(String phone, String amount, String provider) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            android.util.Log.e("PaymentActivity", "MONGIKE_API_KEY is missing in local.properties");
            return null;
        }
        try {
            URL url = new URL("https://mongike.com/api/v1/payments/mobile-money/tanzania");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("order_id", "BOOKIFY-" + System.currentTimeMillis());
            json.put("amount", Integer.parseInt(amount));
            json.put("buyer_phone", phone);
            json.put("provider", provider);
            json.put("fee_payer", "MERCHANT");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            android.util.Log.d("PaymentActivity", "Status response code: " + code);
            if (code >= 200 && code < 300) {
                java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                JSONObject respJson = new JSONObject(response);
                String status = respJson.optString("status");
                if ("success".equalsIgnoreCase(status) || "ok".equalsIgnoreCase(status)) {
                    JSONObject data = respJson.optJSONObject("data");
                    if (data != null) {
                        return data.optString("id");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
