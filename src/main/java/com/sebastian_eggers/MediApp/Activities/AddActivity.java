package com.sebastian_eggers.MediApp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.sebastian_eggers.MediApp.BuildConfig;
import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Adapter.TimeAdapter;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.Enum.DrugForm;
import com.sebastian_eggers.MediApp.R;
import com.sebastian_eggers.MediApp.Worker.NotificationCancelWorker;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AddActivity extends AppCompatActivity {
    protected final Context context = this;
    protected final ArrayList<LocalTime> times = new ArrayList<>();

    protected static final int MENU_ITEM_EDIT = 1;
    protected static final int MENU_ITEM_CREATE = 2;
    protected static final int MENU_ITEM_DELETE = 3;

    private static final String tag = DrugDBHelper.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_add);

        // Compute view elements
        computeWeekdayCheckboxes();
        computeDrugRadioButtons();

        // Initialize event listeners
        initializeAddTimeButtonEventListener();
        initializeAddButtonEventListener();
        initializeDatePicker();
    }

    /*
    Event listener for selected time list item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Build the context menu for the time list
     * @param menu The context menu
     * @param view The list view
     * @param menuInfo The menu info
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.setHeaderTitle(R.string.choose_action);
        menu.add(0, MENU_ITEM_CREATE, 0, R.string.time_edit_create);
        menu.add(1, MENU_ITEM_EDIT, 0, R.string.time_edit_edit);
        menu.add(1, MENU_ITEM_DELETE, 1, R.string.time_edit_delete);
        menu.setGroupDividerEnabled(true);
    }

    /**
     * Handles the actions by the context menu
     * @param item Context menu id
     * @return True if action found, false if not
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final ListView timesListView = findViewById(R.id.list_view_times);
        final TimeAdapter arrayAdapter = (TimeAdapter) timesListView.getAdapter();

        switch (item.getItemId()) {
            case MENU_ITEM_EDIT:
                LocalTime time = times.get(info.position);
                TimePickerDialog timeEditDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        times.set(info.position, LocalTime.of(hourOfDay, minute));
                        arrayAdapter.notifyDataSetChanged();
                        setListViewHeight(timesListView);
                    }
                }, time.getHour(), time.getMinute(), true);
                timeEditDialog.show();
                break;
            case MENU_ITEM_CREATE:
                TimePickerDialog timeCreateDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        times.add(LocalTime.of(hourOfDay, minute));
                        arrayAdapter.notifyDataSetChanged();
                        setListViewHeight(timesListView);
                    }
                }, LocalTime.now().getHour(), LocalTime.now().getMinute(), true);
                timeCreateDialog.show();
                break;
            case MENU_ITEM_DELETE:
                times.remove(info.position);
                arrayAdapter.notifyDataSetChanged();
                setListViewHeight(timesListView);
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Returns the string given by an identifier
     *
     * @param identifier of string.xml
     * @return string identified by identifier
     */
    protected String getName(String identifier) {
        Resources r = getResources();
        int id = r.getIdentifier(identifier, "string", BuildConfig.APPLICATION_ID);
        return r.getString(id);
    }

    /**
     * Computes and set the height of an list view based on its children
     *
     * @param listView list view which height should be computed
     */
    protected static void setListViewHeight(ListView listView) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        TimeAdapter arrayAdapter = (TimeAdapter) listView.getAdapter();
        int total = 0;
        for(int i = 0; i < arrayAdapter.getCount(); i++) {
            View listItem = arrayAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            total += listItem.getMeasuredHeight();
        }
        params.height = total + (listView.getDividerHeight() * (arrayAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * Compute the checkboxes based on the day of week enum
     */
    protected void computeWeekdayCheckboxes() {
        RadioGroup drugIntervalRadioGroup = findViewById(R.id.radio_group_drug_interval);
        drugIntervalRadioGroup.removeAllViews();
        for(DayOfWeek day: DayOfWeek.values()) {
            CheckBox checkBox = new CheckBox(this);
            String dayVal = translateDayOfWeekGermanEnglish(day.toString());
            String text = dayVal.charAt(0) + dayVal.substring(1).toLowerCase();
            checkBox.setText(text);
            drugIntervalRadioGroup.addView(checkBox);
        }
    }

    /**
     * Compute the radio buttons based on the drug form enum
     */
    protected void computeDrugRadioButtons() {
        RadioGroup drugFormRadioGroup = findViewById(R.id.radio_group_drug_form);
        drugFormRadioGroup.removeAllViews();
        for(DrugForm form: DrugForm.values()) {
            RadioButton button = new RadioButton(this);
            button.setText(this.getName(form.toString()));
            drugFormRadioGroup.addView(button);
        }
    }

    /**
     * Initialize the add drug button event listener
     */
    protected void initializeAddButtonEventListener() {
        Button addDrugButton = findViewById(R.id.button_add);
        addDrugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String drugName = ((EditText) findViewById(R.id.edit_drug_name)).getText().toString();
                String drugDescription = ((EditText) findViewById(R.id.edit_drug_description)).getText().toString();
                ArrayList<DayOfWeek> weekDays = getWeekDaysFromActivity();

                String sDosePerIntake = ((EditText) findViewById(R.id.edit_drug_dose_per_intake)).getText().toString();
                final int drugDosePerIntake = (sDosePerIntake.length() > 0) ? Integer.parseInt(sDosePerIntake) : 0;

                String drugDoseUnit = ((EditText) findViewById(R.id.edit_drug_dose_unit)).getText().toString();
                DrugForm drugForm = getDrugFormFromActivity();

                if(drugName.length() > 0 &&
                        weekDays.size() > 0 &&
                        times.size() > 0 &&
                        drugDosePerIntake > 0 &&
                        drugForm != null) {

                    Drug drug = new Drug(drugName, times, weekDays, drugDosePerIntake, drugForm, drugDescription, drugDoseUnit);
                    drug.scheduleNotification(context);

                    String date = ((EditText) findViewById(R.id.edit_drug_date_of_last_intake)).getText().toString();
                    if(date.length() > 0) {
                        drug.setDateOfLastIntake(LocalDate.parse(date));
                        scheduleNotificationCancelWorker(drug, context);
                    }

                    DrugDBHelper dbHelper = new DrugDBHelper(context);
                    dbHelper.addDrug(drug);

                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(mainActivity);
                        }
                    });
                    alert.setTitle(R.string.add_title);
                    alert.setMessage(R.string.add_successful);
                    alert.show();
                }
                else {
                    Log.e(tag, "Somethings missing..");
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle(R.string.add_title);
                    alert.setMessage(R.string.add_unsuccessful);
                    alert.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(drugName.length() <= 0) {
                                findViewById(R.id.edit_drug_name).requestFocus();
                            }
                            else if(drugDosePerIntake <= 0) {
                                findViewById(R.id.edit_drug_dose_per_intake).requestFocus();
                            }
                        }
                    });
                    alert.show();
                }
            }
        });
    }

    /**
     * Get the weekdays checked in the activity
     *
     * @return ArrayList with DayOfWeek enum
     */
    protected ArrayList<DayOfWeek> getWeekDaysFromActivity() {
        ArrayList<DayOfWeek> weekDays = new ArrayList<>();
        RadioGroup drugIntervalRadioGroup = findViewById(R.id.radio_group_drug_interval);
        for(int i = 0; i < drugIntervalRadioGroup.getChildCount(); i++) {
            CheckBox box = (CheckBox) drugIntervalRadioGroup.getChildAt(i);
            if(box.isChecked()) {
                weekDays.add(DayOfWeek.valueOf(translateDayOfWeekGermanEnglish(box.getText().toString()).toUpperCase()));
            }
        }
        return weekDays;
    }

    /**
     * Get the selected DrugForm from the radio buttons
     *
     * @return DrugForm of Drug Model
     */
    protected DrugForm getDrugFormFromActivity() {
        RadioGroup drugFormRadioGroup = findViewById(R.id.radio_group_drug_form);
        for(int i = 0; i < drugFormRadioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) drugFormRadioGroup.getChildAt(i);
            if(radioButton.isChecked())
                return DrugForm.translate(radioButton.getText().toString());
        }
        return null;
    }

    /**
     * Initialize the add time button event listener
     */
    protected void initializeAddTimeButtonEventListener() {
        final TimeAdapter arrayAdapter = new TimeAdapter(this, times);
        final ListView timesListView = findViewById(R.id.list_view_times);
        timesListView.setAdapter(arrayAdapter);
        registerForContextMenu(timesListView);

        Button addTimeButton = findViewById(R.id.button_add_time);
        addTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        times.add(LocalTime.of(hourOfDay, minute));
                        arrayAdapter.notifyDataSetChanged();
                        setListViewHeight(timesListView);
                    }
                }, LocalTime.now().getHour(), LocalTime.now().getMinute(), true);
                timePickerDialog.show();
            }
        });
    }

    /**
     * Translate weekdays between German and English
     *
     * @param day Weekday as DayOfWeek
     * @return The translated uppercase value of the weekday
     */
    public static String translateDayOfWeekGermanEnglish(String day) {
        switch (day.toUpperCase()) {
            case "MONDAY":
                return "MONTAG";
            case "MONTAG":
                return "MONDAY";
            case "TUESDAY":
                return "DIENSTAG";
            case "DIENSTAG":
                return "TUESDAY";
            case "WEDNESDAY":
                return "MITTWOCH";
            case "MITTWOCH":
                return "WEDNESDAY";
            case "THURSDAY":
                return "DONNERSTAG";
            case "DONNERSTAG":
                return "THURSDAY";
            case "FRIDAY":
                return "FREITAG";
            case "FREITAG":
                return "FRIDAY";
            case "SATURDAY":
                return "SAMSTAG";
            case "SAMSTAG":
                return "SATURDAY";
            case "SUNDAY":
                return "SONNTAG";
            default:
                return "SUNDAY";
        }
    }

    /**
     * Initialize the date picker for the date field
     */
    protected void initializeDatePicker() {
        final TextView dateTextView = findViewById(R.id.edit_drug_date_of_last_intake);
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = year + "-" + (month > 9 ? (month + 1) : "0" + (month + 1)) + "-" + (dayOfMonth > 9 ? dayOfMonth : "0" + dayOfMonth);
                        dateTextView.setText(date);
                    }
                }, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());

                datePickerDialog.show();
            }
        });
    }

    /**
     * Schedule a WorkRequest to cancel the notification on a specific date
     *
     * @param drug Drug which notification should be cancelled
     */
    public static void scheduleNotificationCancelWorker(Drug drug, Context context) {
        LocalDate date = drug.getDateOfLastIntake();
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0);
        Calendar today = Calendar.getInstance();

        Data.Builder data = new Data.Builder();
        data.putLong("drug_id", drug.getId());

        WorkRequest cancelRequest = new OneTimeWorkRequest.Builder(NotificationCancelWorker.class)
                .setInitialDelay(calendar.getTimeInMillis() - today.getTimeInMillis(), TimeUnit.MILLISECONDS)
                .setInputData(data.build())
                .build();

        WorkManager.getInstance(context).enqueue(cancelRequest);
    }
}
