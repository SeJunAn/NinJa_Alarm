package com.ninja.alarm.util;

/**
 * 반복 요일(7비트) 표시 유틸. bit0=월 ... bit6=일.
 */
public final class DayFormat {

    private static final String[] DAYS = {"월", "화", "수", "목", "금", "토", "일"};
    private static final int ALL = 0b1111111;       // 매일
    private static final int WEEKDAYS = 0b0011111;  // 월~금
    private static final int WEEKEND = 0b1100000;   // 토·일

    private DayFormat() {}

    /** 7비트 → 사람이 읽는 요일 문자열. */
    public static String format(int repeatDays) {
        int bits = repeatDays & ALL;
        if (bits == 0) return "한 번";
        if (bits == ALL) return "매일";
        if (bits == WEEKDAYS) return "주중";
        if (bits == WEEKEND) return "주말";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DAYS.length; i++) {
            if ((bits & (1 << i)) != 0) {
                if (sb.length() > 0) sb.append("·");
                sb.append(DAYS[i]);
            }
        }
        return sb.toString();
    }

    public static String dayLabel(int index) {
        return DAYS[index];
    }

    public static boolean isSet(int repeatDays, int index) {
        return (repeatDays & (1 << index)) != 0;
    }

    public static int toggle(int repeatDays, int index) {
        return repeatDays ^ (1 << index);
    }
}
