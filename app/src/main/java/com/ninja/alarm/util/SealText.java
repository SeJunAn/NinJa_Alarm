package com.ninja.alarm.util;

import com.ninja.alarm.model.Seal;
import com.ninja.alarm.repository.fake.SealData;

import java.util.List;

/**
 * 인 시퀀스 표시용 텍스트 유틸.
 */
public final class SealText {

    private SealText() {}

    /** sealId 목록 → "未 巳 寅" 처럼 12간지 한자를 공백으로 이은 문자열. */
    public static String zodiac(List<Integer> sealIds) {
        StringBuilder sb = new StringBuilder();
        for (int id : sealIds) {
            Seal s = SealData.byId(id);
            if (s == null) continue;
            if (sb.length() > 0) sb.append("  ");
            sb.append(s.zodiac);
        }
        return sb.toString();
    }

    /** sealId 목록 → "① 未  ② 巳  ③ 寅" 처럼 번호가 붙은 순서 문자열. */
    public static String numberedZodiac(List<Integer> sealIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sealIds.size(); i++) {
            Seal s = SealData.byId(sealIds.get(i));
            if (s == null) continue;
            if (sb.length() > 0) sb.append("   ");
            sb.append(circled(i + 1)).append(' ').append(s.zodiac);
        }
        return sb.toString();
    }

    /** 1→①, … 20까지. 그 이상은 (n). */
    public static String circled(int n) {
        if (n >= 1 && n <= 20) return String.valueOf((char) (0x2460 + n - 1));
        return "(" + n + ")";
    }
}
