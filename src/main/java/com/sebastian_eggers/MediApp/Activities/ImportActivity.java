package com.sebastian_eggers.MediApp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.sebastian_eggers.MediApp.Helper.DrugDBHelper;
import com.sebastian_eggers.MediApp.R;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Objects;

public class ImportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.import_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_import);

        Button importButton = findViewById(R.id.import_sql);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData();
            DrugDBHelper drugDBHelper = new DrugDBHelper(this);
            assert selectedfile != null;
            try {
                drugDBHelper.importDatabase(this, selectedfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.import_successful);
            builder.setNeutralButton("Okay", null);
            builder.create().show();
        }
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
