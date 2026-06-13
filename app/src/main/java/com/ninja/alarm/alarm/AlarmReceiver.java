package com.ninja.alarm.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.dao.AlarmWithSequence;
import com.ninja.alarm.data.mapper.RoomMappers;
import com.ninja.alarm.model.Alarm;

/**
 * Receives scheduled alarm events and device boot events.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            new AlarmScheduler(context).scheduleAllEnabled();
            return;
        }

        if (!AlarmScheduler.ACTION_FIRE_ALARM.equals(action)) return;

        long alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L);
        if (alarmId <= 0) return;

        NinjaDatabase db = NinjaDatabase.getInstance(context);
        AlarmWithSequence row = db.alarmDao().getWithSequence(alarmId);
        Alarm alarm = RoomMappers.toModel(row);
        if (alarm == null || !alarm.enabled) return;

        Intent service = new Intent(context, AlarmService.class)
                .setAction(AlarmService.ACTION_START_ALARM)
                .putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }

        if (alarm.repeatDays == 0) {
            db.alarmDao().setEnabled(alarmId, 0, System.currentTimeMillis());
        } else {
            new AlarmScheduler(context).schedule(alarm);
        }
    }
}
