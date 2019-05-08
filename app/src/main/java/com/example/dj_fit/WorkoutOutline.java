package com.example.dj_fit;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class WorkoutOutline extends BaseActivity {

    private int integer = 1;
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

        //Handles the on click function of adding a day to the workout outline
        btnAddDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Creates Textview representing day of a particular workout (Mon-Sun)
                TextView mText = new TextView(WorkoutOutline.this);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

                //Creates a table to outline workout
                TableLayout newTable = new TableLayout(WorkoutOutline.this);
                TableRow newRow1 = new TableRow(WorkoutOutline.this);
                TableRow newRow2 = new TableRow(WorkoutOutline.this);


                //Creates first row that defines what is in each column
                TextView exerTitle = new TextView(WorkoutOutline.this);
                exerTitle.setGravity(Gravity.CENTER);
                exerTitle.setTextSize(10);
                exerTitle.setText("Exercise");
                TableRow.LayoutParams paramColumn1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, .60f);
                exerTitle.setLayoutParams(paramColumn1);

                TextView targetTitle = new TextView(WorkoutOutline.this);
                targetTitle.setGravity(Gravity.CENTER);
                targetTitle.setTextSize(10);
                targetTitle.setText("Target");
                TableRow.LayoutParams paramColumn2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .20f);
                targetTitle.setLayoutParams(paramColumn2);

                TextView minTitle = new TextView(WorkoutOutline.this);
                minTitle.setGravity(Gravity.CENTER);
                minTitle.setTextSize(10);
                minTitle.setText("Min Weight");
                TableRow.LayoutParams paramColumn3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .1f);
                minTitle.setLayoutParams(paramColumn3);

                TextView maxTitle = new TextView(WorkoutOutline.this);
                maxTitle.setGravity(Gravity.CENTER);
                maxTitle.setTextSize(10);
                maxTitle.setText("Max Weight");
                TableRow.LayoutParams paramColumn4 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .1f);
                maxTitle.setLayoutParams(paramColumn4);

                //Create second row of table that shows what muscle group is targeted
                TextView exerType = new TextView(WorkoutOutline.this);
                exerType.setTextSize(18);
                exerType.setBackgroundResource(R.drawable.edit_border);
                exerType.setText("Chest");
                exerType.setTypeface(Typeface.DEFAULT_BOLD);
                TableRow.LayoutParams exerTypeParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                exerType.setLayoutParams(exerTypeParam);

                //Sets TableLayout below Textview for the desired day
                RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsR.addRule(RelativeLayout.BELOW, 1);
                paramsR.topMargin = 85;
                TableLayout.LayoutParams params2 = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                newTable.setLayoutParams(params2);

                //Adds elements of first row
                newRow1.addView(exerTitle);
                newRow1.addView(targetTitle);
                newRow1.addView(minTitle);
                newRow1.addView(maxTitle);

                //Adds elements of second row
                newRow2.addView(exerType);

                //Add rows to the table
                newTable.addView(newRow1);
                newTable.addView(newRow2);
                newTable.addView(createBaseRow(true, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
                newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
                newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
                newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
                newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));

                //Create the table on screen
                container.addView(newTable, paramsR);

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

    //Function returns a new row for the workout outline to be put in a table
    TableRow createBaseRow(boolean warmUp, TableRow.LayoutParams paramColumn1, TableRow.LayoutParams paramColumn2,
                           TableRow.LayoutParams paramColumn3, TableRow.LayoutParams paramColumn4)
    {
        TableRow baseRow = new TableRow(WorkoutOutline.this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        baseRow.setLayoutParams(params);

        EditText exerEdit = new EditText(WorkoutOutline.this);
        if(warmUp == true)
        {
            exerEdit.setHint("Exercise name \n(Warm up)");
        }
        else
        {
            exerEdit.setHint("Exercise name");
        }
        exerEdit.setTextSize(14);
        exerEdit.setLines(2);
        exerEdit.setSingleLine(false);
        exerEdit.setWidth(0);
        exerEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        exerEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        exerEdit.setBackgroundResource(R.drawable.edit_border);
        exerEdit.setLayoutParams(paramColumn1);

        TextView viewTarget = new TextView(WorkoutOutline.this);
        viewTarget.setText("View");
        viewTarget.setTextSize(14);
        viewTarget.isClickable();
        viewTarget.setBackgroundResource(R.drawable.edit_border);
        viewTarget.setGravity(Gravity.CENTER);
        viewTarget.setLayoutParams(paramColumn2);

        EditText minEdit = new EditText(WorkoutOutline.this);
        minEdit.setHint("#");
        minEdit.setTextSize(14);
        minEdit.setGravity(Gravity.CENTER);
        minEdit.setBackgroundResource(R.drawable.edit_border);
        minEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        minEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        minEdit.setLayoutParams(paramColumn3);

        EditText maxEdit = new EditText(WorkoutOutline.this);
        maxEdit.setHint("#");
        maxEdit.setTextSize(14);
        maxEdit.setGravity(Gravity.CENTER);
        maxEdit.setBackgroundResource(R.drawable.edit_border);
        maxEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        maxEdit.setLayoutParams(paramColumn3);

        baseRow.addView(exerEdit);
        baseRow.addView(viewTarget);
        baseRow.addView(minEdit);
        baseRow.addView(maxEdit);

        return baseRow;
    }

}
