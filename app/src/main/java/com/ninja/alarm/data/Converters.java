package com.ninja.alarm.data;

import androidx.room.TypeConverter;

import com.ninja.alarm.model.Difficulty;

public class Converters {
    @TypeConverter
    public String difficultyToString(Difficulty difficulty) {
        return difficulty == null ? Difficulty.HAGEUP.name() : difficulty.name();
    }

    @TypeConverter
    public Difficulty stringToDifficulty(String value) {
        if (value == null) return Difficulty.HAGEUP;
        try {
            return Difficulty.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return Difficulty.HAGEUP;
        }
    }
}
