package com.example.bookify.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import com.example.bookify.EventDetailActivity;
import com.example.bookify.HomeFeedActivity;
import com.example.bookify.LoginActivity;
import com.example.bookify.R;
import com.example.bookify.RegisterActivity;
import com.example.bookify.data.Event;

/**
 * Guests can browse the home feed freely; this gates the actions that actually need
 * an account (booking/organizing) behind a login-or-register prompt instead. When the
 * prompt was triggered by tapping a specific event, that event rides along as an extra
 * so login/register can drop the user back into it instead of just the home feed.
 */
public class AuthGate {

    private AuthGate() {}

    public static final String PENDING_EVENT_EXTRA = "pending_event_extras";

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("bookify_session", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1) != -1;
    }

    /** Use when there's no specific event to return to (e.g. the "organize event" FAB). */
    public static void promptLogin(Activity activity) {
        promptLogin(activity, null);
    }

    /** Use when the prompt was triggered by tapping a specific event card. */
    public static void promptLogin(Activity activity, Event pendingEvent) {
        Bundle eventExtras = pendingEvent != null ? buildEventExtras(pendingEvent) : null;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.auth_required_title)
                .setMessage(R.string.auth_required_message)
                .setPositiveButton(R.string.log_in, (dialog, which) -> {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    if (eventExtras != null) intent.putExtra(PENDING_EVENT_EXTRA, eventExtras);
                    activity.startActivity(intent);
                })
                .setNegativeButton(R.string.create_account, (dialog, which) -> {
                    Intent intent = new Intent(activity, RegisterActivity.class);
                    if (eventExtras != null) intent.putExtra(PENDING_EVENT_EXTRA, eventExtras);
                    activity.startActivity(intent);
                })
                .setNeutralButton(R.string.action_cancel, null)
                .show();
    }

    private static Bundle buildEventExtras(Event event) {
        Bundle b = new Bundle();
        b.putString("event_id", event.getId());
        return b;
    }

    /**
     * Rebuilds the task with HomeFeedActivity at the root (so back navigation still
     * works normally) and opens the originally-tapped event on top of it.
     */
    public static void continueToPendingEvent(Activity activity, Bundle eventExtras) {
        Intent home = new Intent(activity, HomeFeedActivity.class);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(home);

        Intent detail = new Intent(activity, EventDetailActivity.class);
        detail.putExtras(eventExtras);
        activity.startActivity(detail);
    }
}
