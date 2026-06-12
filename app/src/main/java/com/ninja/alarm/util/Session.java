package com.ninja.alarm.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 로그인 세션 — 현재 로그인한 user_id 를 SharedPreferences 에 보관한다.
 *
 * UI 와 Room 리포지토리가 모두 "현재 사용자"를 같은 출처에서 읽도록 하는 단일 지점이다.
 * 명시적 세션이 없으면 시드된 로컬 게스트 사용자({@link #GUEST_USER_ID})로 동작한다.
 */
public final class Session {

    private static final String FILE = "ninja_session";
    private static final String KEY_USER_ID = "current_user_id";

    /** seed 로 항상 존재하는 로컬 게스트 사용자(닉네임 '그림자'). */
    public static final long GUEST_USER_ID = 1L;

    private Session() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** 로그인/회원가입 성공 시 호출. */
    public static void setUserId(Context ctx, long userId) {
        prefs(ctx).edit().putLong(KEY_USER_ID, userId).apply();
    }

    /** 현재 사용자 id. 세션이 없으면 게스트 id 를 반환한다. */
    public static long userId(Context ctx) {
        return prefs(ctx).getLong(KEY_USER_ID, GUEST_USER_ID);
    }

    /** 명시적으로 로그인한 세션이 있는지(게스트 자동값과 구분). */
    public static boolean isLoggedIn(Context ctx) {
        return prefs(ctx).contains(KEY_USER_ID);
    }

    /** 로그아웃. */
    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY_USER_ID).apply();
    }
}
