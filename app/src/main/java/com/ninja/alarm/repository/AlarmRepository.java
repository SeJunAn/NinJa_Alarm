package com.ninja.alarm.repository;

import com.ninja.alarm.model.Alarm;

import java.util.List;

/**
 * 알람 CRUD 경계. (지시서 9장)
 * 실제 저장/스케줄링은 BE 담당이며 UI 는 이 인터페이스에만 의존한다.
 */
public interface AlarmRepository {
    List<Alarm> getAlarms();

    /** 신규(alarmId<=0)면 추가, 아니면 수정. 확정된 alarmId 를 반환. */
    long upsert(Alarm alarm);

    void setEnabled(long alarmId, boolean enabled);

    void delete(long alarmId);
}
