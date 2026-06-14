package com.ninja.alarm.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 간단한 앱 설정 저장(SharedPreferences).
 * 모션 줄이기 옵션은 Phase 3 의 시그니처 모션에서 존중한다. (지시서 6·10장 접근성)
 */
public final class AppPrefs {

    private static final String FILE = "ninja_prefs";
    private static final String KEY_REDUCE_MOTION = "reduce_motion";
    private static final String KEY_ALARM_SOUND_URI = "alarm_sound_uri";
    private static final String KEY_ALARM_SOUND_TITLE = "alarm_sound_title";

    private AppPrefs() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isReduceMotion(Context ctx) {
        return prefs(ctx).getBoolean(KEY_REDUCE_MOTION, false);
    }

    public static void setReduceMotion(Context ctx, boolean enabled) {
        prefs(ctx).edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply();
    }

    public static String getAlarmSoundUri(Context ctx) {
        return prefs(ctx).getString(KEY_ALARM_SOUND_URI, null);
    }

    public static String getAlarmSoundTitle(Context ctx) {
        return prefs(ctx).getString(KEY_ALARM_SOUND_TITLE, null);
    }

    public static void setAlarmSound(Context ctx, String uri, String title) {
        prefs(ctx).edit()
                .putString(KEY_ALARM_SOUND_URI, uri)
                .putString(KEY_ALARM_SOUND_TITLE, title)
                .apply();
    }
}
