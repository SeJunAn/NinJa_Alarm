package com.ninja.alarm.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;

import com.ninja.alarm.R;
import com.ninja.alarm.repository.room.RoomDismissRepository;
import com.ninja.alarm.ui.dismiss.DismissActivity;
import com.ninja.alarm.util.AppPrefs;

/**
 * Foreground alarm player. It runs until DismissActivity records a successful dismiss.
 */
public class AlarmService extends Service {
    public static final String ACTION_START_ALARM = "com.ninja.alarm.action.START_ALARM";

    private static final String CHANNEL_ID = "ninja_alarm_ringing";
    private static final int NOTIFICATION_ID = 1201;

    private MediaPlayer player;
    private Vibrator vibrator;

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && RoomDismissRepository.ACTION_STOP_ALARM.equals(intent.getAction())) {
                stopAlarm();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        registerStopReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && RoomDismissRepository.ACTION_STOP_ALARM.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        if (intent != null && ACTION_START_ALARM.equals(intent.getAction())) {
            long alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L);
            startForeground(NOTIFICATION_ID, buildNotification(alarmId));
            startSound();
            startVibration();
            openDismissScreen(alarmId);
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterStopReceiver();
        releaseSound();
        stopVibration();
        super.onDestroy();
    }

    private Notification buildNotification(long alarmId) {
        Intent dismiss = dismissIntent(alarmId);
        PendingIntent dismissIntent = PendingIntent.getActivity(
                this,
                (int) (alarmId & 0x7FFFFFFF),
                dismiss,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return builder
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(getString(R.string.alarm_ringing_title))
                .setContentText(getString(R.string.alarm_ringing_body))
                .setContentIntent(dismissIntent)
                .setFullScreenIntent(dismissIntent, true)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
    }

    private Intent dismissIntent(long alarmId) {
        return new Intent(this, DismissActivity.class)
                .putExtra(DismissActivity.EXTRA_ALARM_ID, alarmId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    private void openDismissScreen(long alarmId) {
        try {
            startActivity(dismissIntent(alarmId));
        } catch (Exception ignored) {
            // Full-screen notification remains as fallback if background launch is restricted.
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(getString(R.string.alarm_channel_desc));
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(channel);
    }

    private void registerStopReceiver() {
        IntentFilter filter = new IntentFilter(RoomDismissRepository.ACTION_STOP_ALARM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stopReceiver, filter);
        }
    }

    private void unregisterStopReceiver() {
        try {
            unregisterReceiver(stopReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void startSound() {
        if (player != null && player.isPlaying()) return;
        Uri alarmUri = resolveAlarmUri();
        if (alarmUri == null) return;

        try {
            player = new MediaPlayer();
            player.setDataSource(this, alarmUri);
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception ignored) {
            releaseSound();
            startDefaultSoundFallback();
        }
    }

    private Uri resolveAlarmUri() {
        String saved = AppPrefs.getAlarmSoundUri(this);
        if (saved != null && !saved.trim().isEmpty()) {
            return Uri.parse(saved);
        }

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return alarmUri;
    }

    private void startDefaultSoundFallback() {
        Uri fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (fallback == null) fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (fallback == null) return;

        try {
            player = new MediaPlayer();
            player.setDataSource(this, fallback);
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception ignored) {
            releaseSound();
        }
    }

    private void releaseSound() {
        if (player == null) return;
        try {
            if (player.isPlaying()) player.stop();
        } catch (IllegalStateException ignored) {
        }
        player.release();
        player = null;
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;

        long[] pattern = {0, 700, 350, 700, 700};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            vibrator.vibrate(pattern, 0);
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    private void stopAlarm() {
        releaseSound();
        stopVibration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        stopSelf();
    }
}
