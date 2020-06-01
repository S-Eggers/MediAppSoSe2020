package com.sebastian_eggers.MediApp.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DrugDataSource {
    private static final String tag = DrugDBHelper.class.getSimpleName();

    private SQLiteDatabase database;
    private DrugDBHelper helper;

    public DrugDataSource(Context context) {
        Log.d(tag, "DrugDataSource created DrugDBHelper");
        helper = new DrugDBHelper(context);
    }
}
