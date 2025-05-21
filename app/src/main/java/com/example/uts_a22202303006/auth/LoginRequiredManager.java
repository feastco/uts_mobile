package com.example.uts_a22202303006.auth;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.uts_a22202303006.R;

public class LoginRequiredManager {
    /**
     * Checks if user is logged in with full account (not guest)
     */
    public static boolean isFullyLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_session", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String role = sharedPreferences.getString("role", "");

        // Return true only if user is logged in AND not a guest
        return !username.isEmpty() && !role.equals("guest");
    }

    /**
     * Show custom login required dialog using our XML layout
     */
    public static void showLoginRequiredDialog(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_login_required);

        Button btnLogin = dialog.findViewById(R.id.btnLogin);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Update this existing code block with your new implementation
        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();

            // Clear current login session first
            SharedPreferences sharedPreferences = context.getSharedPreferences("login_session", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();  // This will remove all stored preferences including isLoggedIn
            editor.apply();

            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Set dialog width to match parent
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
