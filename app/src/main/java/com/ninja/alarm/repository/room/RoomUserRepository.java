package com.ninja.alarm.repository.room;

import android.content.Context;

import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.entity.UserEntity;
import com.ninja.alarm.data.mapper.RoomMappers;
import com.ninja.alarm.model.UserProfile;
import com.ninja.alarm.repository.UserRepository;

public class RoomUserRepository implements UserRepository {
    private final NinjaDatabase db;

    public RoomUserRepository(Context context) {
        this.db = NinjaDatabase.getInstance(context);
    }

    @Override
    public UserProfile getProfile(long userId) {
        UserEntity user = db.userDao().getUser(userId);
        if (user == null) user = db.userDao().getFirstUser();
        return RoomMappers.toProfile(user);
    }
}
