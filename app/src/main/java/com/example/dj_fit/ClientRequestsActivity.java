package com.example.dj_fit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientRequestsActivity extends BaseActivity
{
    private static final String TAG = "ClientRequestsActivity";
    private int integer = 1;
    RelativeLayout clientReqLayout;
    TextView titleText;
    String userID;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_requests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        clientReqLayout = findViewById(R.id.clientReqLayout);
        titleText = findViewById(R.id.titleText);

        userID = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseFirestore.getInstance();

        checkForNewClients();

    }

    //Function queries DB to see if their are any clients requesting the user as a trainer
    private void checkForNewClients()
    {
        CollectionReference userRef = mDatabase.collection("trainers").document(userID).collection("clientRequests");
        Query query = userRef.limit(50);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    Log.d(TAG, "Getting documents successful");
                    if(documents.size() != 0)
                    {
                        populatePossibleClients(documents);
                    }
                }
                else
                {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    //Function populates the page with the list of clients requesting the trainer
    private void populatePossibleClients( List<DocumentSnapshot> documents)
    {
        for(int i = 0; i < documents.size(); i++)
        {
            Map<String, Object> docData = documents.get(i).getData();

            //Part of function creates a TextView with client's first and last name
            TextView nameText = new TextView(ClientRequestsActivity.this);
            nameText.setTextAppearance(this, android.R.style.TextAppearance_Large);
            nameText.setText(docData.get("first_name") + " " + docData.get("last_name"));
            nameText.setId(integer);
            RelativeLayout.LayoutParams paramsN = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //Integer is used to keep track of where the view will be placed in relative layout
            if(integer == 1)
            {
                paramsN.addRule(RelativeLayout.BELOW, titleText.getId());
            }
            else
            {
                paramsN.addRule(RelativeLayout.BELOW, integer-1);
            }
            paramsN.leftMargin = 40;
            paramsN.rightMargin = 40;
            paramsN.topMargin = 80;
            nameText.setLayoutParams(paramsN);
            clientReqLayout.addView(nameText);
            integer++;

            //Part of function creates a linear layout with two buttons, one to add user as one of
            //the trainer's clients, and the other to decline the user's client request
            LinearLayout butLayout = new LinearLayout(ClientRequestsActivity.this);
            RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsR.addRule(RelativeLayout.BELOW, integer - 1);
            paramsR.leftMargin = 40;
            butLayout.setId(integer);
            butLayout.setLayoutParams(paramsR);
            integer++;

            LinearLayout.LayoutParams paramsB = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsB.weight = 1;

            Button acceptBut = createAcceptButton(documents.get(i).getId(), (String) docData.get("first_name"), (String) docData.get("last_name") );
            acceptBut.setLayoutParams(paramsB);

            Button declineBut = createDeclineButton(documents.get(i).getId(), (String) docData.get("first_name"), (String) docData.get("last_name"));
            declineBut.setLayoutParams(paramsB);

            //Lastly, adds the programmatically created views to the screen
            butLayout.addView(acceptBut);
            butLayout.addView(declineBut);
            clientReqLayout.addView(butLayout);
        }
    }

    //Function creates a button that can handle accepting a user as a client and returns it
    private Button createAcceptButton(String documentID, String first_name, String last_name)
    {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        final Button acceptBut = new Button(ClientRequestsActivity.this);
        acceptBut.setText("Accept");
        acceptBut.setTextSize(16);
        acceptBut.setTag(userInfo);
        acceptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println((String) v.getTag());
                addUserAsClient( (String) v.getTag());
            }
        });
        return acceptBut;
    }

    //Function creates a button that can handle declining a user as a client and returns it
    private Button createDeclineButton(String documentID, String first_name, String last_name)
    {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        Button declineBut = new Button(ClientRequestsActivity.this);
        declineBut.setText("Decline");
        declineBut.setTextSize(16);
        declineBut.setTag(userInfo);
        declineBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeClientRequest( (String) v.getTag());
            }
        });
        return declineBut;
    }

    //Function adds a user as a client, deleting client request and adding client as current client
    private void addUserAsClient(String clientTag)
    {
        final long start = System.currentTimeMillis();
        String[] clientData = clientTag.split("/");
        HashMap<String, String> docData = new HashMap<>();
        docData.put("first_name", clientData[1]);
        docData.put("last_name", clientData[2]);

        //Sets document in DB to user inputted information
        mDatabase.collection("trainers").document(userID).collection("clientsCurrent")
                .document(clientData[0]).set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        //Sets document in DB to user inputted information
        mDatabase.collection("trainers").document(userID).collection("clientRequests")
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

    }

    //Function declines the client, removing request and removing read permission given
    //by the client
    private void removeClientRequest(String clientTag)
    {
        final long start = System.currentTimeMillis();
        String[] clientData = clientTag.split("/");

        //Deletes the document that is in the trainer's collection of clients
        mDatabase.collection("trainers").document(userID).collection("clientRequests")
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
                .document(userID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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
