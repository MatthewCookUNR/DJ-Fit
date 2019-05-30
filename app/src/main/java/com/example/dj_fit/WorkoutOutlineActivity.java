package com.example.dj_fit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;


public class WorkoutOutlineActivity extends BaseActivity {

    private final static String TAG = "WorkoutOutlineActivity";
    private int integer = 1;
    private int viewNum = 1;
    private final static int REQUEST_CODE_1 = 1;
    private RelativeLayout container;
    private EditText hrEdit, restPeriodEdit, repRangeEdit, setsEdit;
    private Button btnAddDay, btnSaveOutline, btnRemoveDay;
    private ImageView splashImage;
    private String [] dayList = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                         "Saturday", "Sunday"};
    private String [] muscleList = {"Chest", "Biceps", "Triceps", "Back", "Shoulders", "Legs", "Core", "Cardio"};
    private boolean [] daysShown = new boolean [dayList.length];
    private int dayChecked;
    private ArrayList<Integer> selectedMuscles = new ArrayList<>();
    private boolean [] musclesChecked;

    private ArrayList<workoutDay> workoutOutline = new ArrayList<>();
    private ArrayList<ArrayList<String>> videoViewz = new ArrayList<>();


    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseUser currentUser;

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
        setsEdit = findViewById(R.id.setsEdit);
        btnSaveOutline = findViewById(R.id.btnSaveOutline);
        btnAddDay = findViewById(R.id.btnAddDay);
        btnRemoveDay = findViewById(R.id.btnRemoveDay);
        splashImage = findViewById(R.id.splashImage);
        musclesChecked = new boolean[muscleList.length];
        Arrays.fill(daysShown, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        checkIfOutlineExists();

        //Handles the on click function of adding a day to the workout outline
        btnAddDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDayAlert();
            }
        });

        btnRemoveDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveDayAlert();
            }
        });

        //Button saves workout outline to the Firestore database
        btnSaveOutline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                //Variables needed
                String heartRate, restPeriod, repRange, numSets;
                workoutRow tempRow = new workoutRow();

                //Variables set that are same for each workout day
                heartRate = hrEdit.getText().toString();
                restPeriod = restPeriodEdit.getText().toString();
                repRange = repRangeEdit.getText().toString();
                numSets = setsEdit.getText().toString();

                //Organizes view videos for storage
                viewVideosToOutline();

                //Maps used for storage on
                Map<String, Object> rowMap = new HashMap<>();
                Map<String, Object> muscleMap = new HashMap<>();
                Map<String, Object> dayMap = new HashMap<>();
                Map<String, Object> workoutOutlineMap = new HashMap<>();

                //Puts shared variables in outline
                workoutOutlineMap.put("heartRate", heartRate);
                workoutOutlineMap.put("restPeriod", restPeriod);
                workoutOutlineMap.put("repRange", repRange);
                workoutOutlineMap.put("numSets", numSets);

                //Handles adding all of the workout days into a organized workout outline map
                int dayOrder = 1;
                int rowNum = 1;
                int muscleNum = 0;
                for(int i = 0; i < workoutOutline.size(); i++)
                {
                    //System.out.println("Day: " + workoutOutline.get(i).getDay());
                    //System.out.println("Exercise num is: " + workoutOutline.get(i).getExercise().size());
                    //System.out.println("Videolist num is: " + workoutOutline.get(i).getViewVideosList().size());
                    for (int j = 1; j < workoutOutline.get(i).getExercise().size()+1; j++)
                    {
                        //System.out.println("i is:" + i);
                        //System.out.println("j is: " + j);
                        tempRow.setExercise(workoutOutline.get(i).getExercise().get(j-1).getText().toString());
                        tempRow.setMinWeight(workoutOutline.get(i).getMinWeight().get(j-1).getText().toString());
                        tempRow.setMaxWeight(workoutOutline.get(i).getMaxWeight().get(j-1).getText().toString());
                        tempRow.setVideoList(workoutOutline.get(i).getViewVideosList().get(j-1));
                        rowMap.put("row" + rowNum, new workoutRow(tempRow));
                        rowNum++;
                        //Mod 5 is used since each muscle group has a max of 5 exercises
                        if(j % 5 == 0)
                        {
                            rowMap.put("order", muscleNum + 1);
                            muscleMap.put(workoutOutline.get(i).getMusclesUsed().get(muscleNum), new HashMap<>(rowMap));
                            muscleMap.put("dayOrder", dayOrder );
                            rowMap.clear();
                            muscleNum++;
                            rowNum = 1;
                        }
                    }
                    dayMap.put(workoutOutline.get(i).getDay(), new HashMap<>(muscleMap));
                    dayOrder++;
                    muscleMap.clear();
                    muscleNum = 0;
                }
                System.out.println(dayMap);
                //workoutOutlineMap.put("Workout", dayMap);

                //Last part of function that puts workout outline data onto the cloud database
                String userID = currentUser.getUid();
                final long start = System.currentTimeMillis();

                mDatabase.collection("users").document(userID).collection("fitnessData")
                        .document("workoutOutline")
                        .set(workoutOutlineMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "Document Snapshot added w/ time : " + (end - start) );
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
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

    //Adds a new day to the workout outline
    void addDayToOutline(String selectedDay, ArrayList<Integer> selectedMuscles)
    {
        workoutOutline.add(new workoutDay());
        workoutOutline.get(workoutOutline.size()-1).setDay(selectedDay);

        //Creates Textview representing day of a particular workout (Mon-Sun)
        TextView mText = new TextView(WorkoutOutlineActivity.this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 85;
        if(integer > 1)
        {
            params.addRule(RelativeLayout.BELOW, integer - 1);

        }
        mText.setId(integer);
        mText.setText(selectedDay);
        mText.setTextAppearance(WorkoutOutlineActivity.this, android.R.style.TextAppearance_Large);
        mText.setTextSize(30);
        mText.setGravity(Gravity.CENTER);
        mText.setLayoutParams(params);
        workoutOutline.get(workoutOutline.size()-1).setDayView(mText);
        container.addView(mText);
        integer++;

        //Creates a table to outline workout
        TableLayout newTable = new TableLayout(WorkoutOutlineActivity.this);
        TableRow newRow1 = new TableRow(WorkoutOutlineActivity.this);


        //Creates first row that defines what is in each column
        TextView exerTitle = new TextView(WorkoutOutlineActivity.this);
        exerTitle.setGravity(Gravity.CENTER);
        exerTitle.setTextSize(10);
        exerTitle.setText("Exercise");
        TableRow.LayoutParams paramColumn1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, .60f);
        exerTitle.setLayoutParams(paramColumn1);
        workoutOutline.get(workoutOutline.size()-1).setExerTitle(exerTitle);

        TextView viewTitle = new TextView(WorkoutOutlineActivity.this);
        viewTitle.setGravity(Gravity.CENTER);
        viewTitle.setTextSize(10);
        viewTitle.setText("Videos");
        TableRow.LayoutParams paramColumn2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .20f);
        viewTitle.setLayoutParams(paramColumn2);
        workoutOutline.get(workoutOutline.size()-1).setViewTitle(viewTitle);

        TextView minTitle = new TextView(WorkoutOutlineActivity.this);
        minTitle.setGravity(Gravity.CENTER);
        minTitle.setTextSize(10);
        minTitle.setText("Min Weight");
        TableRow.LayoutParams paramColumn3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .1f);
        minTitle.setLayoutParams(paramColumn3);
        workoutOutline.get(workoutOutline.size()-1).setMinTitle(minTitle);


        TextView maxTitle = new TextView(WorkoutOutlineActivity.this);
        maxTitle.setGravity(Gravity.CENTER);
        maxTitle.setTextSize(10);
        maxTitle.setText("Max Weight");
        TableRow.LayoutParams paramColumn4 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .1f);
        maxTitle.setLayoutParams(paramColumn4);
        workoutOutline.get(workoutOutline.size()-1).setMaxTitle(maxTitle);


        //Adds elements of first row
        newRow1.addView(exerTitle);
        newRow1.addView(viewTitle);
        newRow1.addView(minTitle);
        newRow1.addView(maxTitle);

        //Sets TableLayout below Textview for the desired day
        RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsR.addRule(RelativeLayout.BELOW, integer - 1);
        paramsR.topMargin = 85;
        TableLayout.LayoutParams params2 = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newTable.setLayoutParams(params2);

        //Add first row with table labels
        newTable.addView(newRow1);

        for(int i = 0; i < selectedMuscles.size(); i++)
        {
            String currentMuscle = muscleList[selectedMuscles.get(i)];
            workoutOutline.get(workoutOutline.size()-1).addMuscleUsed(currentMuscle);

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
        workoutOutline.get(workoutOutline.size()-1).setMyTable(newTable);
        //Create the table on screen
        container.addView(newTable, paramsR);
    }

    //Creates a clickable textview to be used in the workout outline
    TextView createViewButton()
    {
        final TextView viewButton = new TextView(WorkoutOutlineActivity.this);
        viewButton.setText("View");
        viewButton.setId(viewNum);
        viewNum++;
        viewButton.setTextSize(14);
        viewButton.isClickable();
        viewButton.setBackgroundResource(R.drawable.edit_border);
        viewButton.setGravity(Gravity.CENTER);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent popUp = new Intent(WorkoutOutlineActivity.this, PopActivity.class);
                int butID = viewButton.getId();
                popUp.putExtra("id", butID);
                popUp.putStringArrayListExtra("videos", videoViewz.get(butID-1));
                startActivityForResult(popUp, REQUEST_CODE_1);
            }
        });
        return viewButton;
    }

    //Returns table row that describes the muscle group hit by the following exercises
    TableRow createMuscleTypeRow(String selectedMuscle)
    {
        TableRow muscleRow = new TableRow(WorkoutOutlineActivity.this);
        TextView exerType = new TextView(WorkoutOutlineActivity.this);
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
        TableRow baseRow = new TableRow(WorkoutOutlineActivity.this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        baseRow.setLayoutParams(params);

        EditText exerEdit = new EditText(WorkoutOutlineActivity.this);
        if(warmUp)
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

        EditText minEdit = new EditText(WorkoutOutlineActivity.this);
        minEdit.setHint("#");
        minEdit.setTextSize(14);
        minEdit.setGravity(Gravity.CENTER);
        minEdit.setBackgroundResource(R.drawable.edit_border);
        minEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        minEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        minEdit.setLayoutParams(paramColumn3);

        EditText maxEdit = new EditText(WorkoutOutlineActivity.this);
        maxEdit.setHint("#");
        maxEdit.setTextSize(14);
        maxEdit.setGravity(Gravity.CENTER);
        maxEdit.setBackgroundResource(R.drawable.edit_border);
        maxEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        maxEdit.setLayoutParams(paramColumn3);

        workoutOutline.get(workoutOutline.size()-1).addExercise(exerEdit);
        videoViewz.add(new ArrayList<String>());
        workoutOutline.get(workoutOutline.size()-1).addMinWeight(minEdit);
        workoutOutline.get(workoutOutline.size()-1).addMaxWeight(maxEdit);

        baseRow.addView(exerEdit);
        baseRow.addView(viewTarget);
        baseRow.addView(minEdit);
        baseRow.addView(maxEdit);

        return baseRow;
    }

    //Function handles data received from pop up activity (video list)
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
                    ArrayList<String> tempVideoList;
                    tempVideoList = dataIntent.getStringArrayListExtra("videos");
                    int tempID = dataIntent.getIntExtra("id", 0);
                    videoViewz.get(tempID-1).addAll(tempVideoList);
                }
        }
    }

    void removeDayFromOutline(String day)
    {
        int index = 0;
        for(int i = 0; i < workoutOutline.size(); i++)
        {
            if(workoutOutline.get(i).getDay().equals(day))
            {
                index = i;
                System.out.println(i);
                break;
            }
        }
        workoutOutline.get(index).destroyViews();
        workoutOutline.remove(index);
    }


    //Function repopulates the page with existing workout outline
    void populateOutline(Map<String, Object> docData)
    {
        //Puts shared variables in outline
        hrEdit.setText(docData.get("heartRate").toString());
        setsEdit.setText(docData.get("numSets").toString());
        repRangeEdit.setText(docData.get("repRange").toString());
        restPeriodEdit.setText(docData.get("restPeriod").toString());

        ArrayList<String> tempMuscleOneDay = new ArrayList<>();
        ArrayList<ArrayList<String>>  tempMuscles = new ArrayList<>();
        ArrayList<String> tempDays = new ArrayList<>();

        for(int i = 0; i < 10; i++)
        {
            tempDays.add(null);
            tempMuscles.add(null);
            tempMuscleOneDay.add(null);
        }

        int dayIndex = 0;
        int p = 0;
        Iterator it, it2, it3;
        String tempDay;
        String tempMuscle;

        //Section of function organizes the data in the correct order
        if (docData.get("Workout") != null);
        {
            //Iterate through each day
            it = ((HashMap)docData.get("Workout")).entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry) it.next();
                tempDay = pair.getKey().toString();
                checkDay(tempDay);

                //Iterate through muscle groups in each day (includes day order)
                it2 = ((HashMap) ((HashMap)docData.get("Workout")).get(pair.getKey())).entrySet().iterator();
                while(it2.hasNext())
                {
                    Map.Entry pair2 = (Map.Entry) it2.next();

                    //Check to see if current key is for day order
                    if(pair2.getKey().toString().equals("dayOrder"))
                    {
                        dayIndex = Integer.parseInt(pair2.getValue().toString())-1;
                        tempDays.set(Integer.parseInt(pair2.getValue().toString())-1, tempDay);
                    }
                    else
                    {
                        tempMuscle = pair2.getKey().toString();
                        it3 = ((HashMap) ((HashMap) ((HashMap) docData.get("Workout")).get(pair.getKey())).get(pair2.getKey())).entrySet().iterator();

                        //Iterate through the inside of muscle groups (rows including order of muscle groups)
                        while (it3.hasNext())
                        {
                            Map.Entry pair3 = (Map.Entry) it3.next();

                            //Check to see if current key is for muscle order
                            if(pair3.getKey().toString().equals("order"))
                            {
                                tempMuscleOneDay.set(Integer.parseInt(pair3.getValue().toString())-1, tempMuscle);
                            }
                        }
                    }
                }
                tempMuscles.set(dayIndex, new ArrayList<>(tempMuscleOneDay));
            }
        }

        splashImage.setVisibility(View.GONE);

        //Section of function creates outline and sets data to correct spots in the generated outline
        int t = 0;
        int viewIndex = 0;
        int outlineIndex = 0;
        String exercise, minWeight, maxWeight;
        ArrayList<String> viewVids;

        //p is index of list of days
        while(tempDays.get(p) != null)
        {
            addDayToOutline(tempDays.get(p), convertMuscles(tempMuscles.get(p)));
            //t is index of list of muscles
            while( tempMuscles.get(p).get(t) != null)
            {
                //i is for the number of rows for each muscle group
                for (int i = 0; i < 5; i++)
                {
                    exercise = ((HashMap) ((HashMap) ((HashMap) ((HashMap) docData.get("Workout")).get(tempDays.get(p))).get(tempMuscles.get(p).get(t))).get("row" + (i + 1))).get("exercise").toString();
                    minWeight = ((HashMap) ((HashMap) ((HashMap) ((HashMap) docData.get("Workout")).get(tempDays.get(p))).get(tempMuscles.get(p).get(t))).get("row" + (i + 1))).get("minWeight").toString();
                    maxWeight = ((HashMap) ((HashMap) ((HashMap) ((HashMap) docData.get("Workout")).get(tempDays.get(p))).get(tempMuscles.get(p).get(t))).get("row" + (i + 1))).get("maxWeight").toString();
                    viewVids = (ArrayList<String>) (((HashMap) ((HashMap) ((HashMap) ((HashMap) docData.get("Workout")).get(tempDays.get(p))).get(tempMuscles.get(p).get(t))).get("row" + (i + 1))).get("videoList"));
                    videoViewz.set(viewIndex, viewVids);
                    viewIndex++;

                    workoutOutline.get(p).getExercise().get(outlineIndex).setText(exercise);
                    workoutOutline.get(p).getMinWeight().get(outlineIndex).setText(minWeight);
                    workoutOutline.get(p).getMaxWeight().get(outlineIndex).setText(maxWeight);
                    outlineIndex++;
                }
                t++;
            }
            t = 0;
            outlineIndex = 0;
            p++;
        }
    }

    //Checks if workout outline exists and retrieves it for repopulating the page
    void checkIfOutlineExists()
    {
        final long start = System.currentTimeMillis();

        String userID = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = mDatabase.collection("users").document(userID).collection("fitnessData").document("workoutOutline");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null)
                {
                    Log.w(TAG, "Listen failed", e);
                }

                if (documentSnapshot != null && documentSnapshot.exists())
                {
                    long end = System.currentTimeMillis();
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    Log.d(TAG, "Logged at " + (end - start));
                    populateOutline(documentSnapshot.getData());
                    end = System.currentTimeMillis();
                    Log.d(TAG, "Populate Logged at " + (end - start));
                }
                else
                {
                    Log.d (TAG, "Current data: null");
                    splashImage.setVisibility(View.GONE);
                }
            }
        });
    }

    //Function handles the popup alert that appears after clicking the "Add Day to Routine" button
    void showAddDayAlert()
    {
        //Alert asks user to select what muscle group(s) they wish to work out on chosen day
        final AlertDialog.Builder muscleBuilder = new AlertDialog.Builder(WorkoutOutlineActivity.this);
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
        AlertDialog.Builder dayBuilder = new AlertDialog.Builder(WorkoutOutlineActivity.this);
        dayBuilder.setTitle("Select what day the workout will be on");
        dayBuilder.setSingleChoiceItems(dayList, dayChecked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dayChecked = which;
            }
        });

        dayBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(daysShown[dayChecked])
                {
                    Toast mToast = Toast.makeText(WorkoutOutlineActivity.this, "Day already shown", Toast.LENGTH_SHORT);
                    mToast.show();
                    showAddDayAlert();
                }
                else
                {
                    daysShown[dayChecked] = true;
                    AlertDialog muscleDialog = muscleBuilder.create();
                    muscleDialog.show();
                }
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

    //Function handles the popup alert that appears after clicking the "Add Day to Routine" button
    void showRemoveDayAlert()
    {
        //Alert asks user what day they would like to remove from the workout outline
        AlertDialog.Builder dayBuilder = new AlertDialog.Builder(WorkoutOutlineActivity.this);
        dayBuilder.setTitle("Select what day you'd like to remove");
        dayBuilder.setSingleChoiceItems(dayList, dayChecked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dayChecked = which;
            }
        });

        dayBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(daysShown[dayChecked])
                {
                    daysShown[dayChecked] = false;
                    removeDayFromOutline(dayList[dayChecked]);
                }
                else
                {
                    Toast mToast = Toast.makeText(WorkoutOutlineActivity.this, "Day is not in the outline", Toast.LENGTH_SHORT);
                    mToast.show();
                    showRemoveDayAlert();

                }
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


    //Function adds videos stored for "View" buttons to the workoutOutline object for use
    //of storing them on the cloud
    void viewVideosToOutline()
    {
        int i = 0;
        int j = 1;
        int mod = 1;
        int muscleNum;
        while(i < workoutOutline.size())
        {
            workoutOutline.get(i).clearViewVideosList();
            muscleNum = workoutOutline.get(i).getMusclesUsed().size();
            //System.out.println("Muscle num is: " + muscleNum);
            while( j < videoViewz.size()+1 )
            {
                //Loop uses mod 5 to distinguish between muscle groups since each group has 5
                //exercises possible
                if(mod % (5*muscleNum) == 0 && j != 0)
                {
                    //System.out.println("j during break: " + j);
                    //System.out.println("mod during break" + mod);
                    workoutOutline.get(i).addViewVideos(videoViewz.get(j-1));
                    i++;
                    j++;
                    mod++;
                    break;
                }
                workoutOutline.get(i).addViewVideos(videoViewz.get(j-1));
                mod++;
                j++;
            }
            mod = 1;
        }
        //System.out.println("Final j is: " + j);
    }

    //Function finds given day in list of days and marks it as in outline to avoid duplicate days
    void checkDay(String day)
    {
        for(int p = 0; p < dayList.length; p++)
        {
            if(dayList[p].equals(day))
            {
                daysShown[p] = true;
                break;
            }
        }
    }

    //Function finds which integer in muscleList correlates with muscle in given array
    //Note: Function is used for populating existing outline
    ArrayList<Integer> convertMuscles(ArrayList<String> musclesArray)
    {
        ArrayList<Integer> intMuscles = new ArrayList<>();
        int i = 0;
        while ( musclesArray.get(i) != null)
        {
            for(int z = 0; z < muscleList.length; z++)
            {
                if(muscleList[z].equals(musclesArray.get(i)))
                {
                    intMuscles.add(z);
                    i++;
                }
            }
        }
        return intMuscles;
    }
}