package com.sebastian_eggers.MediApp.Receiver;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sebastian_eggers.MediApp.Enum.NotificationRepeat;
import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.R;
import com.sebastian_eggers.MediApp.Util.NotificationUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            DrugDBHelper drugDBHelper = new DrugDBHelper(context);
            ArrayList<Drug> drugs = drugDBHelper.getAllDrugs();
            boolean intakeToday = false;
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            for(Drug drug: drugs) {
                if (drug.getDays().contains(today))
                    intakeToday = true;

                drug.scheduleNotification(context);
            }
            if(intakeToday) {
                Notification notification = NotificationUtil.buildNotification(context,context.getResources().getString(R.string.app_name),
                        context.getResources().getString(R.string.intake_boot_notification));
                NotificationUtil.scheduleNotification(context, notification, Calendar.getInstance(), NotificationRepeat.NONE);
            }
        }
    }
}
