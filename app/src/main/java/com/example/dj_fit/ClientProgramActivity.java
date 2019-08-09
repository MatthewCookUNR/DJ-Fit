// Program Information /////////////////////////////////////////////////////////
/*
 * @file ClientProgramActivity.java
 *
 * @brief Activity is used to navigate the different information that is shared
  *       by a client with the trainer
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
import android.widget.TextView;

// Client Program Activity Class ////////////////////////////////////////////////////////////////

public class ClientProgramActivity extends BaseActivity
{
    //Class variables
    private TextView titleText;
    private Button btnBackground, btnWorkoutOutline;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_program);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views and variables initialization
        titleText = findViewById(R.id.titleText);
        btnBackground = findViewById(R.id.btnBackground);
        btnWorkoutOutline = findViewById(R.id.btnWorkoutOutline);
        final String[] clientData = getIntent().getStringExtra("clientTag").split("/");
        titleText.setText(clientData[1] + " " + clientData[2]);

        btnBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backgroundAct = new Intent(ClientProgramActivity.this, BackgroundActivity.class);
                backgroundAct.putExtra("clientID", clientData[0]);
                backgroundAct.putExtra("clientTag", getIntent().getStringExtra("clientTag"));
                startActivity(backgroundAct);
            }
        });

        btnWorkoutOutline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent workoutOutlineAct = new Intent(ClientProgramActivity.this, WorkoutOutlineActivity.class);
                workoutOutlineAct.putExtra("clientID", clientData[0]);
                workoutOutlineAct.putExtra("clientTag", getIntent().getStringExtra("clientTag"));
                startActivity(workoutOutlineAct);
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
                        Intent clientReqIntent = new Intent(getApplicationContext(), CurrentClientsActivity.class);
                        startActivity(clientReqIntent);
                        break;
                    case R.id.ic_home:
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                        break;
                    case R.id.ic_training:
                        Intent trainerIntent = new Intent(getApplicationContext(), TrainerMenuActivity.class);
                        startActivity(trainerIntent);
                        break;
                }
                return false;
            }
        });
    }

}
