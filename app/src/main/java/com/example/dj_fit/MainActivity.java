package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends BaseActivity {

    //Variables
    private Button btnBackground, btnWorkoutOutline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize layout variables
        btnBackground = findViewById(R.id.btnBackground);
        btnWorkoutOutline = findViewById(R.id.btnWorkoutOutline);



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



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
