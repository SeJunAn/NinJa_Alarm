package com.ninja.alarm.repository;

import com.ninja.alarm.model.UserProfile;

/**
 * 사용자 프로필 경계. (지시서 9장)
 */
public interface UserRepository {
    UserProfile getProfile(long userId);
}
