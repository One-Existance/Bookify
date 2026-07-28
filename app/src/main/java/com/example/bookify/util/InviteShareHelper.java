package com.example.bookify.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.example.bookify.R;
import com.example.bookify.data.Event;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Builds and shares an event's invite message together with a scannable entry QR
 * (encoding its bookify://event/<access_code> deep link), either through the generic
 * share sheet or directly through WhatsApp. Used for any organizer's PUBLISHED event
 * (public or private) that has an access_code, so door staff can scan a guest's phone
 * to pull the event up in Bookify and confirm entry.
 */
public class InviteShareHelper {

    private InviteShareHelper() {}

    public static void shareGeneric(Activity activity, Event event) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, buildInviteMessage(event));
        applyQrOrText(intent, generateQrUri(activity, event));
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_invite_chooser_title)));
    }

    public static void shareViaWhatsApp(Activity activity, Event event) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, buildInviteMessage(event));
        applyQrOrText(intent, generateQrUri(activity, event));

        intent.setPackage("com.whatsapp");
        if (tryStart(activity, intent)) return;

        intent.setPackage("com.whatsapp.w4b");
        if (tryStart(activity, intent)) return;

        Toast.makeText(activity, R.string.whatsapp_not_installed, Toast.LENGTH_LONG).show();
    }

    private static void applyQrOrText(Intent intent, Uri qrUri) {
        if (qrUri != null) {
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, qrUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setType("text/plain");
        }
    }

    private static boolean tryStart(Activity activity, Intent intent) {
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    private static String buildInviteMessage(Event event) {
        return "You're invited to " + event.getTitle() + "! 🎉\n"
                + "📅 " + event.getDate() + (event.getTime() != null && !event.getTime().isEmpty() ? " · " + event.getTime() : "") + "\n"
                + "📍 " + event.getLocation() + "\n\n"
                + "Show the attached QR code at the door to confirm entry, or tap to view the event:\n"
                + "bookify://event/" + event.getAccessCode() + "\n\n"
                + "Don't have Bookify installed? Enter this code in the app's Private Event screen instead:\n"
                + event.getAccessCode();
    }

    private static Uri generateQrUri(Activity activity, Event event) {
        if (event.getAccessCode() == null) return null;
        try {
            Bitmap bitmap = generateQrBitmap("bookify://event/" + event.getAccessCode());
            File dir = new File(activity.getCacheDir(), "qr_codes");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "event_" + event.getId() + ".png");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            return FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap generateQrBitmap(String text) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 600, 600);
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
