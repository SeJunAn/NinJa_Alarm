package com.ninja.alarm.repository;

import com.ninja.alarm.repository.fake.FakeAlarmRepository;
import com.ninja.alarm.repository.fake.FakeDismissRepository;
import com.ninja.alarm.repository.fake.FakeSequenceRepository;
import com.ninja.alarm.repository.fake.FakeTutorialRepository;
import com.ninja.alarm.repository.fake.FakeUserRepository;

/**
 * 단순 서비스 로케이터 — UI/AI 가 의존하는 repository 의 단일 공급 지점.
 *
 * 개발 중에는 Fake* 구현을 반환하고, BE(김동환) 구현이 들어오면 여기 한 곳만
 * 실제 구현으로 교체하면 된다. (지시서 4·10장의 "DI 지점")
 *
 * 데모 동안 상태(추가한 알람·커스텀 술법 등)를 공유해야 하므로 프로세스 싱글턴으로 둔다.
 */
public final class Repositories {

    private static AlarmRepository alarm;
    private static SequenceRepository sequence;
    private static DismissRepository dismiss;
    private static TutorialRepository tutorial;
    private static UserRepository user;

    // 데모용 단일 사용자
    public static final long CURRENT_USER_ID = 1L;

    private Repositories() {}

    public static synchronized AlarmRepository alarm() {
        if (alarm == null) alarm = new FakeAlarmRepository();
        return alarm;
    }

    public static synchronized SequenceRepository sequence() {
        if (sequence == null) sequence = new FakeSequenceRepository(alarm());
        return sequence;
    }

    public static synchronized DismissRepository dismiss() {
        if (dismiss == null) dismiss = new FakeDismissRepository();
        return dismiss;
    }

    public static synchronized TutorialRepository tutorial() {
        if (tutorial == null) tutorial = new FakeTutorialRepository();
        return tutorial;
    }

    public static synchronized UserRepository user() {
        if (user == null) user = new FakeUserRepository();
        return user;
    }
}
