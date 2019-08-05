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

    //Function checks if the user has already saved their background before
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

    //Function populates background with user's previous background information
    private void populateBackground(Map<String, Object> docData)
    {
        currentFitEdit.setText(docData.get("currentFitProgram").toString());
        medicalEdit.setText(docData.get("medicalHist").toString());
        goalEdit.setText(docData.get("goals").toString());
        availabilityEdit.setText(docData.get("availability").toString());
        additionalEdit.setText(docData.get("otherInfo").toString());
        closeSplashScreen();
    }

    //Function saves the given background information to the database
    private void addBackgroundToDB(String userID)
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

    //Function closes the splash screen
    private void closeSplashScreen()
    {
        splashImage.setVisibility(View.GONE);
        backgroundScroll.setVisibility(View.VISIBLE);
        backgroundText.setVisibility(View.VISIBLE);
        backgroundBtn.setVisibility(View.VISIBLE);
    }

}
