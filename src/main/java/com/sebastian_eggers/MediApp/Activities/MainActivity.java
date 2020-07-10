package com.sebastian_eggers.MediApp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sebastian_eggers.MediApp.Adapter.DrugAdapter;
import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    final private DrugDBHelper dbHelper = new DrugDBHelper(this);
    private ArrayList<Drug> drugs;

    private static final int MENU_ITEM_EDIT = 1;
    private static final int MENU_ITEM_CREATE = 2;
    private static final int MENU_ITEM_DELETE = 3;
    private static final int MENU_ITEM_EXPORT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        FloatingActionButton addButton = findViewById(R.id.floating_add_drug_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addActivity = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(addActivity);
            }
        });

        Button todayButton = findViewById(R.id.button_today);
        todayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent todayActivity = new Intent(getApplicationContext(), TodayActivity.class);
                startActivity(todayActivity);
            }
        });

        Button oldButton = findViewById(R.id.button_old);
        oldButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent oldActivity = new Intent(getApplicationContext(), OldActivity.class);
                startActivity(oldActivity);
            }
        });

        drugs = dbHelper.getAllDrugs();
        LocalDate today = LocalDate.now();
        Iterator<Drug> iterator = drugs.iterator();
        while (iterator.hasNext()) {
            Drug drug = (Drug) iterator.next();
            if (drug.getDateOfLastIntake() != null && drug.getDateOfLastIntake().isBefore(today)) {
                iterator.remove();
            }
        }

        Collections.sort(drugs);
        ListView drugList = findViewById(R.id.drug_list);
        drugList.setElevation(24);
        DrugAdapter drugAdapter = new DrugAdapter(this, drugs);
        drugList.setAdapter(drugAdapter);
        drugAdapter.notifyDataSetChanged();
        initializeNextIntake(drugs);
        registerForContextMenu(drugList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.setHeaderTitle(R.string.choose_action);
        menu.add(0, MENU_ITEM_CREATE, 0, R.string.drug_edit_create);
        menu.add(1, MENU_ITEM_EXPORT, 0, R.string.drug_check);
        menu.add(2, MENU_ITEM_EDIT, 0, R.string.drug_edit_edit);
        menu.add(2, MENU_ITEM_DELETE, 1, R.string.drug_edit_delete);

        // Enable group separator
        menu.setGroupDividerEnabled(true);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final ListView listView = findViewById(R.id.drug_list);
        final DrugAdapter arrayAdapter = (DrugAdapter) listView.getAdapter();

        switch (item.getItemId()) {
            case MENU_ITEM_EDIT:
                Intent editActivity = new Intent(getApplicationContext(), EditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("itemId", drugs.get(info.position).getId());
                editActivity.putExtras(bundle);
                startActivity(editActivity);
                break;
            case MENU_ITEM_CREATE:
                Intent addActivity = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(addActivity);
                break;
            case MENU_ITEM_DELETE:
                Drug delete = drugs.get(info.position);
                delete.cancelNotifications(this);
                dbHelper.deleteDrug(delete);
                drugs.remove(delete);
                arrayAdapter.notifyDataSetChanged();
                initializeNextIntake(drugs);
                break;
            case MENU_ITEM_EXPORT:
                DrugDBHelper.getStoragePermission(this);
                DrugDBHelper drugDBHelper = new DrugDBHelper(this);
                drugDBHelper.exportDatabase();
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeNextIntake(ArrayList<Drug> drugs) {
        TextView nextIntake = findViewById(R.id.next_intake_text_view);
        StringBuilder next = new StringBuilder();

        boolean changed = false;
        LocalTime time = LocalTime.of(23,59,59);
        String drugName = "";
        boolean overdue = false;

        Calendar now = Calendar.getInstance();
        DayOfWeek nowDoW = getCurrentDayOfWeek();
        Calendar lastIntake = Calendar.getInstance();
        ZonedDateTime startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        long maxDiff = now.getTimeInMillis() - startOfToday.toEpochSecond() * 1000;

        for(Drug drug: drugs) {
            lastIntake.setTimeInMillis(drug.getLastIntake());
            long diff = now.getTimeInMillis() - lastIntake.getTimeInMillis();
            if(drug.getDays().contains(nowDoW) && diff > maxDiff + 1 && !drug.isNextIntakeToday()) {
                overdue = true;
            }

            if(drug.isNextIntakeToday()) {
                LocalTime localTime = drug.nextIntake();
                if(time.compareTo(localTime) > 0) {
                    time = localTime;
                    drugName = drug.getName();
                }
                changed = true;
            }
        }

        if(overdue)
            next.append(getResources().getString(R.string.intake_overdue));
        else if(changed)
            next.append(getResources().getString(R.string.next_intake)).append(" ").append(time).append(" (").append(drugName).append(")");
        else
            next.append(getResources().getString(R.string.no_intake_today));

        nextIntake.setText(next.toString());
    }

    private DayOfWeek getCurrentDayOfWeek() {
        Calendar now = Calendar.getInstance();
        switch (now.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return DayOfWeek.MONDAY;
            case Calendar.TUESDAY:
                return DayOfWeek.TUESDAY;
            case Calendar.WEDNESDAY:
                return DayOfWeek.WEDNESDAY;
            case Calendar.THURSDAY:
                return DayOfWeek.THURSDAY;
            case Calendar.FRIDAY:
                return DayOfWeek.FRIDAY;
            case Calendar.SATURDAY:
                return DayOfWeek.SATURDAY;
            default:
                return DayOfWeek.SUNDAY;
        }
    }
}
