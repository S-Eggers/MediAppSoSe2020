package com.sebastian_eggers.MediApp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sebastian_eggers.MediApp.R;

import java.util.Objects;

public class StartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_start);

        Button startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivity);
            }
        });

        Button importButton = findViewById(R.id.button_import);
        importButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent importActivity = new Intent(getApplicationContext(), ImportActivity.class);
                startActivity(importActivity);
            }
        });
    }

}
