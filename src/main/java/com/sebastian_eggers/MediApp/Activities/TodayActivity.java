package com.sebastian_eggers.MediApp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sebastian_eggers.MediApp.Adapter.IntakeDrugAdapter;
import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

public class TodayActivity extends AppCompatActivity {
    final protected DrugDBHelper dbHelper = new DrugDBHelper(this);
    protected ArrayList<Drug> drugs;

    protected static final int MENU_ITEM_EDIT = 1;
    protected static final int MENU_ITEM_CREATE = 2;
    protected static final int MENU_ITEM_DELETE = 3;
    protected static final int MENU_ITEM_EXPORT = 4;
    private static final int MENU_ITEM_INTAKE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.intake_from_today);

        drugs = dbHelper.getAllDrugs(true);
        Collections.sort(drugs);
        ListView drugList = findViewById(R.id.drug_list);
        drugList.setElevation(24);
        IntakeDrugAdapter drugAdapter = new IntakeDrugAdapter(this, drugs);
        drugList.setAdapter(drugAdapter);
        drugAdapter.notifyDataSetChanged();
        registerForContextMenu(drugList);
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
        menu.add(0, MENU_ITEM_CREATE, 0, R.string.drug_edit_create);
        menu.add(0, MENU_ITEM_INTAKE, 1, R.string.drug_intake_done);
        menu.add(1, MENU_ITEM_EXPORT, 2, R.string.drug_check);
        menu.add(2, MENU_ITEM_EDIT, 3, R.string.drug_edit_edit);
        menu.add(2, MENU_ITEM_DELETE, 4, R.string.drug_edit_delete);

        // Enable group separator
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
        final ListView listView = findViewById(R.id.drug_list);
        final IntakeDrugAdapter arrayAdapter = (IntakeDrugAdapter) listView.getAdapter();
        DrugDBHelper drugDBHelper = new DrugDBHelper(this);

        switch (item.getItemId()) {
            case MENU_ITEM_EDIT:
                Intent editActivity = new Intent(getApplicationContext(), EditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("itemId", drugs.get(info.position).getId());
                editActivity.putExtras(bundle);
                startActivity(editActivity);
                break;
            case MENU_ITEM_INTAKE:
                Drug intake = drugs.get(info.position);
                drugDBHelper.setIntakeToCalender(intake, Calendar.getInstance());
                drugs.get(info.position).setLastIntake(Calendar.getInstance().getTimeInMillis());
                arrayAdapter.notifyDataSetChanged();
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
                break;
            case MENU_ITEM_EXPORT:
                DrugDBHelper.getStoragePermission(this);
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
}