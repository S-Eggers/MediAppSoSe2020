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
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sebastian_eggers.MediApp.Helper.DrugAdapter;
import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.R;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String tag = DrugDBHelper.class.getSimpleName();

    final private DrugDBHelper dbHelper = new DrugDBHelper(this);
    private ArrayList<Drug> drugs;

    private static final int MENU_ITEM_EDIT = 1;
    private static final int MENU_ITEM_CREATE = 2;
    private static final int MENU_ITEM_DELETE = 3;
    private static final int MENU_ITEM_CHECK = 4;

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

        drugs = dbHelper.getAllDrugs();
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
        menu.add(1, MENU_ITEM_CHECK, 0, R.string.drug_check);
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
                dbHelper.deleteDrug(delete);
                drugs.remove(delete);
                arrayAdapter.notifyDataSetChanged();
                initializeNextIntake(drugs);
                break;
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
        for(Drug drug: drugs) {
            if(drug.isNextIntakeToday()) {
                LocalTime localTime = drug.nextIntake();
                if(time.compareTo(localTime) > 0) {
                    time = localTime;
                    drugName = drug.getName();
                }
                changed = true;
            }
        }
        if(changed)
            next.append(nextIntake.getText()).append(" ").append(time).append(" (").append(drugName).append(")");
        else
            next.append("Heute keine Einnahme mehr.");

        nextIntake.setText(next.toString());
    }
}
