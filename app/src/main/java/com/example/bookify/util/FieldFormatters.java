package com.example.bookify.util;

import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Event-creation forms used free-text date/time/price fields with only a hint to show
 * the expected format (which then got truncated in the UI anyway). This attaches real
 * pickers instead, and formats every field consistently with the rest of the app
 * ("Jul 25, 2025", "7:00 PM", "Tsh 15,000").
 */
public class FieldFormatters {

    private static final String DATE_PATTERN = "MMM d, yyyy";
    private static final String TIME_PATTERN = "h:mm a";

    private FieldFormatters() {}

    /** Makes the field open a date picker on tap instead of the keyboard. */
    public static void attachDatePicker(AppCompatActivity activity, EditText field) {
        makeReadOnly(field);
        field.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                field.setText(sdf.format(new Date(selection)));
            });
            picker.show(activity.getSupportFragmentManager(), "date_picker");
        });
    }

    /** Makes the field open a time picker on tap instead of the keyboard. */
    public static void attachTimePicker(AppCompatActivity activity, EditText field) {
        makeReadOnly(field);
        field.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(now.get(Calendar.HOUR_OF_DAY))
                    .setMinute(now.get(Calendar.MINUTE))
                    .build();
            picker.addOnPositiveButtonClickListener(v2 -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, picker.getHour());
                cal.set(Calendar.MINUTE, picker.getMinute());
                SimpleDateFormat sdf = new SimpleDateFormat(TIME_PATTERN, Locale.US);
                field.setText(sdf.format(cal.getTime()));
            });
            picker.show(activity.getSupportFragmentManager(), "time_picker");
        });
    }

    private static void makeReadOnly(EditText field) {
        field.setFocusable(false);
        field.setFocusableInTouchMode(false);
        field.setLongClickable(false);
        field.setClickable(true);
    }

    /**
     * Formats a raw digits-only price (e.g. "15000") as "Tsh 15,000". Anything that
     * isn't a plain number (already formatted, or free text) is returned unchanged.
     */
    public static String formatPrice(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || !digits.equals(trimmed.replaceAll("[,\\s]", ""))) {
            return trimmed;
        }
        try {
            long value = Long.parseLong(digits);
            return "Tsh " + String.format(Locale.US, "%,d", value);
        } catch (NumberFormatException e) {
            return trimmed;
        }
    }
}
