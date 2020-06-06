package com.sebastian_eggers.MediApp.Helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.Enum.DrugForm;
import com.sebastian_eggers.MediApp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

public class DrugDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "drugs.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_DRUGS = "tbl_drugs";
    public static final String TABLE_TIME = "tbl_time";
    public static final String TABLE_WEEKDAYS = "tbl_days";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DOSE_PER_INTAKE = "dose_amount";
    public static final String COLUMN_DOSE_FORM = "dose_form";
    public static final String COLUMN_DOSE_UNIT = "dose_unit";

    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_SECONDS = "seconds";
    public static final String COLUMN_DRUG_ID = "drug_id";

    public static final String COLUMN_DAY = "day";

    public static final String SQL_CREATE_DRUG = "CREATE TABLE " + TABLE_DRUGS +
            "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT," +
            COLUMN_DOSE_PER_INTAKE + " INTEGER NOT NULL, " +
            COLUMN_DOSE_FORM + " TEXT NOT NULL, " +
            COLUMN_DOSE_UNIT + " TEXT);";

    public static final String SQL_CREATE_TIME = "CREATE TABLE " + TABLE_TIME +
            "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_HOUR + " INTEGER NOT NULL, " +
            COLUMN_MINUTE + " INTEGER NOT NULL, " +
            COLUMN_SECONDS + " INTEGER NOT NULL, " +
            COLUMN_DRUG_ID + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + COLUMN_DRUG_ID + ") REFERENCES " + TABLE_DRUGS + "(" + COLUMN_ID + "));";

    public static final String SQL_CREATE_WEEKDAYS = "CREATE TABLE " + TABLE_WEEKDAYS +
            "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DAY + " INTEGER NOT NULL, " +
            COLUMN_DRUG_ID + " INTEGER NOT NULL," +
            "FOREIGN KEY(" + COLUMN_DRUG_ID + ") REFERENCES " + TABLE_DRUGS + "(" + COLUMN_ID + "));";


    private static final String tag = DrugDBHelper.class.getSimpleName();
    private Context context;

    public DrugDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        Log.d(tag, "DrugDBHelper created database " + getDatabaseName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(tag, "Table created with following SQL: " + SQL_CREATE_DRUG);
            db.execSQL(SQL_CREATE_DRUG);
            Log.d(tag, "Table created with following SQL: " + SQL_CREATE_TIME);
            db.execSQL(SQL_CREATE_TIME);
            Log.d(tag, "Table created with following SQL: " + SQL_CREATE_WEEKDAYS);
            db.execSQL(SQL_CREATE_WEEKDAYS);

        } catch (Exception ex) {
            Log.e(tag, "Error creating table. " + ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRUGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEEKDAYS);

        onCreate(db);
    }

    public int getDrugsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DRUGS, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public ArrayList<Drug> getAllDrugs() {
        ArrayList<Drug> drugs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DRUGS, null);

        if (cursor.moveToFirst()) {
            do {
                drugs.add(new Drug(
                        cursor.getString(1),
                        this.getTimesForDrug(cursor.getInt(0)),
                        this.getDaysOfWeek(cursor.getInt(0)),
                        cursor.getInt(3),
                        DrugForm.valueOf(cursor.getString(4).toUpperCase())
                ));
                if (cursor.getString(2) != null) {
                    drugs.get(drugs.size() - 1).setDescription(cursor.getString(2));
                }
                if (cursor.getString(5) != null) {
                    drugs.get(drugs.size() - 1).setDoseUnit(cursor.getString(5));
                }
                long id = cursor.getLong(0);
                drugs.get(drugs.size() - 1).setId(id);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(tag, "Found " + drugs.size() + " drugs.");
        return drugs;
    }

    public Drug getDrug(long drugId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DRUGS, new String[]{"*"}, COLUMN_ID + "=?", new String[]{Long.toString(drugId)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        assert cursor != null;
        Drug drug = new Drug(
                cursor.getString(1),
                this.getTimesForDrug(cursor.getInt(0)),
                this.getDaysOfWeek(cursor.getInt(0)),
                cursor.getInt(3),
                DrugForm.valueOf(cursor.getString(4).toUpperCase())
        );
        if (cursor.getString(2) != null) {
            drug.setDescription(cursor.getString(2));
        }
        if (cursor.getString(5) != null) {
            drug.setDoseUnit(cursor.getString(5));
        }
        drug.setId(drugId);
        cursor.close();
        return drug;
    }

    public void deleteDrug(Drug drug) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_DRUGS, COLUMN_ID + " = ?", new String[]{String.valueOf(drug.getId())});
        db.delete(TABLE_TIME, COLUMN_DRUG_ID + " = ?", new String[]{String.valueOf(drug.getId())});
        db.delete(TABLE_WEEKDAYS, COLUMN_DRUG_ID + " = ?", new String[]{String.valueOf(drug.getId())});
        db.close();
    }

    public void addDrug(Drug drug) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(TABLE_DRUGS, null, this.getDrugContentValues(drug));
        drug.setId(id);

        if (id == -1) {
            Log.e(tag, "Failed to insert drug " + drug.getName());
        }

        for (DayOfWeek day : drug.getDays()) {
            long dayId = db.insert(TABLE_WEEKDAYS, null, this.getDayOfWeekContentValues(day, id));
            if (dayId == -1)
                Log.e(tag, "Failed to insert day for drug " + drug.getName());
        }
        for (LocalTime time : drug.getIntake()) {
            long timeId = db.insert(TABLE_TIME, null, this.getTimeContentValues(time, id));
            if (timeId == -1)
                Log.e(tag, "Failed to insert time for drug " + drug.getName());
        }
    }

    public void updateDrug(Drug drug) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_DRUGS, this.getDrugContentValues(drug), COLUMN_ID + "= ?", new String[]{String.valueOf(drug.getId())});
        db.delete(TABLE_WEEKDAYS, COLUMN_DRUG_ID + "=?", new String[]{String.valueOf(drug.getId())});
        for (DayOfWeek day : drug.getDays()) {
            db.insert(TABLE_WEEKDAYS, null, this.getDayOfWeekContentValues(day, drug.getId()));
        }
        db.delete(TABLE_TIME, COLUMN_DRUG_ID + "=?", new String[]{String.valueOf(drug.getId())});
        for (LocalTime time : drug.getIntake()) {
            db.insert(TABLE_TIME, null, this.getTimeContentValues(time, drug.getId()));
        }
    }

    private ContentValues getDrugContentValues(Drug drug) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, drug.getName());
        if (drug.descriptionExists()) values.put(COLUMN_DESCRIPTION, drug.getDescription());
        values.put(COLUMN_DOSE_PER_INTAKE, drug.getDosePerIntake());
        values.put(COLUMN_DOSE_FORM, drug.getForm());
        if (drug.doseUnitExists()) values.put(COLUMN_DOSE_UNIT, drug.getDoseUnit());
        return values;
    }

    private ContentValues getDayOfWeekContentValues(DayOfWeek day, long id) {
        ContentValues dayValues = new ContentValues();
        dayValues.put(COLUMN_DAY, day.getValue());
        dayValues.put(COLUMN_DRUG_ID, id);
        return dayValues;
    }

    private ContentValues getTimeContentValues(LocalTime time, long id) {
        ContentValues timeValues = new ContentValues();
        timeValues.put(COLUMN_HOUR, time.getHour());
        timeValues.put(COLUMN_MINUTE, time.getMinute());
        timeValues.put(COLUMN_SECONDS, time.getSecond());
        timeValues.put(COLUMN_DRUG_ID, id);
        return timeValues;
    }

    private ArrayList<DayOfWeek> getDaysOfWeek(long drugId) {
        ArrayList<DayOfWeek> days = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WEEKDAYS +
                " WHERE " + COLUMN_DRUG_ID + "=" + drugId, null);
        if (cursor.moveToFirst()) {
            do {
                days.add(DayOfWeek.of(cursor.getInt(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return days;
    }

    private ArrayList<LocalTime> getTimesForDrug(long drugId) {
        ArrayList<LocalTime> times = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TIME +
                " WHERE " + COLUMN_DRUG_ID + "=" + drugId, null);

        if (cursor.moveToFirst()) {
            do {
                times.add(LocalTime.of(
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3)
                ));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return times;
    }

    public void importDatabase(final Activity activity, Uri dbPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        try {
            ParcelFileDescriptor newDb = context.getContentResolver().openFileDescriptor(dbPath, "r");
            File oldDb = context.getDatabasePath(this.getDatabaseName());
            FileInputStream importDB = new FileInputStream(newDb.getFileDescriptor());
            FileOutputStream currentDB = new FileOutputStream(oldDb);
            FileChannel src = importDB.getChannel();
            FileChannel dst = currentDB.getChannel();

            src.transferTo(0, src.size(), dst);

            src.close();
            dst.close();
            newDb.close();

            builder.setMessage(R.string.import_successful);
            builder.setNeutralButton(R.string.okay, null);
        }
        catch (IOException | NullPointerException e) {
            Log.e(tag + "_IMPORT_DATABASE", Objects.requireNonNull(e.getLocalizedMessage()));
            builder.setMessage(R.string.import_unsuccessful);
            builder.setNegativeButton(R.string.okay, null);
        }
        builder.create().show();
    }

    public void exportDatabase() {
        File sd = Environment.getExternalStorageDirectory();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        try {
            if (sd.canWrite()) {
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File currentDB = context.getDatabasePath(this.getDatabaseName());
                File backupDB = new File(downloads, "Medikamentenplan.db");
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                builder.setMessage(R.string.export_successful);
                builder.setNeutralButton(R.string.okay, null);
            }
            else {
                builder.setMessage(R.string.export_unsuccessful);
                builder.setNegativeButton(R.string.okay, null);
                throw new Exception("Can't write to external storage.");
            }
        }
        catch (Exception e) {
            Log.e(tag + "_EXPORT_DATABASE", Objects.requireNonNull(e.getLocalizedMessage()));
        }
        finally {
            builder.create().show();
        }
    }

    public static void getStoragePermission(Activity activity) {
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
    }
}
