package com.example.bookify.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import com.example.bookify.LoginActivity;
import com.example.bookify.R;
import com.example.bookify.RegisterActivity;

/**
 * Guests can browse the home feed freely; this gates the actions that actually need
 * an account (booking/organizing) behind a login-or-register prompt instead.
 */
public class AuthGate {

    private AuthGate() {}

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("bookify_session", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1) != -1;
    }

    public static void promptLogin(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.auth_required_title)
                .setMessage(R.string.auth_required_message)
                .setPositiveButton(R.string.log_in, (dialog, which) ->
                        activity.startActivity(new Intent(activity, LoginActivity.class)))
                .setNegativeButton(R.string.create_account, (dialog, which) ->
                        activity.startActivity(new Intent(activity, RegisterActivity.class)))
                .setNeutralButton(R.string.action_cancel, null)
                .show();
    }
}
