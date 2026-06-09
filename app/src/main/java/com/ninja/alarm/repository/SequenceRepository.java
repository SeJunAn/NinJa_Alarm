package com.ninja.alarm.repository;

import com.ninja.alarm.model.DismissPlan;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.Sequence;

import java.util.List;

/**
 * 술법·인 데이터 경계. (지시서 9장)
 */
public interface SequenceRepository {

    /** 해제 화면이 알람 1건의 해제 계획을 받아온다(술법명·난이도·time_limit·순서대로의 인). */
    DismissPlan getDismissPlan(long alarmId);

    /** 술법 목록. includeCustom=false 면 프리셋만. */
    List<Sequence> getSequences(boolean includeCustom);

    Sequence getSequence(long sequenceId);

    /** 커스텀 술법 저장. 확정된 sequenceId 반환. */
    long saveCustomSequence(Sequence seq, List<Integer> orderedSealIds);

    /** 12종 인 전체. */
    List<Seal> getSeals();
}
