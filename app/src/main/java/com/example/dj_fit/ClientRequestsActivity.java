// Program Information /////////////////////////////////////////////////////////
/*
 * @file ClientRequestsActivity.java
 *
 * @brief Activity is used to display user's that have requested the trainer
 *        and allow the trainer to accept or decline them
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
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

// Client Requests Activity Class ////////////////////////////////////////////////////////////////

public class ClientRequestsActivity extends BaseActivity
{

    //Class variables
    private static final String TAG = "ClientRequestsActivity";
    private int integer = 1;
    private RelativeLayout clientReqLayout;
    private TextView titleText;
    private ImageView splashImage;
    private String userID;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_requests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views and variables initialization
        clientReqLayout = findViewById(R.id.clientReqLayout);
        titleText = findViewById(R.id.titleText);
        splashImage = findViewById(R.id.splashImage);


        userID = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseFirestore.getInstance();

        final RotateAnimation rotateAnimation = new RotateAnimation(0f, 720f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        splashImage.startAnimation(rotateAnimation);

        checkForNewClients();

        BottomNavigationView bottomNavigationItemView = findViewById(R.id.bottomNavigationItemView);
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch(menuItem.getItemId())
                {
                    case R.id.ic_back:
                        Intent clientReqIntent = new Intent(getApplicationContext(), TrainerMenuActivity.class);
                        startActivity(clientReqIntent);
                        break;
                    case R.id.ic_home:
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                        break;
                    case R.id.ic_training:
                        Intent trainerIntent = new Intent(getApplicationContext(), TrainerMenuActivity.class);
                        startActivity(trainerIntent);
                        break;
                }
                return false;
            }
        });

    }

    // Function definitions ////////////////////////////////////////////////////////

    /*
     *@Name: Check for New Clients
     *
     *@Purpose: Checks to see if the trainer has any new client requests
     *
     *@Param N/A
     *
     *@Brief: Function queries DB to see if the user has any new client
     *        requests that have been made
     *
     *@ErrorsHandled: N/A
     */
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
                    else
                    {
                        closeSplashScreen();

                    }
                }
                else
                {
                    closeSplashScreen();
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /*
     *@Name: Populate Clients
     *
     *@Purpose: Populates page with list of clients requesting trainer
     *
     *@Param N/A
     *
     *@Brief: Function loops through all of the received documents from the DB query
     *        and creates views to display them as new client requests
     *
     *@ErrorsHandled: N/A
     */    private void populatePossibleClients( List<DocumentSnapshot> documents)
    {
        for(int i = 0; i < documents.size(); i++)
        {
            Map<String, Object> docData = documents.get(i).getData();

            //Creates a TextView with client's first and last name
            TextView nameText = new TextView(ClientRequestsActivity.this);
            nameText.setTextAppearance(this, android.R.style.TextAppearance_Large);
            nameText.setText(docData.get("first_name") + " " + docData.get("last_name"));
            nameText.setId(integer);
            RelativeLayout.LayoutParams paramsN = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //Integer is used to keep track of where the view will be placed in relative layout

            //Case where the view is the first one added
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

            //Creates a linear layout with two buttons, one to add user as one of
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
        closeSplashScreen();
    }


    /*
     *@Name: Create Accept Button
     *
     *@Purpose: Create button that accepts user as trainer's new client
     *
     *@Param in: Client's ID (documentID)
     *       in: Client's first name (first_name)
     *       in: Client's last name (last_name)
     *       out: Accept client button (acceptBut)
     *
     *@Brief: Function creates a button with client's user information like ID and
     *        first/last name which will allow the trainer to distinguish them and
     *        find their information
     *
     *@ErrorsHandled: N/A
     */
    private Button createAcceptButton(String documentID, String first_name, String last_name)
    {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        final Button acceptBut = new Button(ClientRequestsActivity.this);
        acceptBut.setText("Accept");
        acceptBut.setTag(userInfo);
        acceptBut.setTextSize(16);
        acceptBut.setTransformationMethod(null);
        acceptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println((String) v.getTag());
                addUserAsClient( (String) v.getTag());
            }
        });
        return acceptBut;
    }

    /*
     *@Name: Create Decline Button
     *
     *@Purpose: Create button that declines user as trainer's new client
     *
     *@Param in: Client's ID (documentID)
     *       in: Client's first name (first_name)
     *       in: Client's last name (last_name)
     *       out: Accept client button (acceptBut)
     *
     *@Brief: Function creates a button with client's user information like ID and
     *        first/last name which will allow the trainer to distinguish them and
     *        find their information
     *
     *@ErrorsHandled: N/A
     */    private Button createDeclineButton(String documentID, String first_name, String last_name)
    {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        Button declineBut = new Button(ClientRequestsActivity.this);
        declineBut.setText("Decline");
        declineBut.setTag(userInfo);
        declineBut.setTextSize(16);
        declineBut.setTransformationMethod(null);
        declineBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeClientRequest( (String) v.getTag());
            }
        });
        return declineBut;
    }

    /*
     *@Name: Add User as Client
     *
     *@Purpose: Adds user as one of the trainer's clients
     *
     *@Param in: Client's Tag (clientTag)
     *
     *@Brief: Function sets document for client in collection of current clients
     *        and then deletes client from list of client requests
     *
     *@ErrorsHandled: N/A
     */
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


    /*
     *@Name: Remove Client Request
     *
     *@Purpose: Remove client request from list of client requests
     *
     *@Param in: Client's Tag (clientTag)
     *
     *@Brief: Function deletes document that contains client request
     *        and removes the access to data that the client gave
     *        the trainer
     *
     *@ErrorsHandled: N/A
     */
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
        titleText.setVisibility(View.VISIBLE);
        clientReqLayout.setVisibility(View.VISIBLE);
    }
}
