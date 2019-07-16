package com.example.dj_fit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BaseActivity {

    //Variables
    private static final String TAG = "MainActivity";
    private Button btnBackground, btnWorkoutOutline, btnRegisterTrainer, btnTrainerProfile, btnFindTrainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize layout variables
        btnBackground = findViewById(R.id.btnBackground);
        btnWorkoutOutline = findViewById(R.id.btnWorkoutOutline);
        btnRegisterTrainer = findViewById(R.id.btnRegisterTrainer);
        btnTrainerProfile = findViewById(R.id.btnTrainerProfile);
        btnFindTrainer = findViewById(R.id.btnFindTrainer);

        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String first_name = myPreferences.getString("first_name", "");
        String trainerID = myPreferences.getString("trainerID", "");

        //If user is a trainer, register trainer button is modify trainer instead
        if(!trainerID.equals(""))
        {
            btnRegisterTrainer.setText("Modify Trainer Information");
        }

        //If name doesn't appear in shared preferences, get from DB and put it into there
        if(first_name.equals(""))
        {
            final long start = System.currentTimeMillis();

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

            String userID = mAuth.getCurrentUser().getUid();
            DocumentReference docRef = mDatabase.collection("users").document(userID);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            long end = System.currentTimeMillis();
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            Log.d(TAG, "Logged at " + (end - start));

                            SharedPreferences.Editor myEditor = myPreferences.edit();
                            myEditor.putString("first_name", document.get("first_name").toString());
                            myEditor.putString("last_name", document.get("last_name").toString());
                            myEditor.apply();
                            end = System.currentTimeMillis();
                            Log.d(TAG, "Set name in Shared Pref logged at " + (end - start));
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        btnBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backgroundAct = new Intent(MainActivity.this, BackgroundActivity.class);
                startActivity(backgroundAct);
            }
        });

        btnWorkoutOutline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent workoutOutlineAct = new Intent(MainActivity.this, WorkoutOutlineActivity.class);
                startActivity(workoutOutlineAct);
            }
        });

        btnRegisterTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trainerRegisterIntent = new Intent(MainActivity.this, TrainerRegisterActivity.class);
                startActivity(trainerRegisterIntent);
            }
        });

        btnTrainerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trainerProfileIntent = new Intent(MainActivity.this, TrainerProfileActivity.class);
                trainerProfileIntent.putExtra("isOwner", true);
                startActivity(trainerProfileIntent);
            }
        });
        btnFindTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findTrainerIntent = new Intent(MainActivity.this, FindTrainerActivity.class);
                startActivity(findTrainerIntent);
            }
        });
    }
}
