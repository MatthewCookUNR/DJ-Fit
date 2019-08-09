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
import android.support.design.internal.BottomNavigationItemView;
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
    private Button btnBackground, btnWorkoutOutline, btnFindTrainer;

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

        BottomNavigationView bottomNavigationItemView = findViewById(R.id.bottomNavigationItemView);
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch(menuItem.getItemId())
                {
                    case R.id.ic_back:
                        break;
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

    // Function definitions ////////////////////////////////////////////////////////

    /*
     *@Name: Adjust UI
     *
     *@Purpose: Change UI based on if user is a trainer
     *
     *@Param N/A
     *
     *@Brief: Function makes the trainer menu button visisble and removes the register
     *        as trainer button
     *
     *@ErrorsHandled: N/A
     */
    private void adjustUI()
    {
        /*
        Button btnTrainerMenu = new Button(MainActivity.this);
        RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsR.addRule(RelativeLayout.BELOW, R.id.btnFindTrainer);
        String myText = "Trainer Menu";
        btnTrainerMenu.setLayoutParams(paramsR);
        btnTrainerMenu.setTextAppearance(this, android.R.style.TextAppearance_Large);
        btnTrainerMenu.setTransformationMethod(null);
        btnTrainerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trainerMenuIntent = new Intent(MainActivity.this, TrainerMenuActivity.class);
                startActivity(trainerMenuIntent);
            }
        });
        btnTrainerMenu.setText(myText);
        activity_main.addView(btnTrainerMenu);
        */
    }
}
