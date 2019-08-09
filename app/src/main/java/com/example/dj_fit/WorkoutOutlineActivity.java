// Program Information /////////////////////////////////////////////////////////
/*
 * @file WorkoutOutlineActivity.java
 *
 * @brief Activity is used to create and modify the user's workout outline
 *
 * @author Matthew Cook
 * @author Collin Potter
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Workout Outline Activity Class ////////////////////////////////////////////////////////////////

public class WorkoutOutlineActivity extends BaseActivity {

    //Class variables
    private final static String TAG = "WorkoutOutlineActivity";
    private boolean isOwner = false;
    private String userID;
    private int integer = 1;
    private int viewNum = 1;
    private final static int REQUEST_CODE_1 = 1;
    private RelativeLayout container, topContent, botButtons;
    private FloatingActionButton fab;
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
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_outline);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final long start = System.currentTimeMillis();

        //Views and variables initialization
        container = findViewById(R.id.relativeScroll);
        hrEdit = findViewById(R.id.hrEdit);
        restPeriodEdit = findViewById(R.id.restPeriodEdit);
        repRangeEdit = findViewById(R.id.repRangeEdit);
        setsEdit = findViewById(R.id.setsEdit);
        btnSaveOutline = findViewById(R.id.btnSaveOutline);
        btnAddDay = findViewById(R.id.btnAddDay);
        btnRemoveDay = findViewById(R.id.btnRemoveDay);
        splashImage = findViewById(R.id.splashImage);
        topContent = findViewById(R.id.topContent);
        botButtons = findViewById(R.id.botButtons);
        musclesChecked = new boolean[muscleList.length];
        Arrays.fill(daysShown, false);

        final RotateAnimation rotateAnimation = new RotateAnimation(0f, 720f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        splashImage.startAnimation(rotateAnimation);

        //Checks to see if viewer is the user itself or a trainer
        userID = getIntent().getStringExtra("clientID");
        if(userID == null)
        {
            isOwner = true;
            userID = FirebaseAuth.getInstance().getUid();
        }

        mDatabase = FirebaseFirestore.getInstance();

        final long end = System.currentTimeMillis();
        checkIfOutlineExists();
        System.out.println("Time initialize: " + (end - start));

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
                //Puts workout outline data onto the cloud database
                final long start = System.currentTimeMillis();

                mDatabase.collection("users").document(userID).collection("fitnessData")
                        .document("workoutOutline")
                        .set(createOutlineMap())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "Document Snapshot added w/ time : " + (end - start) );
                        Toast.makeText(getApplicationContext(), "Save Successful!", Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                                Toast.makeText(getApplicationContext(), "Save Failure!", Toast.LENGTH_SHORT).show();
                            }
                        });
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
                        if(isOwner)
                        {
                            Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(homeIntent);
                        }
                        else
                        {
                            Intent clientIntent = new Intent(getApplicationContext(), ClientProgramActivity.class);
                            clientIntent.putExtra("clientTag", getIntent().getStringExtra("clientTag"));
                            startActivity(clientIntent);
                        }

                        break;
                    case R.id.ic_home:
                        Intent homeIntent2 = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent2);
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
     *@Name: Create Outline Map
     *
     *@Purpose: Creates a map of workout outline data
     *
     *@Param out: Map of workout outline data
     *
     *@Brief: Retrieves data from workout outline and then organizes it into a complex,
     *        nested Map.
     *
     *@ErrorsHandled: N/A
     */

    private Map createOutlineMap()
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
            for (int j = 1; j < workoutOutline.get(i).getExercise().size()+1; j++)
            {
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
        workoutOutlineMap.put("Workout", dayMap);
        return workoutOutlineMap;
    }

    /*
     *@Name: Add Day to Outline
     *
     *@Purpose: Adds views for a new day added to workout outline
     *
     *@Param in: String representing day being added (selectedDay)
     *       in: ArrayList of integers representing muscle groups
     *       selected (selectedMuscles)
     *
     *@Brief: Function creates views for the new day in the correct order (Mon-Sun)
     *        and adds it to the global list of workoutDays object for later data
     *        accessing
     *
     *@ErrorsHandled: N/A
     */
    private void addDayToOutline(String selectedDay, ArrayList<Integer> selectedMuscles)
    {
        //Find where the day is in order of days
        int selectedIndex = 0;
        for (int i = 0; i < dayList.length; i++)
        {
            if(selectedDay.equals(dayList[i]))
            {
                selectedIndex = i;
            }
        }

        //Find insert index for the selected day
        int insertIndex = findDayIndex(selectedIndex);

        //Add new day to end of outline if it is the last day
        //Otherwise, insert in correct spot
        if(workoutOutline.size() == insertIndex)
        {
            workoutOutline.add(new workoutDay());
        }
        else
        {
            workoutOutline.add(insertIndex, new workoutDay());
        }

        workoutOutline.get(insertIndex).setDay(selectedDay);
        workoutOutline.get(insertIndex).setDayOrderIndex(selectedIndex);

        //Creates TextView representing day of a particular workout (Mon-Sun)
        TextView mText = new TextView(WorkoutOutlineActivity.this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 85;

        //Case that this is the first day added
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
        workoutOutline.get(insertIndex).setDayView(mText);
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
        TableRow.LayoutParams paramExercise = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, .60f);
        exerTitle.setLayoutParams(paramExercise);
        workoutOutline.get(insertIndex).setExerTitle(exerTitle);

        TextView viewTitle = new TextView(WorkoutOutlineActivity.this);
        viewTitle.setGravity(Gravity.CENTER);
        viewTitle.setTextSize(10);
        viewTitle.setText("Videos");
        TableRow.LayoutParams paramVideos = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .20f);
        viewTitle.setLayoutParams(paramVideos);
        workoutOutline.get(insertIndex).setViewTitle(viewTitle);

        TextView minTitle = new TextView(WorkoutOutlineActivity.this);
        minTitle.setGravity(Gravity.CENTER);
        minTitle.setTextSize(10);
        minTitle.setText("Min Weight");
        TableRow.LayoutParams paramWeight = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, .1f);
        minTitle.setLayoutParams(paramWeight);
        workoutOutline.get(insertIndex).setMinTitle(minTitle);


        TextView maxTitle = new TextView(WorkoutOutlineActivity.this);
        maxTitle.setGravity(Gravity.CENTER);
        maxTitle.setTextSize(10);
        maxTitle.setText("Max Weight");
        maxTitle.setLayoutParams(paramWeight);
        workoutOutline.get(insertIndex).setMaxTitle(maxTitle);


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
            workoutOutline.get(insertIndex).addMuscleUsed(currentMuscle);

            //Add rows to the table
            newTable.addView(createMuscleTypeRow(currentMuscle));
            newTable.addView(createBaseRow(insertIndex, true, paramExercise, paramVideos, paramWeight));
            newTable.addView(createBaseRow(insertIndex, false, paramExercise, paramVideos, paramWeight));
            newTable.addView(createBaseRow(insertIndex, false, paramExercise, paramVideos, paramWeight));
            newTable.addView(createBaseRow(insertIndex,false, paramExercise, paramVideos, paramWeight));
            newTable.addView(createBaseRow(insertIndex,false, paramExercise, paramVideos, paramWeight));
        }

        newTable.setId(integer);
        integer++;
        workoutOutline.get(insertIndex).setMyTable(newTable);

        //Create the table on screen
        if(insertIndex != workoutOutline.size()-1)
        {
            remakeLayouts(insertIndex);
        }
        else
        {
            container.addView(newTable, paramsR);
        }
    }

    /*
     *@Name: Find Day Index
     *
     *@Purpose: Finds the index where the new day will be inserted
     *          in the outline
     *
     *@Param in: Integer representing where the selected index is in terms
     *           of order(Mon-Sun)
     *       out: Integer for where the new day should be inserted in outline
     *
     *@Brief: N/A
     *
     *@ErrorsHandled: N/A
     */
    private int findDayIndex(int selectedIndex)
    {
        int insertIndex = 0;

        //Finds index for where day will be added in current layout
        for(int z = 0; z < workoutOutline.size(); z++)
        {
            if(workoutOutline.get(z).getDayOrderIndex() < selectedIndex)
            {
                //Case: Add to end of outline
                if(z == workoutOutline.size()-1)
                {
                    insertIndex = z+1;
                    break;
                }
                //Case: Add after current day but before the following day
                else if (workoutOutline.get(z+1).getDayOrderIndex() > selectedIndex )
                {
                    insertIndex = z+1;
                    break;
                }
                //If neither pass, move to next day to check
            }
            //Case: Add before the entire list
            if(workoutOutline.get(z).getDayOrderIndex() > selectedIndex)
            {
                insertIndex = z;
                break;
            }
        }
        return insertIndex;
    }

    /*
     *@Name: Create View Button
     *
     *@Purpose: Creates a view button for the workout outline
     *
     *@Param out: Clickable TextView for viewing youtube links
     *
     *@Brief: Creates button that allows user to open a pop up showing
     *        youtube links that they have added for each exercise
     *
     *@ErrorsHandled: N/A
     */
    private TextView createViewButton()
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

    /*
     *@Name: Create Muscle Type Row
     *
     *@Purpose: Creates a view button for the workout outline
     *
     *@Param in: String for muscle that is being added (selectedMuscle)
     *       out: Table row showing muscle type
     *
     *@Brief: Returns new table row that will be placed in the outline that
     *        says what type of muscle it is
     *
     *@ErrorsHandled: N/A
     */
    private TableRow createMuscleTypeRow(String selectedMuscle)
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

    /*
     *@Name: Create Base Row
     *
     *@Purpose: Creates a base row for the workout outline
     *
     *@Param in: Integer for order that row is inserted (insertIndex)
     *       in: Boolean for whether row is warm up row (warmUp)
     *       in: Params for the exercise table column 1 (paramExercise)
     *       in: Params for the exercise table column 2 (paramVideos)
     *       in: Params for the exercise table column 3 (paramWeight)
     *       out: TableRow for new exercise added to workout
     *
     *@Brief: Returns new table row that will be placed in the outline that
     *        represents a new exercise in the the workout day
     *
     *@ErrorsHandled: N/A
     */
    private TableRow createBaseRow(int insertIndex, boolean warmUp, TableRow.LayoutParams paramExercise, TableRow.LayoutParams paramVideos,
                           TableRow.LayoutParams paramWeight)
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
        exerEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        exerEdit.setBackgroundResource(R.drawable.edit_border);
        exerEdit.setLayoutParams(paramExercise);

        TextView viewTarget = createViewButton();
        viewTarget.setLayoutParams(paramVideos);

        EditText minEdit = new EditText(WorkoutOutlineActivity.this);
        minEdit.setHint("#");
        minEdit.setTextSize(14);
        minEdit.setGravity(Gravity.CENTER);
        minEdit.setBackgroundResource(R.drawable.edit_border);
        minEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        minEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        minEdit.setLayoutParams(paramWeight);

        EditText maxEdit = new EditText(WorkoutOutlineActivity.this);
        maxEdit.setHint("#");
        maxEdit.setTextSize(14);
        maxEdit.setGravity(Gravity.CENTER);
        maxEdit.setBackgroundResource(R.drawable.edit_border);
        maxEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        maxEdit.setLayoutParams(paramWeight);

        workoutOutline.get(insertIndex).addExercise(exerEdit);
        videoViewz.add(insertIndex, new ArrayList<String>());
        workoutOutline.get(insertIndex).addMinWeight(minEdit);
        workoutOutline.get(insertIndex).addMaxWeight(maxEdit);

        baseRow.addView(exerEdit);
        baseRow.addView(viewTarget);
        baseRow.addView(minEdit);
        baseRow.addView(maxEdit);

        return baseRow;
    }

    /*
     *@Name: On Activity Result
     *
     *@Purpose: Handles receiving youtube links from pop up activity
     *
     *@Param in: Integer code telling what request was for (requestCode)
     *       in: Integer code telling status of result (resultCode)
     *       in: Data received from the intent (data)
     *
     *@Brief: Function checks what kind of request was received and if the result
     *        returned OK. If true, it stores the list of youtube links in videolist
     *
     *@ErrorsHandled: N/A
     */
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
                    System.out.println("Video list in main: " + tempVideoList);
                    videoViewz.get(tempID-1).clear();
                    videoViewz.get(tempID-1).addAll(tempVideoList);
                }
        }
    }

    /*
     *@Name: Remove Day from Outline
     *
     *@Purpose: Removes day from the outline
     *
     *@Param in: String for what day is being removed (day)
     *
     *@Brief: Function searches the workoutOutline object to find
     *        the given day. Then the day is removed from workoutOutline
     *
     *@ErrorsHandled: N/A
     */
    private void removeDayFromOutline(String day)
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

    /*
     *@Name: Sort Outline Data
     *
     *@Purpose: Sorts outline data received from Firestore
     *
     *@Param in: Document containing workout outline (docData)
     *
     *@Brief: Function organizes data in a way that makes it possible
     *        to populate the workout outline activity
     *
     *@ErrorsHandled: N/A
     */
    private void sortOutlineData(Map<String, Object> docData)
    {
        //Puts shared variables in outline
        hrEdit.setText(docData.get("heartRate").toString());
        setsEdit.setText(docData.get("numSets").toString());
        repRangeEdit.setText(docData.get("repRange").toString());
        restPeriodEdit.setText(docData.get("restPeriod").toString());

        ArrayList<String> tempMuscleOneDay = new ArrayList<>();
        ArrayList<ArrayList<String>>  tempMuscles = new ArrayList<>();
        ArrayList<String> tempDays = new ArrayList<>();

        for(int i = 0; i < 8; i++)
        {
            tempDays.add(null);
            tempMuscles.add(null);
            tempMuscleOneDay.add(null);
        }

        int dayIndex = 0;
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
                for(int i = 0; i < 8; i++)
                {
                    tempMuscleOneDay.set(i, null);
                }
            }
        }
        populateOutline(docData, tempDays, tempMuscles);
    }

    /*
     *@Name: Populate Outline
     *
     *@Purpose: Sorts outline data received from Firestore
     *
     *@Param in: Document data containing outline (docData)
     *       in: ArrayList of Strings containing workout days (tempDays)
     *       in: Nested ArrayList representing list of workouts
     *       for each day (tempMuscles)
     *
     *@Brief: Function adds the workout outline days to outline and
     *        populates them with the existing outline data
     *
     *@ErrorsHandled: N/A
     */
    void populateOutline( Map<String, Object> docData, ArrayList<String> tempDays,
                          ArrayList<ArrayList<String>>  tempMuscles)
    {
        //Section of function creates outline and sets data to correct spots in the generated outline
        int t = 0;
        int p = 0;
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
                    exercise = ((HashMap) ((HashMap) ((HashMap) ((HashMap) docData.get("Workout"))
                            .get(tempDays.get(p))).get(tempMuscles.get(p).get(t)))
                            .get("row" + (i + 1))).get("exercise").toString();
                    minWeight = ((HashMap) ((HashMap) ((HashMap) ((HashMap) docData.get("Workout"))
                            .get(tempDays.get(p))).get(tempMuscles.get(p).get(t)))
                            .get("row" + (i + 1))).get("minWeight").toString();
                    maxWeight = ((HashMap) ((HashMap) ((HashMap) ((HashMap) docData.get("Workout"))
                            .get(tempDays.get(p))).get(tempMuscles.get(p).get(t)))
                            .get("row" + (i + 1))).get("maxWeight").toString();
                    viewVids = (ArrayList<String>) (((HashMap) ((HashMap) ((HashMap) ((HashMap) docData
                            .get("Workout")).get(tempDays.get(p))).get(tempMuscles
                            .get(p).get(t))).get("row" + (i + 1))).get("videoList"));
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
        closeSplashScreen();
    }

    /*
     *@Name: Check if Outline Exists
     *
     *@Purpose: Checks to see if a outline has been saved already
     *
     *@Param N/A
     *
     *@Brief: Function tries to get the workout outline document and
     *        attempts to populate the page if one exists
     *
     *@ErrorsHandled: N/A
     */
    void checkIfOutlineExists()
    {
        final long start = System.currentTimeMillis();

        DocumentReference docRef = mDatabase.collection("users").document(userID).collection("fitnessData").document("workoutOutline");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Log.d(TAG, "Logged at " + (end - start));
                        sortOutlineData(document.getData());
                        end = System.currentTimeMillis();
                        Log.d(TAG, "Populate Logged at " + (end - start));
                    } else {
                        closeSplashScreen();
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /*
     *@Name: Show Add Day Alert
     *
     *@Purpose: Shows a alert that handles adding a day
     *
     *@Param N/A
     *
     *@Brief: Function opens a popup alert if the user clicks on
     *        the "Add Day"
     *
     *@ErrorsHandled: N/A
     */
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

    /*
     *@Name: Show Remove Day Alert
     *
     *@Purpose: Shows a alert that handles removing a day
     *
     *@Param N/A
     *
     *@Brief: Function opens a popup alert if the user clicks on
     *        the "Remove Day"
     *
     *@ErrorsHandled: N/A
     */
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

    /*
     *@Name: Remake Layouts
     *
     *@Purpose: Remakes the outline layout
     *
     *@Param in: Integer for where the new day will be added (insertIndex)
     *
     *@Brief: Function remakes the layout when a new day is inserting in the outline.
     *        Puts the day in the correct spot and then shifts the rest down
     *
     *@ErrorsHandled: N/A
     */
    void remakeLayouts(int insertIndex)
    {
        //First section inserts the new day into correct spot
        int titleIndex, tableIndex;
        int zIndex = insertIndex+1;
        titleIndex = workoutOutline.get(insertIndex+1).getDayView().getId();
        tableIndex = workoutOutline.get(insertIndex+1).getMyTable().getId();
        container.removeView(workoutOutline.get(insertIndex).getDayView());
        container.removeView(workoutOutline.get(insertIndex).getMyTable());
        workoutOutline.get(insertIndex).getDayView().setId(titleIndex);
        workoutOutline.get(insertIndex).getMyTable().setId(tableIndex);


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 85;
        params.addRule(RelativeLayout.BELOW, titleIndex-1);
        container.addView(workoutOutline.get(insertIndex).getDayView(), params);

        RelativeLayout.LayoutParams paramsT = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsT.topMargin = 85;
        paramsT.addRule(RelativeLayout.BELOW, titleIndex);
        container.addView(workoutOutline.get(insertIndex).getMyTable(), paramsT);

        //This loop shifts the IDs of the layout elements by 1 and recreates them
        while(zIndex < workoutOutline.size()-1)
        {
            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsT = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            titleIndex = (workoutOutline.get(zIndex+1).getDayView().getId());
            tableIndex = (workoutOutline.get(zIndex+1).getMyTable().getId());
            container.removeView(workoutOutline.get(zIndex).getDayView());
            container.removeView(workoutOutline.get(zIndex).getMyTable());
            workoutOutline.get(zIndex).getDayView().setId(titleIndex);
            workoutOutline.get(zIndex).getMyTable().setId(tableIndex);

            params.topMargin = 85;
            params.addRule(RelativeLayout.BELOW, titleIndex-1);
            container.addView(workoutOutline.get(zIndex).getDayView(), params);

            paramsT.topMargin = 85;
            paramsT.addRule(RelativeLayout.BELOW, titleIndex);
            container.addView(workoutOutline.get(zIndex).getMyTable(), paramsT);
            zIndex++;
        }

        //Last part of function sets the final day in the outline to the end based off of current global id integer
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsT = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        titleIndex = integer-2;
        tableIndex = integer-1;
        container.removeView(workoutOutline.get(zIndex).getDayView());
        container.removeView(workoutOutline.get(zIndex).getMyTable());
        workoutOutline.get(zIndex).getDayView().setId(titleIndex);
        workoutOutline.get(zIndex).getMyTable().setId(tableIndex);

        params.topMargin = 85;
        params.addRule(RelativeLayout.BELOW, titleIndex-1);
        container.addView(workoutOutline.get(zIndex).getDayView(), params);

        paramsT.topMargin = 85;
        paramsT.addRule(RelativeLayout.BELOW, titleIndex);
        container.addView(workoutOutline.get(zIndex).getMyTable(), paramsT);
    }


    /*
     *@Name: View Videos To Outline
     *
     *@Purpose: Takes all video lists from different view buttons and put into outline
     *
     *@Param
     *
     *@Brief: Function uses mod 5 * (number of muscles targeted) to distinguish
     *        the different days in global view videos list
     *
     *@ErrorsHandled: N/A
     */
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

    /*
     *@Name: Check Day in day list
     *
     *@Purpose: Checks that the given day is being shown
     *
     *@Param in: String day that will be checked (day)
     *
     *@Brief: Function finds given day in list of days and
     *        marks it as in outline to avoid duplicate days
     *
     *@ErrorsHandled: N/A
     */
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

    /*
     *@Name: Convert Muscles
     *
     *@Purpose:  Converts list of muscles names to
     *           format used to populate outline
     *
     *@Param in: ArrayList of actual names of muscles
     *           to be shown
     *       out: ArrayList of integers corresponding
     *       muscles to be shown
     *
     *@Brief: Converts muscle array from ArrayList of Names
     *        to ArrayList of integers corresponding to the
     *        muscle
     *
     *@ErrorsHandled: N/A
     */
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

    /*
     *@Name: Close Splash Screen
     *
     *@Purpose: Removes Splash Image to show background
     *
     *@Param N/A
     *
     *@Brief: Sets splash image visibility to gone and the other elements to visible.
     *        Also ends splash image spin animation. The result is that the outline
     *        elements are now shown.
     *
     *@ErrorsHandled: N/A
     */
    private void closeSplashScreen()
    {
        splashImage.clearAnimation();
        splashImage.setVisibility(View.GONE);
        topContent.setVisibility(View.VISIBLE);
        botButtons.setVisibility(View.VISIBLE);
    }
}