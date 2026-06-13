package com.ninja.alarm.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.entity.AlarmEntity;
import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.ui.dismiss.DismissActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Bridges saved alarm rows to Android AlarmManager.
 */
public class AlarmScheduler {
    public static final String ACTION_FIRE_ALARM = "com.ninja.alarm.action.FIRE_ALARM";
    public static final String EXTRA_ALARM_ID = "extra_alarm_id";

    private final Context appContext;
    private final AlarmManager alarmManager;

    public AlarmScheduler(Context context) {
        this.appContext = context.getApplicationContext();
        this.alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedule(Alarm alarm) {
        if (alarm == null || alarm.alarmId <= 0 || !alarm.enabled) return;
        long triggerAt = nextTriggerAt(alarm.timeHHmm, alarm.repeatDays);
        schedule(alarm.alarmId, alarm.timeHHmm, alarm.repeatDays, triggerAt);
    }

    public void scheduleEntity(AlarmEntity alarm) {
        if (alarm == null || alarm.alarmId <= 0 || alarm.isEnabled != 1) return;
        long triggerAt = nextTriggerAt(alarm.alarmTime, alarm.repeatDays);
        schedule(alarm.alarmId, alarm.alarmTime, alarm.repeatDays, triggerAt);
    }

    public void scheduleAllEnabled() {
        List<AlarmEntity> alarms = NinjaDatabase.getInstance(appContext).alarmDao().getEnabledAlarms();
        for (AlarmEntity alarm : alarms) {
            scheduleEntity(alarm);
        }
    }

    public void cancel(long alarmId) {
        if (alarmId <= 0 || alarmManager == null) return;
        alarmManager.cancel(operationIntent(alarmId));
    }

    private void schedule(long alarmId, String timeHHmm, int repeatDays, long triggerAt) {
        if (alarmManager == null) return;

        PendingIntent operation = operationIntent(alarmId);
        PendingIntent showIntent = showIntent(alarmId);
        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(triggerAt, showIntent);

        alarmManager.cancel(operation);
        alarmManager.setAlarmClock(info, operation);
    }

    private PendingIntent operationIntent(long alarmId) {
        Intent intent = new Intent(appContext, AlarmReceiver.class)
                .setAction(ACTION_FIRE_ALARM)
                .putExtra(EXTRA_ALARM_ID, alarmId);
        return PendingIntent.getBroadcast(
                appContext,
                requestCode(alarmId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent showIntent(long alarmId) {
        Intent intent = new Intent(appContext, DismissActivity.class)
                .putExtra(DismissActivity.EXTRA_ALARM_ID, alarmId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                appContext,
                requestCode(alarmId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static int requestCode(long alarmId) {
        return (int) (alarmId & 0x7FFFFFFF);
    }

    static long nextTriggerAt(String timeHHmm, int repeatDays) {
        String[] hm = safeTime(timeHHmm).split(":");
        int hour = Integer.parseInt(hm[0]);
        int minute = Integer.parseInt(hm[1]);

        Calendar now = Calendar.getInstance();
        Calendar candidate = Calendar.getInstance();
        candidate.set(Calendar.HOUR_OF_DAY, hour);
        candidate.set(Calendar.MINUTE, minute);
        candidate.set(Calendar.SECOND, 0);
        candidate.set(Calendar.MILLISECOND, 0);

        if (repeatDays == 0) {
            if (!candidate.after(now)) {
                candidate.add(Calendar.DAY_OF_YEAR, 1);
            }
            return candidate.getTimeInMillis();
        }

        for (int offset = 0; offset <= 7; offset++) {
            Calendar next = (Calendar) candidate.clone();
            next.add(Calendar.DAY_OF_YEAR, offset);
            if (next.after(now) && isRepeatDay(repeatDays, next)) {
                return next.getTimeInMillis();
            }
        }

        if (!candidate.after(now)) {
            candidate.add(Calendar.DAY_OF_YEAR, 1);
        }
        return candidate.getTimeInMillis();
    }

    private static boolean isRepeatDay(int repeatDays, Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int bitIndex;
        if (day == Calendar.SUNDAY) {
            bitIndex = 6;
        } else {
            bitIndex = day - Calendar.MONDAY;
        }
        return (repeatDays & (1 << bitIndex)) != 0;
    }

    private static String safeTime(String timeHHmm) {
        if (timeHHmm == null || !timeHHmm.matches("\\d{2}:\\d{2}")) {
            return "07:00";
        }
        return String.format(Locale.US, "%s", timeHHmm);
    }
}
