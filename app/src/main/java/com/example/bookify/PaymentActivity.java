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
    private String eventId;
    private int userId;
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

        eventId    = getIntent().getStringExtra("event_id");
        eventPrice = getIntent().getStringExtra("event_price");
        eventTitle = getIntent().getStringExtra("event_title");

        ((TextView) findViewById(R.id.tv_payment_amount)).setText(eventPrice);
        etPhone = findViewById(R.id.et_phone);
        progressBar = findViewById(R.id.pb_loading);
        btnPay = findViewById(R.id.btn_pay_now);

        btnPay.setOnClickListener(v -> initiatePayment());
    }

    private void initiatePayment() {
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, R.string.payment_toast_enter_phone, Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = eventPrice.replace("Tsh", "").replace(",", "").replace(" ", "").trim();
        
        btnPay.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            String transactionId = callMongikeInitiate(phone, amountStr);
            
            if (transactionId != null) {
                // Poll for status until payment is confirmed
                boolean isVerified = false;
                int attempts = 0;
                while (attempts < 20) { // Poll for ~60 seconds (3s * 20)
                    try { Thread.sleep(3000); } catch (InterruptedException e) {}
                    
                    String status = checkMongikeStatus(transactionId);
                    if ("SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                        isVerified = true;
                        break;
                    } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                        break;
                    }
                    attempts++;
                }
                
                final boolean success = isVerified;
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnPay.setEnabled(true);
                    if (success) {
                        db.completePayment(userId, eventId);
                        Toast.makeText(this, R.string.payment_toast_success, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, R.string.payment_toast_verification_failed, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnPay.setEnabled(true);
                    Toast.makeText(this, R.string.payment_toast_could_not_initiate, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String checkMongikeStatus(String transactionId) {
        try {
            URL url = new URL("https://mongike.com/api/v1/payments/" + transactionId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-api-key", apiKey);

            int code = conn.getResponseCode();
            if (code == 200) {
                java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                JSONObject respJson = new JSONObject(response);
                if (respJson.has("data")) {
                    return respJson.getJSONObject("data").optString("status");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    private String callMongikeInitiate(String phone, String amount) {
        try {
            URL url = new URL("https://mongike.com/api/v1/payments/mobile-money/tanzania");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("order_id", "BOOKIFY-" + System.currentTimeMillis());
            json.put("amount", Integer.parseInt(amount));
            json.put("buyer_phone", phone);
            json.put("fee_payer", "MERCHANT");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                JSONObject respJson = new JSONObject(response);
                if ("success".equalsIgnoreCase(respJson.optString("status"))) {
                    return respJson.getJSONObject("data").optString("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
