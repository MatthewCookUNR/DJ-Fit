// Program Information /////////////////////////////////////////////////////////
/*
 * @file BackgroundActivity.java
 *
 * @brief Background Activity is used by the user to enter some information about
 *        themselves that will make it easier for the trainer to provide them
 *        with a personal fitness program.
 *
 * @author Matthew Cook
 * @author Collin Potters
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

// Background Activity Class ////////////////////////////////////////////////////////////////

public class BackgroundActivity extends BaseActivity {

    //Class variables
    private static final String TAG = "BackgroundActivity";
    private EditText currentFitEdit, goalEdit, medicalEdit,
                     availabilityEdit, additionalEdit;
    private String userID;
    private Button btnSubmit;
    private ScrollView backgroundScroll;
    private RelativeLayout backgroundText, backgroundBtn;
    private ImageView splashImage;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views and variables initialization
        currentFitEdit = findViewById(R.id.currentFitEdit);
        goalEdit = findViewById(R.id.goalEdit);
        medicalEdit = findViewById(R.id.medicalEdit);
        availabilityEdit = findViewById(R.id.availabilityEdit);
        additionalEdit = findViewById(R.id.additionalEdit);
        btnSubmit = findViewById(R.id.btnSubmit);
        backgroundScroll = findViewById(R.id.backgroundScroll);
        backgroundText = findViewById(R.id.backgroundText);
        backgroundBtn = findViewById(R.id.backgroundBtn);
        splashImage = findViewById(R.id.splashImage);

        userID = getIntent().getStringExtra("clientID");
        if(userID == null)
        {
            userID = FirebaseAuth.getInstance().getUid();
        }

        mDatabase = FirebaseFirestore.getInstance();
        checkIfBackgroundExists();


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addBackgroundToDB(userID);
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

    // Function definitions ////////////////////////////////////////////////////////

    /*
     *@Name: Check if Background Exists
     *
     *@Purpose: Checks if the user has already saved their background before
     *
     *@Param N/A
     *
     *@Brief: The function uses a snapshot listener to see if they have a
     *        background doc present in the database, which would mean that
     *        they have previously saved their background
     *
     *@ErrorsHandled: N/A
     */
    private void checkIfBackgroundExists()
    {
        final long start = System.currentTimeMillis();
        DocumentReference docRef = mDatabase.collection("users").document(userID).collection("fitnessData").document("backgroundDoc");
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
                    populateBackground(documentSnapshot.getData());
                }
                else
                {
                    Log.d (TAG, "Current data: null");
                    splashImage.setVisibility(View.GONE);
                    backgroundScroll.setVisibility(View.VISIBLE);
                    backgroundText.setVisibility(View.VISIBLE);
                    backgroundBtn.setVisibility(View.VISIBLE);                }
            }
        });
    }

    /*
     *@Name: Populate Background
     *
     *@Purpose: Populates background with user's previous background information
     *
     *@Param N/A
     *
     *@Brief: Uses the received Map of background information and uses the
     *        corresponding fields to populate the page
     *
     *@ErrorsHandled: N/A
     */
    private void populateBackground(Map<String, Object> docData)
    {
        currentFitEdit.setText(docData.get("currentFitProgram").toString());
        medicalEdit.setText(docData.get("medicalHist").toString());
        goalEdit.setText(docData.get("goals").toString());
        availabilityEdit.setText(docData.get("availability").toString());
        additionalEdit.setText(docData.get("otherInfo").toString());
        closeSplashScreen();
    }

    /*
     *@Name: Add Background to Database
     *
     *@Purpose: Saves the given background information to the database
     *
     *@Param in: String
     *
     *@Brief: Sets a document in the database that is a Map of user's background
     *
     *@ErrorsHandled: N/A
     */
    private void addBackgroundToDB(String userID)
    {
        Map doctData = createBackgroundMap();
        final long start = System.currentTimeMillis();

        mDatabase.collection("users").document(userID).collection("fitnessData")
                .document("backgroundDoc")
                .set(doctData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "Document Snapshot added w/ time : " + (end - start) );
                        Toast.makeText(getApplicationContext(), "Submit Successful!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getApplicationContext(), "Submit Failure!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /*
     *@Name: Create Background Map
     *
     *@Purpose: Saves the given background information to the database
     *
     *@Param out: Map containing background info (doctData)
     *
     *@Brief: Gets the different entered text in the form of strings
     *        and puts in into a Map.
     *
     *@ErrorsHandled: N/A
     */
    private Map createBackgroundMap()
    {
        String currentFit = currentFitEdit.getText().toString();
        String medicalHist = medicalEdit.getText().toString();
        String goals = goalEdit.getText().toString();
        String availability = availabilityEdit.getText().toString();
        String otherInfo = additionalEdit.getText().toString();

        Map<String, Object> doctData = new HashMap<>();
        doctData.put("currentFitProgram", currentFit);
        doctData.put("medicalHist", medicalHist);
        doctData.put("goals", goals);
        doctData.put("availability", availability);
        doctData.put("otherInfo", otherInfo);
        return doctData;
    }

    /*
     *@Name: Close Splash Screen
     *
     *@Purpose: Removes Splash Image to show background
     *
     *@Param N/A
     *
     *@Brief: Sets splash image visibility to gone and the other elements to visible.
     *        The result is that the background entry elements are now shown.
     *
     *@ErrorsHandled: N/A
     */
    private void closeSplashScreen()
    {
        splashImage.setVisibility(View.GONE);
        backgroundScroll.setVisibility(View.VISIBLE);
        backgroundText.setVisibility(View.VISIBLE);
        backgroundBtn.setVisibility(View.VISIBLE);
    }

}
