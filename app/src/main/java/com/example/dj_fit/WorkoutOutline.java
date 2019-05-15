package com.example.dj_fit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class WorkoutOutline extends BaseActivity {

    private int integer = 1;
    private final static int REQUEST_CODE_1 = 1;
    private RelativeLayout container;
    private EditText hrEdit, restPeriodEdit, repRangeEdit;
    private Button btnAddDay, btnSaveOutline;
    String [] dayList = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                         "Saturday", "Sunday"};
    String [] muscleList = {"Chest", "Biceps", "Triceps", "Back", "Shoulders", "Legs", "Core", "Cardio"};
    boolean [] daysShown = new boolean [dayList.length];
    int dayChecked;
    ArrayList<Integer> selectedMuscles = new ArrayList<>();
    boolean [] musclesChecked;

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
        musclesChecked = new boolean[muscleList.length];
        Arrays.fill(daysShown, Boolean.FALSE);

        //Handles the on click function of adding a day to the workout outline
        btnAddDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Alert asks user to select what muscle group(s) they wish to work out on chosen day
                final AlertDialog.Builder muscleBuilder = new AlertDialog.Builder(WorkoutOutline.this);
                muscleBuilder.setTitle("Select what muscle group(s) will be targeted");
                muscleBuilder.setMultiChoiceItems(muscleList, musclesChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked)
                        {
                            if(! selectedMuscles.contains(which))
                            {
                                selectedMuscles.add(which);
                            }
                        }
                        else if(selectedMuscles.contains(which))
                        {
                            selectedMuscles.remove(selectedMuscles.indexOf(which));
                        }
                    }
                });
                muscleBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        addDayToOutline(dayList[dayChecked], selectedMuscles);
                    }
                });
                muscleBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                //Alert asks user what day they would like to add to the workout outline
                AlertDialog.Builder dayBuilder = new AlertDialog.Builder(WorkoutOutline.this);
                dayBuilder.setTitle("Select what day the workout will be on");
                dayBuilder.setSingleChoiceItems(dayList, dayChecked, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dayChecked = which;
                        if(daysShown[dayChecked])
                        {
                            Toast mToast = Toast.makeText(WorkoutOutline.this, "Day already shown", Toast.LENGTH_SHORT);
                            mToast.show();
                        }
                    }
                });

                dayBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        daysShown[dayChecked] = true;
                        AlertDialog muscleDialog = muscleBuilder.create();
                        muscleDialog.show();
                    }
                });
                dayBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


                AlertDialog mDialog = dayBuilder.create();
                mDialog.show();

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

    void addDayToOutline(String selectedDay, ArrayList<Integer> selectedMuscles)
    {
        //Creates Textview representing day of a particular workout (Mon-Sun)
        TextView mText = new TextView(WorkoutOutline.this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 85;
        if(integer > 1)
        {
            params.addRule(RelativeLayout.BELOW, integer - 1);

        }
        mText.setId(integer);
        mText.setText(selectedDay);
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
        targetTitle.setText("Videos");
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

        //Adds elements of first row
        newRow1.addView(exerTitle);
        newRow1.addView(targetTitle);
        newRow1.addView(minTitle);
        newRow1.addView(maxTitle);

        //Sets TableLayout below Textview for the desired day
        RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsR.addRule(RelativeLayout.BELOW, integer - 1);
        paramsR.topMargin = 85;
        TableLayout.LayoutParams params2 = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newTable.setLayoutParams(params2);


        //Create second row of table that shows what muscle group is targeted


        //Add first row with table labels
        newTable.addView(newRow1);

        for(int i = 0; i < selectedMuscles.size(); i++)
        {
            String currentMuscle = muscleList[selectedMuscles.get(i)];

            //Add rows to the table
            newTable.addView(createMuscleTypeRow(currentMuscle));
            newTable.addView(createBaseRow(true, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
            newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
            newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
            newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));
            newTable.addView(createBaseRow(false, paramColumn1, paramColumn2, paramColumn3, paramColumn4));

        }

        newTable.setId(integer);
        integer++;
        //Create the table on screen
        container.addView(newTable, paramsR);
    }

    //Creates a clickable textview to be used in the workout outline
    TextView createViewButton()
    {
        TextView viewButton = new TextView(WorkoutOutline.this);
        viewButton.setText("View");
        viewButton.setTextSize(14);
        viewButton.isClickable();
        viewButton.setBackgroundResource(R.drawable.edit_border);
        viewButton.setGravity(Gravity.CENTER);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popUp = new Intent(WorkoutOutline.this, PopActivity.class);
                popUp.putExtra("Link", "I need youtube link");
                startActivityForResult(popUp, REQUEST_CODE_1);
            }
        });
        return viewButton;
    }

    //Returns table row that describes the muscle group hit by the following exercises
    TableRow createMuscleTypeRow(String selectedMuscle)
    {
        TableRow muscleRow = new TableRow(WorkoutOutline.this);

        TextView exerType = new TextView(WorkoutOutline.this);
        exerType.setTextSize(18);
        exerType.setText(selectedMuscle);
        exerType.setBackgroundResource(R.drawable.edit_border);
        exerType.setTypeface(Typeface.DEFAULT_BOLD);
        TableRow.LayoutParams exerTypeParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        exerType.setLayoutParams(exerTypeParam);

        muscleRow.addView(exerType);
        return muscleRow;
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

        TextView viewTarget = createViewButton();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent)
    {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // The returned result data is identified by requestCode.
        // The request code is specified in startActivityForResult(intent, REQUEST_CODE_1); method.
        switch (requestCode)
        {
            // This request code is set by startActivityForResult(intent, REQUEST_CODE_1) method.
            case REQUEST_CODE_1:
                if(resultCode == RESULT_OK)
                {
                    ArrayList<String> test = dataIntent.getStringArrayListExtra("videos");
                }
        }
    }

}
