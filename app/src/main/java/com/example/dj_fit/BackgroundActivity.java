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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class BackgroundActivity extends BaseActivity {

    private static final String TAG = "BackgroundActivity";

    private EditText currentFitEdit, goalEdit, medicalEdit,
                     availabilityEdit, additionalEdit;

    private Button btnSubmit;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentFitEdit = findViewById(R.id.currentFitEdit);
        goalEdit = findViewById(R.id.goalEdit);
        medicalEdit = findViewById(R.id.medicalEdit);
        availabilityEdit = findViewById(R.id.availabilityEdit);
        additionalEdit = findViewById(R.id.additionalEdit);
        btnSubmit = findViewById(R.id.btnSubmit);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        checkIfBackgroundExists();


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userID = mAuth.getCurrentUser().getUid();
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

    private void checkIfBackgroundExists()
    {
        final long start = System.currentTimeMillis();

        String userID = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = mDatabase.collection("users").document(userID).collection("background").document("backgroundDoc");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if( task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if( document.exists())
                    {
                        Map<String, Object> doctData = document.getData();
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "DocumentSnapshot data at time: " + (end - start));
                        populateBackground(doctData);
                    }
                    else
                    {
                        Log.d(TAG, "No such document");
                    }
                }
                else
                {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void populateBackground(Map<String, Object> docData)
    {
        currentFitEdit.setText(docData.get("currentFitProgram").toString());
        medicalEdit.setText(docData.get("medicalHist").toString());
        goalEdit.setText(docData.get("goals").toString());
        availabilityEdit.setText(docData.get("availability").toString());
        additionalEdit.setText(docData.get("otherInfo").toString());
    }

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

        mDatabase.collection("users").document(userID).collection("background")
                .document("backgroundDoc")
                .set(doctData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document Snapshot added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }


}
