// Program Information /////////////////////////////////////////////////////////
/*
 * @file MainActivity.java
 *
 * @brief Shows the main menu screen for the app
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

// Main Activity Class ////////////////////////////////////////////////////////////////

public class MainActivity extends BaseActivity {

    //Class variables
    private static final String TAG = "MainActivity";
    RelativeLayout activity_main;
    private Button btnBackground, btnWorkoutOutline, btnFindTrainer, btnMyTrainers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views initialization
        activity_main = findViewById(R.id.activity_main);
        btnBackground = findViewById(R.id.btnBackground);
        btnWorkoutOutline = findViewById(R.id.btnWorkoutOutline);
        btnFindTrainer = findViewById(R.id.btnFindTrainer);
        btnMyTrainers = findViewById(R.id.btnMyTrainers);

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

        btnFindTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findTrainerIntent = new Intent(MainActivity.this, FindTrainerActivity.class);
                startActivity(findTrainerIntent);
            }
        });

        btnMyTrainers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myTrainersIntent = new Intent(MainActivity.this, MyTrainersActivity.class);
                startActivity(myTrainersIntent);
            }
        });

        BottomNavigationView bottomNavigationItemView = findViewById(R.id.bottomNavigationItemView);
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch(menuItem.getItemId())
                {
                    case R.id.ic_home:
                        //Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        //startActivity(homeIntent);
                        break;
                    case R.id.ic_training:
                        //Checks to see if the user is currently a trainer
                        final SharedPreferences myPreferences =
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String trainerCode = myPreferences.getString("trainerCode", "");
                        if(!trainerCode.equals("false"))
                        {
                            Intent trainerIntent = new Intent(getApplicationContext(), TrainerMenuActivity.class);
                            startActivity(trainerIntent);
                        }
                        else
                        {
                            Intent becomeTrainerIntent = new Intent(getApplicationContext(), BecomeTrainerActivity.class);
                            startActivity(becomeTrainerIntent);
                        }
                        break;
                }
                return false;

            }
        });
    }
}
