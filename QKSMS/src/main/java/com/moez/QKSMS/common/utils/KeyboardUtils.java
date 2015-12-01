package com.moez.QKSMS.common.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtils {
    public static final String TAG = "KeyboardUtils";

    /**
     * Hides the keyboard. Note that both the context and the view must be non-null.
     * @param context
     * @param view used to getConversation the window token
     */
    public static void hide(Context context, View view) {
        if (context != null && view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            Log.w(TAG, "hide called with null parameter: " + context + " " + view);
        }
    }

    /**
     * Hides the keyboard. Note that both the context and the view must be non-null.
     * @param context
     * @param view used to getConversation the window token
     */
    public static void hide(Activity context) {
        hide(context, context.getCurrentFocus());
    }

    /**
     * Shows the keyboard.
     * @param context
     */
    public static void show(Context context) {
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        } else {
            Log.w(TAG, "show called with null context: " + context);
        }
    }

    /**
     * Shows the keyboard and attempts to set the focus on the given view.
     * @param context
     * @param view
     */
    public static void showAndFocus(Context context, View view) {
        show(context);
        if (view != null) {
            view.requestFocus();
        }
    }
}
