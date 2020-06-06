package com.sebastian_eggers.MediApp.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;

import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // ToDo: TESTEN!
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            DrugDBHelper drugDBHelper = new DrugDBHelper(context);
            ArrayList<Drug> drugs = drugDBHelper.getAllDrugs();
            for(Drug drug: drugs) {
                drug.scheduleNotification(context);
            }
        }
    }
}
