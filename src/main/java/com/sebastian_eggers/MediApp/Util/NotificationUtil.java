package com.sebastian_eggers.MediApp.Util;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

import com.sebastian_eggers.MediApp.Activities.MainActivity;
import com.sebastian_eggers.MediApp.Activities.TodayActivity;
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
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_foreground));
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground);
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(alarmManager == null) {
            showAlert(context, R.string.failed, R.string.scheduling_failed);
            return;
        }

        long oneMinute = 0; // Half a minute would be okay, but confusing as it is often half a minute earlier: 1000 * 30;

        switch (repeating) {
            case DAILY:
                 alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - oneMinute, AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
            case WEEKLY:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - oneMinute, AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                break;
            default:
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelNotification(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager == null) {
            showAlert(context, R.string.failed, R.string.cancel_failed);
            return;
        }
        alarmManager.cancel(pendingIntent);
    }

    private static void showAlert(Context context, int title, int message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setNegativeButton(R.string.okay, null);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }
}
