package com.example.dj_fit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class FindTrainerActivity extends AppCompatActivity {

    private static final String TAG = "FindTrainerActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private StorageReference mStorageRef;
    Button btnFindTrainer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_trainer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnFindTrainer = findViewById(R.id.btnFindTrainer);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("trainerPics");

        btnFindTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findTrainerFromID();
                modifyTrainerFromID();
            }
        });
    }

    //Function used to test security rules on reading a specific user's information based on their trainer status
    //User should be able to do this
    private void findTrainerFromID()
    {
        final long start = System.currentTimeMillis();
        DocumentReference docRef = mDatabase.collection("users").document("HF9b38AapJZWc9wc9fSMOh6w9qj1");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Log.d(TAG, "Logged at " + (end - start));
                        end = System.currentTimeMillis();
                        Log.d(TAG, "Populate Logged at " + (end - start));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //Function used to test security rules on writing a specific user's information based on their trainer status
    //User should not be able to do this
    private void modifyTrainerFromID()
    {
        Map<String, Object> doctData2 = new HashMap<>();
        doctData2.put("Role", "Dumbhead");
        mDatabase.collection("users")
                .document("HF9b38AapJZWc9wc9fSMOh6w9qj1")
                .collection("editors")
                .document("HF9b38AapJZWc9wc9fSMOh6w9qj1")
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
