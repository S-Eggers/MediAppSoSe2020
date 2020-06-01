package com.sebastian_eggers.MediApp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import com.sebastian_eggers.MediApp.BuildConfig;
import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Helper.TimeAdapter;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.Models.DrugForm;
import com.sebastian_eggers.MediApp.R;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.setHeaderTitle(R.string.choose_action);
        menu.add(0, MENU_ITEM_CREATE, 0, R.string.time_edit_create);
        menu.add(1, MENU_ITEM_EDIT, 0, R.string.time_edit_edit);
        menu.add(1, MENU_ITEM_DELETE, 1, R.string.time_edit_delete);
        menu.setGroupDividerEnabled(true);
    }

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
            String text = day.toString().charAt(0) + day.toString().substring(1).toLowerCase();
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
                String drugName = ((EditText) findViewById(R.id.edit_drug_name)).getText().toString();
                String drugDescription = ((EditText) findViewById(R.id.edit_drug_description)).getText().toString();
                ArrayList<DayOfWeek> weekDays = getWeekDaysFromActivity();
                int drugDosePerIntake = Integer.parseInt(((EditText) findViewById(R.id.edit_drug_dose_per_intake)).getText().toString());
                String drugDoseUnit = ((EditText) findViewById(R.id.edit_drug_dose_unit)).getText().toString();
                DrugForm drugForm = getDrugFormFromActivity();

                if(drugName.length() > 0 && weekDays.size() > 0 && drugDosePerIntake > 0 && drugForm != null) {
                    Drug drug = new Drug(drugName, times, weekDays, drugDosePerIntake, drugForm, drugDescription, drugDoseUnit);

                    DrugDBHelper dbHelper = new DrugDBHelper(context);
                    dbHelper.addDrug(drug);

                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainActivity);
                }
                else {
                    Log.e(tag, "Somethings missing..");
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
                weekDays.add(DayOfWeek.valueOf(box.getText().toString().toUpperCase()));
            }
        }
        return weekDays;
    }

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
}
