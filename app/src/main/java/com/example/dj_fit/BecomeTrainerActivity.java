package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class BecomeTrainerActivity extends AppCompatActivity {

    private Button btnBecomeTrainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_trainer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnBecomeTrainer = findViewById(R.id.btnBecomeTrainer);
        btnBecomeTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trainerIntent = new Intent(getApplicationContext(), TrainerRegisterActivity.class);
                startActivity(trainerIntent);
            }
        });
    }

}
