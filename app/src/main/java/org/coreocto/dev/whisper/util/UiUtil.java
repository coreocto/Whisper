package org.coreocto.dev.whisper.util;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by John on 3/18/2018.
 */

public class UiUtil {
    public static void showToast(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showModalError(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Error")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
}
