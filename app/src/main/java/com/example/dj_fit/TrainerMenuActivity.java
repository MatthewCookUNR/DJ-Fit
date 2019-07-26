package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class TrainerMenuActivity extends BaseActivity {

    Button btnModifyProfile, btnTrainerProfile, btnCurrentClients, btnClientRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnModifyProfile = findViewById(R.id.btnModifyProfile);
        btnTrainerProfile = findViewById(R.id.btnTrainerProfile);
        btnCurrentClients = findViewById(R.id.btnCurrentClients);
        btnClientRequests = findViewById(R.id.btnClientRequests);

        btnModifyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent modifyProfileIntent = new Intent(TrainerMenuActivity.this, TrainerRegisterActivity.class);
                startActivity(modifyProfileIntent);
            }
        });

        btnTrainerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trainerProfileIntent = new Intent(TrainerMenuActivity.this, TrainerProfileActivity.class);
                trainerProfileIntent.putExtra("isOwner", true);
                startActivity(trainerProfileIntent);
            }
        });

        btnCurrentClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent currentClientIntent = new Intent(TrainerMenuActivity.this, CurrentClientsActivity.class);
                startActivity(currentClientIntent);
            }
        });

        btnClientRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent clientRequestsIntent = new Intent(TrainerMenuActivity.this, ClientRequestsActivity.class);
                startActivity(clientRequestsIntent);
            }
        });
    }

}
