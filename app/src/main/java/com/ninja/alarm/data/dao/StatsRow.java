package com.ninja.alarm.data.dao;

import androidx.room.ColumnInfo;

public class StatsRow {
    @ColumnInfo(name = "totalAttempts")
    public int totalAttempts;

    @ColumnInfo(name = "successCount")
    public int successCount;

    @ColumnInfo(name = "failCount")
    public int failCount;

    @ColumnInfo(name = "avgDurationSec")
    public float avgDurationSec;
}
