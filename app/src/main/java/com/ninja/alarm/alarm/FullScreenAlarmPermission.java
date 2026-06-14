package com.ninja.alarm.alarm;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Handles Android 14+ full-screen alarm notification access.
 */
public final class FullScreenAlarmPermission {

    private FullScreenAlarmPermission() {}

    public static boolean isRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
    }

    public static boolean canUse(Context context) {
        if (!isRequired()) return true;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        return nm != null && nm.canUseFullScreenIntent();
    }

    public static Intent settingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                .setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }
}
