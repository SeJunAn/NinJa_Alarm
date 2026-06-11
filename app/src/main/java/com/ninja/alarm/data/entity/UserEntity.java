package com.ninja.alarm.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = "email", unique = true)
        }
)
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public long userId;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @Nullable
    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @Nullable
    @ColumnInfo(name = "password_salt")
    public String passwordSalt;

    @ColumnInfo(name = "exp")
    public int exp;

    @ColumnInfo(name = "current_level")
    public int currentLevel;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public UserEntity(long userId, String email, String nickname, @Nullable String passwordHash,
                      @Nullable String passwordSalt, int exp, int currentLevel, String title,
                      long createdAt, long updatedAt) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.exp = exp;
        this.currentLevel = currentLevel;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
