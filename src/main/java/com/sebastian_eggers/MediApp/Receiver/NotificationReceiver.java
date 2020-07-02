package com.sebastian_eggers.MediApp.Receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_ID = "notification-id";
    public static final String NOTIFICATION = "notification";
    public static final String NOTIFICATION_CHANNEL_NAME = "MED_APP_NOTIFICATION_CHANNEL";
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);
    }
}
