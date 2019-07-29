package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
                startActivity(backgroundAct);
            }
        });

        btnWorkoutOutline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent workoutOutlineAct = new Intent(ClientProgramActivity.this, WorkoutOutlineActivity.class);
                workoutOutlineAct.putExtra("clientID", clientData[0]);
                startActivity(workoutOutlineAct);
            }
        });
    }

}
