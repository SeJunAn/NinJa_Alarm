package com.ninja.alarm.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ninja.alarm.data.entity.UserEntity;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    UserEntity getUser(long userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getByEmail(String email);

    @Query("SELECT * FROM users ORDER BY user_id ASC LIMIT 1")
    UserEntity getFirstUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    /** 회원가입 전용 — 이메일 unique 충돌 시 예외(REPLACE 로 기존 계정을 덮지 않도록 ABORT). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertNew(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("UPDATE users SET exp = :exp, current_level = :currentLevel, title = :title, updated_at = :updatedAt WHERE user_id = :userId")
    void updateGrowth(long userId, int exp, int currentLevel, String title, long updatedAt);
}
