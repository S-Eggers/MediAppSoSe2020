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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class DrugDBHelper extends SQLiteOpenHelper {

    /**
     * _____________________________________________________________________________________________
     *
     *                                  SQL & Database
     *
     * _____________________________________________________________________________________________
     */

    public static final String DB_NAME = "drugs.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_DRUGS = "tbl_drugs";
    public static final String TABLE_TIME = "tbl_time";
    public static final String TABLE_WEEKDAYS = "tbl_days";
    public static final String TABLE_NOTIFICATIONS = "tbl_notifications";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DOSE_PER_INTAKE = "dose_amount";
    public static final String COLUMN_DOSE_FORM = "dose_form";
    public static final String COLUMN_DOSE_UNIT = "dose_unit";
    public static final String COLUMN_LAST_INTAKE = "last_intake";
    public static final String COLUMN_TAKE_DRUG_UNTIL = "take_drug_until";

    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_SECONDS = "seconds";
    public static final String COLUMN_DRUG_ID = "drug_id";

    public static final String COLUMN_DAY = "day";

    public static final String COLUMN_NOT_TEXT = "text";
    public static final String COLUMN_NOT_TIME = "time";
    public static final String COLUMN_NOT_REPEATING = "repeating";

    public static final String SQL_CREATE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS +
            "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NOT_TEXT + " TEXT," +
            COLUMN_NOT_TIME + " INTEGER," +
            COLUMN_NOT_REPEATING + " TEXT);";


    public static final String SQL_CREATE_DRUG = "CREATE TABLE " + TABLE_DRUGS +
            "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT," +
            COLUMN_DOSE_PER_INTAKE + " INTEGER NOT NULL, " +
            COLUMN_DOSE_FORM + " TEXT NOT NULL, " +
            COLUMN_DOSE_UNIT + " TEXT," +
            COLUMN_LAST_INTAKE + " INTEGER," +
            COLUMN_TAKE_DRUG_UNTIL + " TEXT);";

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

    /**
     * _____________________________________________________________________________________________
     *
     *                                  General class functions
     *
     * _____________________________________________________________________________________________
     */

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
            Log.d(tag, "Table created with following SQL: " + SQL_CREATE_NOTIFICATIONS);
            db.execSQL(SQL_CREATE_NOTIFICATIONS);

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

    /**
     * _____________________________________________________________________________________________
     *
     *                                  Drug functions
     *
     * _____________________________________________________________________________________________
     */

    /**
     * Get the number of drugs
     *
     * @return Drug count
     */
    public int getDrugsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DRUGS, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Get drugs by a sql cursor
     *
     * @param cursor Cursor with executed sql query
     * @return All Drugs matching the query
     */
    private ArrayList<Drug> getAllDrugsByQuery(Cursor cursor) {
        ArrayList<Drug> drugs = new ArrayList<>();

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

                long lastIntake = cursor.getLong(6);
                drugs.get(drugs.size() - 1).setLastIntake(lastIntake);
                String dateOfLastIntake = cursor.getString(7);
                if(dateOfLastIntake != null) {
                    LocalDate date = LocalDate.parse(dateOfLastIntake);
                    Log.d("___", date.toString());
                    drugs.get(drugs.size() - 1).setDateOfLastIntake(date);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(tag, "Found " + drugs.size() + " drugs.");
        return drugs;
    }

    /**
     * Get all drugs
     *
     * @return All drugs in the database
     */
    public ArrayList<Drug> getAllDrugs() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DRUGS, null);
        return this.getAllDrugsByQuery(cursor);
    }

    /**
     * Get all drugs, it is possible to specify only drugs that are taken today
     *
     * @param onlyToday True if only the drugs from today or false if all drugs
     * @return All drugs
     */
    public ArrayList<Drug> getAllDrugs(boolean onlyToday) {
        if(onlyToday) {
            SQLiteDatabase db = this.getReadableDatabase();

            String sql = "SELECT " + TABLE_DRUGS + "." + COLUMN_ID + ", " +
                    TABLE_DRUGS + "." + COLUMN_NAME + ", " +
                    TABLE_DRUGS + "." + COLUMN_DESCRIPTION + ", " +
                    TABLE_DRUGS + "." + COLUMN_DOSE_PER_INTAKE + ", " +
                    TABLE_DRUGS + "." + COLUMN_DOSE_FORM + ", " +
                    TABLE_DRUGS + "." + COLUMN_DOSE_UNIT + ", " +
                    TABLE_DRUGS + "." + COLUMN_LAST_INTAKE + ", " +
                    TABLE_DRUGS + "." + COLUMN_TAKE_DRUG_UNTIL + ", " +
                    TABLE_WEEKDAYS + "." + COLUMN_DAY +
                    " FROM " + TABLE_DRUGS +
                    " INNER JOIN " + TABLE_WEEKDAYS + " " +
                    "ON " + TABLE_WEEKDAYS + "." +
                    COLUMN_DRUG_ID + "=" + TABLE_DRUGS + "." + COLUMN_ID +
                    " WHERE " + COLUMN_DAY + "=?";

            Cursor cursor = db.rawQuery(sql, new String[] {Integer.toString(LocalDate.now().getDayOfWeek().getValue())});

            return this.getAllDrugsByQuery(cursor);
        }
        else {
            return this.getAllDrugs();
        }
    }

    /**
     * Get a single drug by the id
     * @param drugId The drug id
     * @return A single Drug Model
     */
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
        String dateOfLastIntake = cursor.getString(7);
        if(dateOfLastIntake != null)
            drug.setDateOfLastIntake(LocalDate.parse(dateOfLastIntake));

        drug.setId(drugId);
        cursor.close();
        return drug;
    }

    /**
     * Delete a drug and its time and week values from the database
     *
     * @param drug Drug Model
     */
    public void deleteDrug(Drug drug) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_DRUGS, COLUMN_ID + " = ?", new String[]{String.valueOf(drug.getId())});
        db.delete(TABLE_TIME, COLUMN_DRUG_ID + " = ?", new String[]{String.valueOf(drug.getId())});
        db.delete(TABLE_WEEKDAYS, COLUMN_DRUG_ID + " = ?", new String[]{String.valueOf(drug.getId())});
        db.close();
    }

    /**
     * Delete all drugs and their time and week values from the database
     */
    public void deleteAllDrugs() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DRUGS);
        db.execSQL("DELETE FROM " + TABLE_TIME);
        db.execSQL("DELETE FROM " + TABLE_WEEKDAYS);
    }

    /**
     * Add a single drug to the database
     *
     * @param drug Drug Model
     */
    public void addDrug(Drug drug) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(TABLE_DRUGS, null, this.getDrugContentValues(drug));
        if(drug.getId() == -1)
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

    /**
     * Update a drug
     *
     * @param drug Drug Model
     */
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

    /**
     * Update the last intake of a Drug
     *
     * @param drug Drug Model
     * @param calendar Calender with last intake
     */
    public void setIntakeToCalender(Drug drug, Calendar calendar) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_INTAKE, calendar.getTimeInMillis());
        db.update(TABLE_DRUGS, values, COLUMN_ID + "=?", new String[]{String.valueOf(drug.getId())});
    }

    /**
     * Prepare the content values of the drug
     *
     * @param drug Drug Model
     * @return ContentValues with drug content
     */
    private ContentValues getDrugContentValues(Drug drug) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, drug.getName());
        if (drug.descriptionExists()) values.put(COLUMN_DESCRIPTION, drug.getDescription());
        values.put(COLUMN_DOSE_PER_INTAKE, drug.getDosePerIntake());
        values.put(COLUMN_DOSE_FORM, drug.getForm());
        if (drug.doseUnitExists()) values.put(COLUMN_DOSE_UNIT, drug.getDoseUnit());
        if (drug.getDateOfLastIntake() != null) {
            String date = drug.getDateOfLastIntake().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            values.put(COLUMN_TAKE_DRUG_UNTIL, date);
        }
        if (drug.getLastIntake() > 0) {
            values.put(COLUMN_LAST_INTAKE, drug.getLastIntake());
        }

        return values;
    }

    /**
     * Prepare a weekday value of the drug
     *
     * @param day DayOfWeek
     * @param id Drug id
     * @return ContentValue with the weekday
     */
    private ContentValues getDayOfWeekContentValues(DayOfWeek day, long id) {
        ContentValues dayValues = new ContentValues();
        dayValues.put(COLUMN_DAY, day.getValue());
        dayValues.put(COLUMN_DRUG_ID, id);
        return dayValues;
    }

    /**
     * Prepare a time value of the drug
     *
     * @param time Time value
     * @param id Drug id
     * @return ContentValue with the time
     */
    private ContentValues getTimeContentValues(LocalTime time, long id) {
        ContentValues timeValues = new ContentValues();
        timeValues.put(COLUMN_HOUR, time.getHour());
        timeValues.put(COLUMN_MINUTE, time.getMinute());
        timeValues.put(COLUMN_SECONDS, time.getSecond());
        timeValues.put(COLUMN_DRUG_ID, id);
        return timeValues;
    }

    /**
     * Get all the days from a single drug
     *
     * @param drugId Drug id
     * @return List with the days
     */
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

    /**
     * Get all times from a single drug
     *
     * @param drugId Drug id
     * @return Time values of the drug
     */
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


    /**
     * _____________________________________________________________________________________________
     *
     *                                  Database-Import / Export
     *
     * _____________________________________________________________________________________________
     */

    /**
     * Import a database by a path
     *
     * @param dbPath Path to the exported database values
     */
    public void importDatabase(Uri dbPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        try {
            ParcelFileDescriptor newDb = context.getContentResolver().openFileDescriptor(dbPath, "r");
            assert newDb != null;
            FileInputStream importDB = new FileInputStream(newDb.getFileDescriptor());
            ObjectInputStream objectInputStream = new ObjectInputStream(importDB);
            ArrayList<Drug> drugs = (ArrayList<Drug>) objectInputStream.readObject();
            objectInputStream.close();
            importDB.close();
            newDb.close();
            deleteAllDrugs();
            for(Drug drug: drugs) {
                addDrug(drug);
            }
            builder.setMessage(R.string.import_successful);
            builder.setNeutralButton(R.string.okay, null);
        }
        catch (IOException | NullPointerException | ClassNotFoundException e) {
            Log.e(tag + "_IMPORT_DATABASE", Objects.requireNonNull(e.getLocalizedMessage()));
            builder.setMessage(R.string.import_unsuccessful);
            builder.setNegativeButton(R.string.okay, null);
        }
        builder.create().show();
    }

    /**
     * Export all values from the database
     */
    public void exportDatabase() {
        ArrayList<Drug> drugs = getAllDrugs();
        File sd = Environment.getExternalStorageDirectory();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        try {
            if (sd.canWrite()) {
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                File backupSer = new File(downloads, "meds.mediplan");
                FileOutputStream fileOutputStream = new FileOutputStream(backupSer);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(drugs);
                objectOutputStream.close();
                fileOutputStream.close();
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

    /**
     * Get the storage permission from the system
     *
     * @param activity Current activity
     */
    public static void getStoragePermission(Activity activity) {
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
    }
}
