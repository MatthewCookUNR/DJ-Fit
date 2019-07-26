package com.example.dj_fit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;


public class MainActivity extends BaseActivity {

    //Variables
    private static final String TAG = "MainActivity";
    private Button btnBackground, btnWorkoutOutline, btnRegisterTrainer, btnTrainerProfile,
                   btnFindTrainer, btnClientRequests, btnCurrentClients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize layout variables
        btnBackground = findViewById(R.id.btnBackground);
        btnWorkoutOutline = findViewById(R.id.btnWorkoutOutline);
        btnRegisterTrainer = findViewById(R.id.btnRegisterTrainer);
        btnTrainerProfile = findViewById(R.id.btnTrainerProfile);
        btnFindTrainer = findViewById(R.id.btnFindTrainer);
        btnClientRequests = findViewById(R.id.btnClientRequests);
        btnCurrentClients = findViewById(R.id.btnCurrentClients);

        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String trainerCode = myPreferences.getString("trainerCode", "");

        if(!trainerCode.equals("false"))
        {
            btnRegisterTrainer.setText("Modify Trainer Profile");
        }

        btnBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backgroundAct = new Intent(MainActivity.this, BackgroundActivity.class);
                startActivity(backgroundAct);
            }
        });

        btnWorkoutOutline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent workoutOutlineAct = new Intent(MainActivity.this, WorkoutOutlineActivity.class);
                startActivity(workoutOutlineAct);
            }
        });

        btnRegisterTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trainerRegisterIntent = new Intent(MainActivity.this, TrainerRegisterActivity.class);
                startActivity(trainerRegisterIntent);
            }
        });

        btnTrainerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trainerProfileIntent = new Intent(MainActivity.this, TrainerProfileActivity.class);
                trainerProfileIntent.putExtra("isOwner", true);
                startActivity(trainerProfileIntent);
            }
        });
        btnFindTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findTrainerIntent = new Intent(MainActivity.this, FindTrainerActivity.class);
                startActivity(findTrainerIntent);
            }
        });

        btnClientRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent clientRequestsIntent = new Intent(MainActivity.this, ClientRequestsActivity.class);
                startActivity(clientRequestsIntent);
            }
        });

        btnCurrentClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent currentClientIntent = new Intent(MainActivity.this, CurrentClientsActivity.class);
                startActivity(currentClientIntent);
            }
        });
    }
}
