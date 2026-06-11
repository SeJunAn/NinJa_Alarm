package com.ninja.alarm;

import android.app.Application;

import com.ninja.alarm.repository.Repositories;

public class NinjaAlarmApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Repositories.init(this);
    }
}
