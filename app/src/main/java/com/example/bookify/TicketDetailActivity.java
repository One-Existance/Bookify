package com.example.bookify;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class TicketDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        String title = getIntent().getStringExtra("event_title");
        String info  = getIntent().getStringExtra("event_info");
        String ticket = getIntent().getStringExtra("ticket_number");
        String image  = getIntent().getStringExtra("event_image");

        ((TextView) findViewById(R.id.tv_detail_title)).setText(title);
        ((TextView) findViewById(R.id.tv_detail_info)).setText(info);
        ((TextView) findViewById(R.id.tv_detail_ticket_no)).setText(ticket);

        if (image != null && !image.isEmpty()) {
            android.widget.ImageView ivEvent = findViewById(R.id.iv_event_image);
            if (ivEvent != null) {
                ivEvent.setVisibility(android.view.View.VISIBLE);
                Glide.with(this).load(image).into(ivEvent);
            }
        }

        ImageView ivQr = findViewById(R.id.iv_detail_qr);
        if (ticket != null) {
            try {
                Bitmap bitmap = generateQRCode(ticket);
                ivQr.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private Bitmap generateQRCode(String text) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 500, 500);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
