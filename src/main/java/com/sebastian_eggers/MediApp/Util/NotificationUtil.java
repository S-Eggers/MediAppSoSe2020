package com.sebastian_eggers.MediApp.Util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.sebastian_eggers.MediApp.Activities.TodayActivity;
import com.sebastian_eggers.MediApp.Enum.NotificationRepeat;
import com.sebastian_eggers.MediApp.R;
import com.sebastian_eggers.MediApp.Receiver.NotificationReceiver;

import java.util.Calendar;

public class NotificationUtil {
    private static int NOTIFICATION_ID = 0;

    // ToDo: Notifications mergen und speichern, damit man die zurücksetzen kann.
    public static Notification buildNotification(Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(NotificationReceiver.NOTIFICATION_CHANNEL_ID);

        Intent notificationIntent = new Intent(context, TodayActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    public static void scheduleNotification(Context context, Notification notification, Calendar calendar, NotificationRepeat repeating) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, ++NOTIFICATION_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

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
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void mergeNotification(Context context, Notification notification) {

    }

    public static boolean isMergeAble(Notification notification1, Notification notification2) {
        return true;
    }

    public static void cancelNotification(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }
}
