package com.sebastian_eggers.MediApp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sebastian_eggers.MediApp.Adapter.DrugAdapter;
import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.R;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Objects;

public class OldActivity extends TodayActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.intake_old);

        drugs = dbHelper.getAllDrugs();
        LocalDate today = LocalDate.now();
        // unschön, das hätte man über SQL regeln sollen...
        Iterator<Drug> iterator = drugs.iterator();
        while (iterator.hasNext()) {
            Drug drug = (Drug) iterator.next();
            if (drug.getDateOfLastIntake() == null || drug.getDateOfLastIntake().isAfter(today)) {
                iterator.remove();
            }
        }

        ListView drugList = findViewById(R.id.drug_list);
        drugList.setElevation(24);
        DrugAdapter drugAdapter = new DrugAdapter(this, drugs);
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
        menu.clear();
        menu.setHeaderTitle(R.string.choose_action);
        menu.add(0, MENU_ITEM_CREATE, 0, R.string.drug_edit_create);
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
        final DrugAdapter arrayAdapter = (DrugAdapter) listView.getAdapter();
        DrugDBHelper drugDBHelper = new DrugDBHelper(this);

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
                break;
            case MENU_ITEM_EXPORT:
                DrugDBHelper.getStoragePermission(this);
                drugDBHelper.exportDatabase();
            default:
                return false;
        }
        return true;
    }
}
