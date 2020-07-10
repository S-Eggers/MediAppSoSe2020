package com.sebastian_eggers.MediApp.Worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;

public class NotificationCancelWorker extends Worker {
    private Context context;

    public NotificationCancelWorker(Context context, WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        DrugDBHelper drugDBHelper = new DrugDBHelper(context);
        long drugId = getInputData().getLong("drug_id", 0);
        if(drugId == 0)
            return Result.failure();

        Drug drug = drugDBHelper.getDrug(drugId);
        if(drug == null)
            return Result.failure();
        
        drug.cancelNotifications(context);
        return Result.success();
    }
}
