package com.sebastian_eggers.MediApp.Util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.sebastian_eggers.MediApp.Enum.NotificationRepeat;
import com.sebastian_eggers.MediApp.R;
import com.sebastian_eggers.MediApp.Receiver.NotificationReceiver;

import java.util.Calendar;

public class NotificationUtil {
    private static int NOTIFICATION_ID = 0;

    public static Notification buildNotification(Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(NotificationReceiver.NOTIFICATION_CHANNEL_ID);
        return builder.build();
    }

    public static void scheduleNotification(Context context, Notification notification, Calendar calendar, NotificationRepeat repeating) {
        PendingIntent pendingIntent = getPendingIntent(context, notification);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        switch (repeating) {
            case DAILY:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
            case WEEKLY:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                break;
            default:
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void scheduleNotification(Context context, Notification notification, Calendar calendar) {
        scheduleNotification(context, notification, calendar, NotificationRepeat.NONE);
    }

    public static void instantNotification(Context context, Notification notification) {
        delayedNotification(context, notification, 0);
    }

    public static void delayedNotification(Context context, Notification notification, int delay) {
        PendingIntent pendingIntent = getPendingIntent(context, notification);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
    }

    private static PendingIntent getPendingIntent(Context context, Notification notification) {
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, ++NOTIFICATION_ID);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        return PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
