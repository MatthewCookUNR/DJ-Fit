// Program Information /////////////////////////////////////////////////////////
/*
 * @file TrainerMenuActivity.java
 *
 * @brief Serves a secondary main menu for trainer specific options
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

// Trainer Menu Activity Class ////////////////////////////////////////////////////////////////

public class TrainerMenuActivity extends BaseActivity {

    //Class variables
    Button btnModifyProfile, btnTrainerProfile, btnCurrentClients, btnClientRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views initialization
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

        BottomNavigationView bottomNavigationItemView = findViewById(R.id.bottomNavigationItemView);
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch(menuItem.getItemId())
                {
                    case R.id.ic_back:
                        Intent clientReqIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(clientReqIntent);
                        break;
                    case R.id.ic_home:
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                        break;
                    case R.id.ic_training:
                        break;
                }
                return false;
            }
        });
    }

}
