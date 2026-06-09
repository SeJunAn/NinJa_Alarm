package com.ninja.alarm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 술법(인 시퀀스). (지시서 2.3)
 *
 * orderedSealIds 가 인식의 핵심 — 인을 맺는 순서(step_order)를 담는다.
 */
public class Sequence {
    public final long sequenceId;
    public final String name;       // 한글 술법명
    public final String nameEn;     // 영문 술법명
    public final Difficulty difficulty;
    public final boolean isCustom;
    public final List<Integer> orderedSealIds; // 순서대로의 seal_id

    public Sequence(long sequenceId, String name, String nameEn, Difficulty difficulty,
                    boolean isCustom, List<Integer> orderedSealIds) {
        this.sequenceId = sequenceId;
        this.name = name;
        this.nameEn = nameEn;
        this.difficulty = difficulty;
        this.isCustom = isCustom;
        this.orderedSealIds = new ArrayList<>(orderedSealIds);
    }

    public int sealCount() {
        return orderedSealIds.size();
    }
}
