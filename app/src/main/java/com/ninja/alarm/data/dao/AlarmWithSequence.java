package com.ninja.alarm.data.dao;

import androidx.room.ColumnInfo;

public class AlarmWithSequence {
    @ColumnInfo(name = "alarm_id")
    public long alarmId;

    @ColumnInfo(name = "alarm_time")
    public String alarmTime;

    @ColumnInfo(name = "repeat_days")
    public int repeatDays;

    @ColumnInfo(name = "label")
    public String label;

    @ColumnInfo(name = "time_limit")
    public int timeLimit;

    @ColumnInfo(name = "is_enabled")
    public int isEnabled;

    @ColumnInfo(name = "sequence_id")
    public long sequenceId;

    @ColumnInfo(name = "sequence_name")
    public String sequenceName;

    @ColumnInfo(name = "difficulty")
    public String difficulty;
}
