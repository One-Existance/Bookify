package com.example.bookify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.bookify.data.CheckInResult;
import com.example.bookify.data.DatabaseHelper;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Camera-based QR scan screen for door entry. Scans either a per-attendee ticket QR
 * (checks it in against this event, rejects duplicates/wrong-event/unpaid via
 * DatabaseHelper.checkInTicket) or an event-level invite QR (bookify://event/<code>,
 * just confirms it's a genuine code for this event - those are shared, not unique,
 * so there's no anti-duplicate protection for that path).
 */
public class ScanEntryActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 2001;
    private static final String DEEP_LINK_PREFIX = "bookify://event/";

    private DatabaseHelper db;
    private String eventId;
    private String eventAccessCode;

    private ImageView ivResultIcon;
    private TextView tvResultTitle, tvResultMessage;
    private Button btnScan;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    handleScanResult(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_entry);

        db = new DatabaseHelper(this);
        eventId = getIntent().getStringExtra("event_id");
        eventAccessCode = getIntent().getStringExtra("event_access_code");
        String eventTitle = getIntent().getStringExtra("event_title");

        ivResultIcon = findViewById(R.id.iv_result_icon);
        tvResultTitle = findViewById(R.id.tv_result_title);
        tvResultMessage = findViewById(R.id.tv_result_message);
        btnScan = findViewById(R.id.btn_scan);

        ((TextView) findViewById(R.id.tv_scan_title)).setText(
                eventTitle != null ? eventTitle : getString(R.string.scan_entry_title));
        tvResultMessage.setText(getString(R.string.scan_idle_message, eventTitle));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_done).setOnClickListener(v -> finish());
        btnScan.setOnClickListener(v -> startScan());
    }

    private void startScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }
        launchScanner();
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.scan_prompt));
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        scanLauncher.launch(options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchScanner();
            } else {
                Toast.makeText(this, R.string.scan_camera_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleScanResult(String scanned) {
        btnScan.setText(R.string.scan_button_next);

        if (scanned.startsWith(DEEP_LINK_PREFIX)) {
            String code = scanned.substring(DEEP_LINK_PREFIX.length());
            if (eventAccessCode != null && eventAccessCode.equals(code)) {
                showResult(true, R.string.scan_result_valid_invite_title,
                        getString(R.string.scan_result_valid_invite_message));
            } else {
                showResult(false, R.string.scan_result_invalid_title,
                        getString(R.string.scan_result_invalid_message));
            }
            return;
        }

        CheckInResult result = db.checkInTicket(scanned, eventId);
        switch (result.status) {
            case CHECKED_IN:
                showResult(true, R.string.scan_result_checked_in_title,
                        getString(R.string.scan_result_checked_in_message, result.attendeeName));
                break;
            case ALREADY_CHECKED_IN:
                showResult(false, R.string.scan_result_already_title,
                        getString(R.string.scan_result_already_message, result.attendeeName));
                break;
            case WRONG_EVENT:
                showResult(false, R.string.scan_result_wrong_event_title,
                        getString(R.string.scan_result_wrong_event_message, result.eventTitle));
                break;
            case UNPAID:
                showResult(false, R.string.scan_result_unpaid_title,
                        getString(R.string.scan_result_unpaid_message));
                break;
            case INVALID_TICKET:
            default:
                showResult(false, R.string.scan_result_invalid_title,
                        getString(R.string.scan_result_invalid_message));
                break;
        }
    }

    private void showResult(boolean success, int titleRes, String message) {
        ivResultIcon.setImageResource(success ? R.drawable.ic_check_circle : R.drawable.ic_warning_circle);
        ivResultIcon.setImageTintList(android.content.res.ColorStateList.valueOf(
                getColor(success ? R.color.whatsapp_green : R.color.error_red)));
        tvResultTitle.setText(titleRes);
        tvResultTitle.setVisibility(android.view.View.VISIBLE);
        tvResultMessage.setText(message);
    }
}
