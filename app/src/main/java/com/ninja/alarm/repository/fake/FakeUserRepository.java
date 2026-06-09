package com.ninja.alarm.repository.fake;

import com.ninja.alarm.model.UserProfile;
import com.ninja.alarm.repository.UserRepository;

/**
 * 메모리 기반 Fake 사용자 저장소.
 * TODO(BE): 실제 UserRepository 로 교체.
 */
public class FakeUserRepository implements UserRepository {

    @Override
    public UserProfile getProfile(long userId) {
        // 칭호: 하급닌자→중급닌자→상급닌자→카게급
        return new UserProfile("그림자", 1340, 7, "중급닌자", 340, 500);
    }
}
