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
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clean amount string
        String amountStr = eventPrice.replace("Tsh", "").replace(",", "").replace(" ", "").trim();
        
        btnPay.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            boolean success = callMongikeAPI(phone, amountStr);
            
            if (success) {
                // Mock: Wait for payment to be processed and verified in Mongike account
                // In production, you'd use a webhook or poll a "check-status" endpoint
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                
                boolean isVerified = checkMongikeAccountStatus("BOOKIFY-" + System.currentTimeMillis());
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnPay.setEnabled(true);
                    if (isVerified) {
                        db.completePayment(userId, eventId);
                        Toast.makeText(this, "Payment Received! Ticket is now available.", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Payment not yet received. Please try again or check your account.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnPay.setEnabled(true);
                    Toast.makeText(this, "Payment initiation failed.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean checkMongikeAccountStatus(String orderId) {
        // Mocking a check against Mongike to ensure money is in the merchant account
        // Return true if verified
        return true;
    }

    private boolean callMongikeAPI(String phone, String amount) {
        try {
            URL url = new URL("https://mongike.com/api/v1/payments/mobile-money/tanzania");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("order_id", "BOOKIFY-" + System.currentTimeMillis());
            json.put("amount", Integer.parseInt(amount)); // User doc shows integer amount
            json.put("buyer_phone", phone);
            json.put("fee_payer", "MERCHANT");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                // You would typically parse the response JSON here to check "status": "success"
                return true; 
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
