package com.sebastian_eggers.MediApp.Receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * ToDo Projekt Notifikationen
 *
 * ToDo: 1. Die Notification muss angekündigt werden, sobald das Medikament erstellt ist
 * ToDo: 2. Die Notificationen müssen beim Neustart alle wieder angestellt werden
 * => BootReceiver muss funktionieren
 * ToDo: 3. Die Notification muss wieder feuern nachdem sie ignoriert wurde.
 * => setRepeating
 * ToDo: 4. Die Notification muss auch ausgestellt werden können
 * => MainActivity in die ListView muss checkbox oder button sein
 * => MainActivity in ListView muss Zeit rot gehighlightet werden
 * ToDo: 5. Die Notification muss eine neue Notification einstellen, wenn sie erledigt wurde
 * => Kein Plan wie wo oder was
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            DrugDBHelper drugDBHelper = new DrugDBHelper(context);
            ArrayList<Drug> drugs = drugDBHelper.getAllDrugs();
            for(Drug drug: drugs) {
                drug.cancelNotification(context);
                drug.scheduleNotification(context);
            }
        }
    }
}
