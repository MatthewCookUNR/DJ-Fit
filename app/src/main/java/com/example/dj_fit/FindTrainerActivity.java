// Program Information /////////////////////////////////////////////////////////
/*
 * @file FindTrainerActivity.java
 *
 * @brief Find Trainer Activity is used by users to locate the profile page for
 *        trainers so that they can request them.
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

// Find Trainer Activity Class ////////////////////////////////////////////////////////////////

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

        BottomNavigationView bottomNavigationItemView = findViewById(R.id.bottomNavigationItemView);
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch(menuItem.getItemId())
                {
                    case R.id.ic_back:
                        Intent clientReqIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(clientReqIntent);
                        break;
                    case R.id.ic_home:
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
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
     *@Name: Find Trainer From ID
     *
     *@Purpose: Use user-entered trainer code to locate trainer
     *
     *@Param in: Trainer's unique code (trainerCode)
     *
     *@Brief: Function uses given code to look for trainer's document in the database.
     *        If found, takes user to the trainers profile page
     *
     *@ErrorsHandled: N/A
     */
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

    /*
     *@Name: View Trainer Profile Page
     *
     *@Purpose: Opens up the trainer profile page for the given trainer code
     *
     *@Param in: Map containing information to find trainer (docData)
     *           Trainer's unique code (trainerCode)
     *
     *@Brief: Function checks to see if trainer is the user itself or a client.
     *        If user is a client, user's first/last name are put in intent
     *        along with the trainer's code
     *
     *@ErrorsHandled: N/A
     */
    private void viewTrainerProfilePage(Map<String, Object> docData, String trainerID)
    {
        Intent trainerProfileIntent = new Intent(FindTrainerActivity.this, TrainerProfileActivity.class);

        //If trainer code corresponds to your own profile, open accordingly
        if(trainerID.equals(userId))
        {
            trainerProfileIntent.putExtra("isOwner", true);
        }
        //Else, put information needed to find trainer and request trainer them in Intent
        else
        {
            trainerProfileIntent.putExtra("isOwner", false);
            trainerProfileIntent.putExtra("trainerID", trainerID);
            trainerProfileIntent.putExtra("first_name", docData.get("first_name").toString());
            trainerProfileIntent.putExtra("last_name", docData.get("last_name").toString());
        }
        startActivity(trainerProfileIntent);
    }
}
