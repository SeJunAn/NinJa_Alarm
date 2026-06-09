package com.ninja.alarm.repository.fake;

import com.ninja.alarm.model.Seal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 12종 인(印) 시드 데이터 — labels.csv 의 12간지(1~12)에 대응. (지시서 2.3)
 *
 * sealId = labels.csv 줄 번호와 동일.
 * 손모양 가이드 이미지는 아직 없어 imageUri=null (튜토리얼에서 자리표시자로 처리).
 */
public final class SealData {

    private SealData() {}

    // {한글명, 12간지 한자}
    private static final String[][] DEF = {
            {"쥐 · Ne",     "子"},
            {"소 · Ushi",   "丑"},
            {"호랑이 · Tora", "寅"},
            {"토끼 · U",     "卯"},
            {"용 · Tatsu",  "辰"},
            {"뱀 · Mi",     "巳"},
            {"말 · Uma",    "午"},
            {"양 · Hitsuji", "未"},
            {"원숭이 · Saru", "申"},
            {"닭 · Tori",   "酉"},
            {"개 · Inu",    "戌"},
            {"돼지 · I",     "亥"},
    };

    /** 12종 인 전체(displayOrder 순). */
    public static List<Seal> all() {
        List<Seal> seals = new ArrayList<>(DEF.length);
        for (int i = 0; i < DEF.length; i++) {
            int sealId = i + 1; // labels.csv 1~12
            seals.add(new Seal(sealId, DEF[i][0], DEF[i][1], sealId, null));
        }
        return seals;
    }

    /** sealId → Seal (없으면 null). */
    public static Seal byId(int sealId) {
        if (sealId >= 1 && sealId <= DEF.length) {
            return new Seal(sealId, DEF[sealId - 1][0], DEF[sealId - 1][1], sealId, null);
        }
        return null;
    }

    public static List<Seal> unmodifiableAll() {
        return Collections.unmodifiableList(all());
    }
}
