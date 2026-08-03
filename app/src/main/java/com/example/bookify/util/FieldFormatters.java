package com.example.bookify.util;

import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import android.text.TextUtils;

import java.text.ParseException;
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

    /**
     * Whether an event dated/timed like this hasn't happened yet - used to gate the edit
     * action, since editing an event after it's already taken place doesn't make sense.
     * A missing time is treated as "editable until the end of that day." Fails open
     * (treats unparseable dates as still upcoming) since this is only a client-side UX
     * gate, not a correctness guarantee - the app has no server-side validation anywhere else.
     */
    public static boolean isUpcoming(String date, String time) {
        if (TextUtils.isEmpty(date)) return true;
        try {
            if (TextUtils.isEmpty(time)) {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
                Date parsed = sdf.parse(date);
                if (parsed == null) return true;
                Calendar endOfDay = Calendar.getInstance();
                endOfDay.setTime(parsed);
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                return endOfDay.getTime().after(new Date());
            }
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN + " " + TIME_PATTERN, Locale.US);
            Date parsed = sdf.parse(date + " " + time);
            return parsed == null || parsed.after(new Date());
        } catch (ParseException e) {
            return true;
        }
    }
}
