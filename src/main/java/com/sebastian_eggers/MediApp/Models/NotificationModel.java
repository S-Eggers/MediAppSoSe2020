package com.sebastian_eggers.MediApp.Models;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sebastian_eggers.MediApp.Enum.NotificationRepeat;
import com.sebastian_eggers.MediApp.R;
import com.sebastian_eggers.MediApp.Receiver.NotificationReceiver;
import com.sebastian_eggers.MediApp.Util.NotificationUtil;

import java.util.Calendar;

public class NotificationModel {
    private Context context;
    private String title;
    private String content;
    private int id;
    private int stack;
    private Notification notification;
    private Calendar calendar;
    private NotificationRepeat notificationRepeat;


    public NotificationModel(Context context, String content, int id) {
        this.context = context;
        this.title = context.getResources().getString(R.string.app_name);
        this.content = context.getResources().getString(R.string.intake_notification).replace("{-}", content);
        this.id = id;
        this.stack = 0;
        this.notification = NotificationUtil.buildNotification(context, title, content);
    }

    public NotificationModel stack() {
        stack++;
        content = content.replace("ist fällig.", " und " + stack + " mehr ist fällig.");
        notification = NotificationUtil.buildNotification(context, title, content);

        return this;
    }

    public NotificationModel cancel() {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);

        return this;
    }

    public NotificationModel schedule(Calendar calendar, NotificationRepeat repeating) {
        this.calendar = calendar;
        this.notificationRepeat = repeating;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, id);
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

        return this;
    }
}
