package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.util.List;
import java.util.Map;

public class CurrentClientsActivity extends BaseActivity {
    private static final String TAG = "CurrentClientsActivity";
    private int integer = 1;
    RelativeLayout clientLayout;
    TextView titleText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_clients);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        clientLayout = findViewById(R.id.clientLayout);
        titleText = findViewById(R.id.titleText);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        checkForClients();
    }

    //Function queries DB to see if the user has any current clients for training
    private void checkForClients() {
        String userID = mAuth.getUid();
        CollectionReference userRef = mDatabase.collection("trainers").document(userID).collection("clientsCurrent");
        Query query = userRef.limit(50);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    Log.d(TAG, "Getting documents successful");
                    if (documents.size() != 0) {
                        populateClients(documents);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    //Function populates activity with the trainers current clients if there are any
    private void populateClients(List<DocumentSnapshot> documents)
    {
        for (int i = 0; i < documents.size(); i++)
        {
            Map<String, Object> docData = documents.get(i).getData();

            //Part of function creates a TextView with client's first and last name
            TextView nameText = new TextView(CurrentClientsActivity.this);
            nameText.setTextAppearance(this, android.R.style.TextAppearance_Large);
            nameText.setText(docData.get("first_name") + " " + docData.get("last_name"));
            nameText.setId(integer);
            RelativeLayout.LayoutParams paramsN = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //Integer is used to keep track of where the view will be placed in relative layout
            if (integer == 1) {
                paramsN.addRule(RelativeLayout.BELOW, titleText.getId());
            } else {
                paramsN.addRule(RelativeLayout.BELOW, integer - 1);
            }
            paramsN.leftMargin = 40;
            paramsN.rightMargin = 40;
            paramsN.topMargin = 80;
            nameText.setLayoutParams(paramsN);
            clientLayout.addView(nameText);
            integer++;

            //Part of function creates a linear layout with two buttons, one to view client's program
            //and another to remove the user as the trainer's client
            LinearLayout butLayout = new LinearLayout(CurrentClientsActivity.this);
            RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsR.addRule(RelativeLayout.BELOW, integer - 1);
            paramsR.leftMargin = 40;
            butLayout.setId(integer);
            butLayout.setLayoutParams(paramsR);
            integer++;

            LinearLayout.LayoutParams paramsB = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsB.weight = 1;

            Button acceptBut = createViewProgramButton(documents.get(i).getId(), (String) docData.get("first_name"), (String) docData.get("last_name"));
            acceptBut.setLayoutParams(paramsB);

            Button declineBut = createRemoveButton(documents.get(i).getId(), (String) docData.get("first_name"), (String) docData.get("last_name"));
            declineBut.setLayoutParams(paramsB);

            //Lastly, adds the programmatically created views to the screen
            butLayout.addView(acceptBut);
            butLayout.addView(declineBut);
            clientLayout.addView(butLayout);
        }
    }

    //Function creates a button that allows the user to manage their client's fitness program
    private Button createViewProgramButton(String documentID, String first_name, String last_name) {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        final Button acceptBut = new Button(CurrentClientsActivity.this);
        acceptBut.setText("View Program");
        acceptBut.setTextSize(16);
        acceptBut.setTag(userInfo);
        acceptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewClientProgram((String) v.getTag());
            }
        });
        return acceptBut;
    }

    //Function creates a button that can handle removing a user as a client
    private Button createRemoveButton(String documentID, String first_name, String last_name) {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        Button declineBut = new Button(CurrentClientsActivity.this);
        declineBut.setText("Remove");
        declineBut.setTextSize(16);
        declineBut.setTag(userInfo);
        declineBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUserAsClient((String) v.getTag());
            }
        });
        return declineBut;
    }

    //Function takes the user to a seperate activity to view their personal program
    private void viewClientProgram(String clientTag)
    {
        Intent clientProgramIntent = new Intent(CurrentClientsActivity.this, ClientProgramActivity.class);
        clientProgramIntent.putExtra("clientTag", clientTag);
        startActivity(clientProgramIntent);
    }

    //Function removes the user as one of the trainer's clients
    private void removeUserAsClient(String clientTag)
    {
        String userId = mAuth.getUid();
        final long start = System.currentTimeMillis();
        String[] clientData = clientTag.split("/");

        //Deletes the document that is in the trainer's collection of clients
        mDatabase.collection("trainers").document(userId).collection("clientsCurrent")
                .document(clientData[0]).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
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

        //Deletes the document that allows the trainer to view the user's fitness program
        mDatabase.collection("users").document(clientData[0]).collection("editors")
                .document(userId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
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

}
