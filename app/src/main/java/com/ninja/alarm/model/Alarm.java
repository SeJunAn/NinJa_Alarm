package com.ninja.alarm.model;

/**
 * 알람 1건. (지시서 2.3)
 *
 * 스케줄링(AlarmManager)·저장은 BE 담당이며, UI 는 이 DTO 만 다룬다.
 */
public class Alarm {
    public long alarmId;
    public String timeHHmm;     // 'HH:mm'
    public int repeatDays;      // 7비트: bit0=월 ... bit6=일 (0 이면 반복 없음)
    public String label;
    public int timeLimitSec;    // 설정 시점 스냅샷
    public boolean enabled;
    public long sequenceId;     // 연결된 술법
    public String sequenceName; // 표시용 캐시
    public Difficulty difficulty;

    public Alarm(long alarmId, String timeHHmm, int repeatDays, String label, int timeLimitSec,
                 boolean enabled, long sequenceId, String sequenceName, Difficulty difficulty) {
        this.alarmId = alarmId;
        this.timeHHmm = timeHHmm;
        this.repeatDays = repeatDays;
        this.label = label;
        this.timeLimitSec = timeLimitSec;
        this.enabled = enabled;
        this.sequenceId = sequenceId;
        this.sequenceName = sequenceName;
        this.difficulty = difficulty;
    }
}
