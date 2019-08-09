// Program Information /////////////////////////////////////////////////////////
/*
 * @file CurrentClientsActivity.java
 *
 * @brief Activity is used to display a trainer's current clients and allow them
 *        to view their client's workout information or remove them as a client
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.List;
import java.util.Map;

// Current Clients Activity Class ////////////////////////////////////////////////////////////////

public class CurrentClientsActivity extends BaseActivity {

    //Class variables
    private static final String TAG = "CurrentClientsActivity";
    private int integer = 1;
    private RelativeLayout clientLayout;
    private TextView titleText;
    private FirebaseAuth mAuth;
    private ImageView splashImage;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_clients);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views and variables initialization
        clientLayout = findViewById(R.id.clientLayout);
        titleText = findViewById(R.id.titleText);
        splashImage = findViewById(R.id.splashImage);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        final RotateAnimation rotateAnimation = new RotateAnimation(0f, 720f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        splashImage.startAnimation(rotateAnimation);

        checkForClients();

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
     *@Name: Check for Clients
     *
     *@Purpose: Checks to see if the trainer has any clients
     *
     *@Param N/A
     *
     *@Brief: Function queries DB to see if the user has any current clients
     *        for training that have been accepted
     *
     *@ErrorsHandled: N/A
     */
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
                    else
                    {
                        closeSplashScreen();
                    }
                } else {
                    closeSplashScreen();
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /*
     *@Name: Populate Clients
     *
     *@Purpose: Populates page with list of clients
     *
     *@Param N/A
     *
     *@Brief: Function loops through all of the received documents from the DB query
     *        and creates views to display them as clients
     *
     *@ErrorsHandled: N/A
     */
    private void populateClients(List<DocumentSnapshot> documents)
    {
        for (int i = 0; i < documents.size(); i++)
        {
            Map<String, Object> docData = documents.get(i).getData();

            //Creates a TextView with client's first and last name
            TextView nameText = new TextView(CurrentClientsActivity.this);
            nameText.setTextAppearance(this, android.R.style.TextAppearance_Large);
            nameText.setText(docData.get("first_name") + " " + docData.get("last_name"));
            nameText.setId(integer);
            RelativeLayout.LayoutParams paramsN = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //Integer is used to keep track of where the view will be placed in relative layout

            //Case where the view is the first one added
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

            //Creates a linear layout with two buttons, one to view client's program
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
        closeSplashScreen();
    }

    /*
     *@Name: Create View Program Button
     *
     *@Purpose: Create button that shows client's program
     *
     *@Param in: Client's ID (documentID)
     *       in: Client's first name (first_name)
     *       in: Client's last name (last_name)
     *       out: View client's program button (viewBut)
     *
     *@Brief: Function creates a button with client's user information like ID and
     *        first/last name which will allow the trainer to distinguish them and
     *        find their information
     *
     *@ErrorsHandled: N/A
     */
    private Button createViewProgramButton(String documentID, String first_name, String last_name) {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        final Button viewBut = new Button(CurrentClientsActivity.this);
        viewBut.setText("View Program");
        viewBut.setTextSize(16);
        viewBut.setTag(userInfo);
        viewBut.setTransformationMethod(null);
        viewBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewClientProgram((String) v.getTag());
            }
        });
        return viewBut;
    }

    /*
     *@Name: Create Remove Button
     *
     *@Purpose: Create button that removes client
     *
     *@Param in: Client's ID (documentID)
     *       in: Client's first name (first_name)
     *       in: Client's last name (last_name)
     *       out: Remove client button (declineBut)
     *
     *@Brief: Function creates a button with client's user information like ID and
     *        first/last name which will allow the trainer to distinguish them and
     *        remove them as their client
     *
     *@ErrorsHandled: N/A
     */
    private Button createRemoveButton(String documentID, String first_name, String last_name) {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        Button removeBut = new Button(CurrentClientsActivity.this);
        removeBut.setText("Remove");
        removeBut.setTextSize(16);
        removeBut.setTag(userInfo);
        removeBut.setTransformationMethod(null);
        removeBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUserAsClient((String) v.getTag());
            }
        });
        return removeBut;
    }

    /*
     *@Name: View Client Program
     *
     *@Purpose: Takes trainer to their client's fitness program
     *
     *@Param in: Client's Tag (clientTag)
     *
     *@Brief: Function takes the user to a seperate activity to view
     *        their personal program
     *
     *@ErrorsHandled: N/A
     */
    private void viewClientProgram(String clientTag)
    {
        Intent clientProgramIntent = new Intent(CurrentClientsActivity.this, ClientProgramActivity.class);
        clientProgramIntent.putExtra("clientTag", clientTag);
        startActivity(clientProgramIntent);
    }

    /*
     *@Name: Remove User as Client
     *
     *@Purpose: Removes client as trainer's accepted client
     *
     *@Param in: Client's Tag (clientTag)
     *
     *@Brief: Function deletes user from their list of clients
     *        and removes their own permission to view/modify
     *       the clients fitness program
     *
     *@ErrorsHandled: N/A
     */
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
        clientLayout.setVisibility(View.VISIBLE);
    }

}
