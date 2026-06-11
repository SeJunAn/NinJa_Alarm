package com.ninja.alarm.repository;

import android.content.Context;

import com.ninja.alarm.repository.fake.FakeAlarmRepository;
import com.ninja.alarm.repository.fake.FakeDismissRepository;
import com.ninja.alarm.repository.fake.FakeSequenceRepository;
import com.ninja.alarm.repository.fake.FakeTutorialRepository;
import com.ninja.alarm.repository.fake.FakeUserRepository;
import com.ninja.alarm.repository.room.RoomAlarmRepository;
import com.ninja.alarm.repository.room.RoomDismissRepository;
import com.ninja.alarm.repository.room.RoomSequenceRepository;
import com.ninja.alarm.repository.room.RoomTutorialRepository;
import com.ninja.alarm.repository.room.RoomUserRepository;

/**
 * 앱 전체 Repository DI 지점.
 *
 * BE 적용 후 UI 호출부는 그대로 Repositories.alarm(), sequence() 등을 사용한다.
 * Application에서 Repositories.init(appContext)를 먼저 호출해야 한다.
 */
public final class Repositories {
    public static final long CURRENT_USER_ID = 1L;
    private static Context appContext;

    private static AlarmRepository alarm;
    private static SequenceRepository sequence;
    private static DismissRepository dismiss;
    private static TutorialRepository tutorial;
    private static UserRepository user;

    private Repositories() {}

    public static synchronized void init(Context context) {
        appContext = context.getApplicationContext();

        // init이 다시 호출되면 기존 싱글턴을 초기화해서 Context 교체/테스트에 안전하게 한다.
        alarm = null;
        sequence = null;
        dismiss = null;
        tutorial = null;
        user = null;
    }

    private static Context requireContext() {
        if (appContext == null) {
            throw new IllegalStateException(
                    "Repositories.init(context)가 먼저 호출되어야 합니다. " +
                            "NinjaAlarmApp을 만들고 AndroidManifest.xml application android:name에 등록하세요."
            );
        }
        return appContext;
    }

    public static synchronized AlarmRepository alarm() {
        if (alarm == null) {
            alarm = new RoomAlarmRepository(requireContext());
            // 문제가 생기면 임시 복구:
            // alarm = new FakeAlarmRepository();
        }
        return alarm;
    }

    public static synchronized SequenceRepository sequence() {
        if (sequence == null) {
            sequence = new RoomSequenceRepository(requireContext());
            // 문제가 생기면 임시 복구:
            // sequence = new FakeSequenceRepository(alarm());
        }
        return sequence;
    }

    public static synchronized DismissRepository dismiss() {
        if (dismiss == null) {
            dismiss = new RoomDismissRepository(requireContext());
            // 문제가 생기면 임시 복구:
            // dismiss = new FakeDismissRepository();
        }
        return dismiss;
    }

    public static synchronized TutorialRepository tutorial() {
        if (tutorial == null) {
            tutorial = new RoomTutorialRepository(requireContext());
            // 문제가 생기면 임시 복구:
            // tutorial = new FakeTutorialRepository();
        }
        return tutorial;
    }

    public static synchronized UserRepository user() {
        if (user == null) {
            user = new RoomUserRepository(requireContext());
            // 문제가 생기면 임시 복구:
            // user = new FakeUserRepository();
        }
        return user;
    }
}
