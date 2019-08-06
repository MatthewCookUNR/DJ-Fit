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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

// Main Activity Class ////////////////////////////////////////////////////////////////

public class MainActivity extends BaseActivity {

    //Class variables
    private static final String TAG = "MainActivity";
    RelativeLayout activity_main;
    private Button btnBackground, btnWorkoutOutline, btnRegisterTrainer, btnFindTrainer;

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
        btnRegisterTrainer = findViewById(R.id.btnRegisterTrainer);
        btnFindTrainer = findViewById(R.id.btnFindTrainer);

        //Checks to see if the user is currently a trainer
        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String trainerCode = myPreferences.getString("trainerCode", "");

        if(!trainerCode.equals("false"))
        {
            System.out.println("yes" + trainerCode);
            adjustUI();
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


        btnFindTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findTrainerIntent = new Intent(MainActivity.this, FindTrainerActivity.class);
                startActivity(findTrainerIntent);
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
        btnRegisterTrainer.setVisibility(View.GONE);
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
    }
}
