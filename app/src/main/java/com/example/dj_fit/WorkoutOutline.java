package com.example.dj_fit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class WorkoutOutline extends BaseActivity {

    private static int integer = 0;
    private RelativeLayout container;
    private EditText hrEdit, restPeriodEdit, repRangeEdit;
    private Button btnAddDay, btnSaveOutline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_outline);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //container = findViewById(R.id.activity_workout_outline);
        hrEdit = findViewById(R.id.hrEdit);
        restPeriodEdit = findViewById(R.id.restPeriodEdit);
        repRangeEdit = findViewById(R.id.repRangeEdit);
        btnSaveOutline = findViewById(R.id.btnWorkoutOutline);
        btnAddDay = findViewById(R.id.btnAddDay);

        btnAddDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView mText = new TextView(WorkoutOutline.this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, R.id.btnAddDay);
                params.setMargins(0,30,0,0);
                mText.setId(integer);
                mText.setText("Monday");
                mText.setTextAppearance(WorkoutOutline.this, android.R.style.TextAppearance_Large);
                mText.setLayoutParams(params);
                container.addView(mText);
                integer++;
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
