package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindTrainerActivity extends BaseActivity {

    //Class variables
    private static final String TAG = "FindTrainerActivity";
    private FirebaseFirestore mDatabase;
    private Button btnFindTrainer;
    private EditText trainerCodeEdit;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_trainer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views and variables initialization
        btnFindTrainer = findViewById(R.id.btnFindTrainer);
        trainerCodeEdit = findViewById(R.id.trainerCodeEdit);
        mDatabase = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        btnFindTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                findTrainerFromID(trainerCodeEdit.getText().toString());
            }
        });
    }

    //Function used to test security rules on reading a specific user's information based on their trainer status
    //User should be able to do this
    private void findTrainerFromID(String trainerCode)
    {
        CollectionReference userRef = mDatabase.collection("trainers");
        Query query = userRef.whereEqualTo("trainerCode", trainerCode);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    Log.d(TAG, "Getting documents successful");
                    viewTrainerProfilePage(documents.get(0).getData(), documents.get(0).getId());
                }
                else
                {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    //Opens up the trainer profile page for the given trainer code that was found in DB
    private void viewTrainerProfilePage(Map<String, Object> docData, String trainerID)
    {
        Intent trainerProfileIntent = new Intent(FindTrainerActivity.this, TrainerProfileActivity.class);
        //If trainer code corresponds to your own profile, open accordingly
        if(trainerID.equals(userId))
        {
            trainerProfileIntent.putExtra("isOwner", true);
        }
        else
        {
            trainerProfileIntent.putExtra("isOwner", false);
            trainerProfileIntent.putExtra("trainerID", trainerID);
            trainerProfileIntent.putExtra("first_name", docData.get("first_name").toString());
            trainerProfileIntent.putExtra("last_name", docData.get("last_name").toString());
        }
        startActivity(trainerProfileIntent);
    }

    //Function used to test security rules on writing a specific user's information based on their trainer status
    //User should not be able to do this
    private void modifyTrainerFromID()
    {
        Map<String, Object> doctData2 = new HashMap<>();
        doctData2.put("Role", "User");
        mDatabase.collection("users")
                .document("userId")
                .collection("editors")
                .document("userId")
                .update(doctData2).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document2 Snapshot added");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document 2", e);
            }
        });
    }
}
