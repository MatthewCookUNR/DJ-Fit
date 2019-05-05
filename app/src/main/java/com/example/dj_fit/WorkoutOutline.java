package com.example.dj_fit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
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

        container = findViewById(R.id.relativeScroll);
        hrEdit = findViewById(R.id.hrEdit);
        restPeriodEdit = findViewById(R.id.restPeriodEdit);
        repRangeEdit = findViewById(R.id.repRangeEdit);
        btnSaveOutline = findViewById(R.id.btnWorkoutOutline);
        btnAddDay = findViewById(R.id.btnAddDay);

        btnAddDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView mText = new TextView(WorkoutOutline.this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = 85;
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                mText.setId(integer);
                mText.setText("Monday");
                mText.setTextAppearance(WorkoutOutline.this, android.R.style.TextAppearance_Large);
                mText.setTextSize(30);
                mText.setGravity(Gravity.CENTER);
                mText.setLayoutParams(params);
                container.addView(mText);
                integer++;

                TableLayout newTable = new TableLayout(WorkoutOutline.this);
                TableRow newRow1 = new TableRow(WorkoutOutline.this);

                TextView exerText = new TextView(WorkoutOutline.this);
                exerText.setGravity(Gravity.CENTER);
                exerText.setTextSize(10);
                exerText.setText("Exercises");
                exerText.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView targetText = new TextView(WorkoutOutline.this);
                targetText.setGravity(Gravity.CENTER);
                targetText.setTextSize(10);
                targetText.setText("Target");
                targetText.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView minText = new TextView(WorkoutOutline.this);
                minText.setGravity(Gravity.CENTER);
                minText.setTextSize(10);
                minText.setText("Min Weight");
                minText.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView maxText = new TextView(WorkoutOutline.this);
                maxText.setGravity(Gravity.CENTER);
                maxText.setTextSize(10);
                maxText.setText("Max Weight");
                maxText.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

                EditText edit1 = new EditText(WorkoutOutline.this);


                TableLayout.LayoutParams params2 = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                newTable.setLayoutParams(params2);
                newRow1.addView(exerText);
                newRow1.addView(targetText);
                newRow1.addView(minText);
                newRow1.addView(maxText);

                newTable.addView(newRow1);
                container.addView(newTable);

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
