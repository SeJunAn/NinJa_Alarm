package com.ninja.alarm.repository.fake;

import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.model.Difficulty;
import com.ninja.alarm.repository.AlarmRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 메모리 기반 Fake 알람 저장소. 앱 실행 중에만 유지된다(영속화 없음).
 *
 * TODO(BE): Room 기반 AlarmRepository 로 교체. 스케줄링은 AlarmManager(BE).
 */
public class FakeAlarmRepository implements AlarmRepository {

    private final List<Alarm> alarms = new ArrayList<>();
    private long nextId = 1;

    public FakeAlarmRepository() {
        seed();
    }

    private void seed() {
        // 평일(월~금) 07:00 — 분신술(하급, 3인 × 3.0초 = 9초)
        alarms.add(new Alarm(nextId++, "07:00", 0b0011111, "기상", 9,
                true, 1, "분신술", Difficulty.HAGEUP));
        // 주말(토·일) 09:30 — 호화구술(상급, 6인 × 1.5초 = 9초)
        alarms.add(new Alarm(nextId++, "09:30", 0b1100000, "주말 늦잠 방지", 9,
                false, 5, "호화구술", Difficulty.SANGGEUP));
    }

    @Override
    public List<Alarm> getAlarms() {
        return new ArrayList<>(alarms);
    }

    @Override
    public long upsert(Alarm alarm) {
        if (alarm.alarmId <= 0) {
            alarm.alarmId = nextId++;
            alarms.add(alarm);
        } else {
            for (int i = 0; i < alarms.size(); i++) {
                if (alarms.get(i).alarmId == alarm.alarmId) {
                    alarms.set(i, alarm);
                    return alarm.alarmId;
                }
            }
            alarms.add(alarm); // 없으면 신규로 추가
        }
        return alarm.alarmId;
    }

    @Override
    public void setEnabled(long alarmId, boolean enabled) {
        for (Alarm a : alarms) {
            if (a.alarmId == alarmId) {
                a.enabled = enabled;
                return;
            }
        }
    }

    @Override
    public void delete(long alarmId) {
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).alarmId == alarmId) {
                alarms.remove(i);
                return;
            }
        }
    }
}
