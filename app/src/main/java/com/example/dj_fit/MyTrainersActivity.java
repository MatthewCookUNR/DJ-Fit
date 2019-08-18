package com.example.dj_fit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
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

public class MyTrainersActivity extends AppCompatActivity {

    private static final String TAG = "MyTrainersActivity";
    private RelativeLayout trainersLayout;
    private TextView titleText;
    private ImageView splashImage;
    private int integer = 1;
    private List<DocumentSnapshot> documents;
    private FirebaseFirestore mDatabase;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trainers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        trainersLayout = findViewById(R.id.trainersLayout);
        titleText = findViewById(R.id.titleText);
        splashImage = findViewById(R.id.splashImage);

        userID = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseFirestore.getInstance();

        final RotateAnimation rotateAnimation = new RotateAnimation(0f, 720f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        splashImage.startAnimation(rotateAnimation);

        checkForCurrentTrainers();

        BottomNavigationView bottomNavigationItemView = findViewById(R.id.bottomNavigationItemView);
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch(menuItem.getItemId())
                {
                    case R.id.ic_back:
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                    case R.id.ic_home:
                        Intent homeIntent2 = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent2);
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
     *@Name: Check for Trainers
     *
     *@Purpose: Checks to see if the user has any trainers
     *
     *@Param N/A
     *
     *@Brief: Function queries DB to see if the user has any trainers
     *
     *@ErrorsHandled: N/A
     */
    private void checkForCurrentTrainers()
    {
        CollectionReference userRef = mDatabase.collection("users").document(userID)
                .collection("editors");
        Query query = userRef.limit(50);
        query.whereEqualTo("Role", "Trainer").whereEqualTo("isAccepted", true)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    documents = task.getResult().getDocuments();
                    Log.d(TAG, "Getting documents successful");
                    if(documents.size() != 0)
                    {
                        populateTrainers(documents);
                        closeSplashScreen();
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
     *@Name: Populate Trainers
     *
     *@Purpose: Populates page with list of client's trainers
     *
     *@Param N/A
     *
     *@Brief: Function loops through all of the received documents from the DB query
     *        and creates views to display them as trainers
     *
     *@ErrorsHandled: N/A
     */
    private void populateTrainers( List<DocumentSnapshot> documents)
    {
        for(int i = 0; i < documents.size(); i++)
        {
            Map<String, Object> docData = documents.get(i).getData();

            //Creates a TextView with client's first and last name
            TextView nameText = new TextView(MyTrainersActivity.this);
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
            trainersLayout.addView(nameText);
            integer++;

            //Creates a linear layout with two buttons, one to view trainer's profile page
            //and the other to remove the user as a the client's trainer
            LinearLayout butLayout = new LinearLayout(MyTrainersActivity.this);
            RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsR.addRule(RelativeLayout.BELOW, integer - 1);
            paramsR.leftMargin = 40;
            butLayout.setId(integer);
            butLayout.setLayoutParams(paramsR);
            integer++;

            LinearLayout.LayoutParams paramsB = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsB.weight = 1;

            Button acceptBut = createViewProfile(documents.get(i).getId(), (String) docData.get("first_name"), (String) docData.get("last_name") );
            acceptBut.setLayoutParams(paramsB);

            Button declineBut = createRemoveButton(documents.get(i).getId(), (String) docData.get("first_name"), (String) docData.get("last_name"));
            declineBut.setLayoutParams(paramsB);

            //Lastly, adds the programmatically created views to the screen
            butLayout.addView(acceptBut);
            butLayout.addView(declineBut);
            trainersLayout.addView(butLayout);
        }
        //closeSplashScreen();
    }

    /*
     *@Name: Create View Profile Button
     *
     *@Purpose: Create button that allows user to view client's profile
     *
     *@Param in: Trainer's ID (documentID)
     *       in: Trainer's first name (first_name)
     *       in: Trainer's last name (last_name)
     *       out: View profile button (viewBut)
     *
     *@Brief: Function creates a button with trainer's user information like ID and
     *        first/last name which will allow the trainer to distinguish them and
     *        find their information
     *
     *@ErrorsHandled: N/A
     */
    private Button createViewProfile(String documentID, String first_name, String last_name)
    {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        final Button viewBut = new Button(MyTrainersActivity.this);
        viewBut.setText("View Profile");
        viewBut.setTag(userInfo);
        viewBut.setTextSize(16);
        viewBut.setTransformationMethod(null);
        viewBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewTrainerProfile( (String) v.getTag());
            }
        });
        return viewBut;
    }

    /*
     *@Name: Create Decline Button
     *
     *@Purpose: Create button that removes user as client's trainer
     *
     *@Param in: Trainer's ID (documentID)
     *       in: Trainer's first name (first_name)
     *       in: Trainer's last name (last_name)
     *       out: Remove Trainer button (removeBut)
     *
     *@Brief: Function creates a button with trainer's user information like ID and
     *        first/last name which will allow the trainer to distinguish them and
     *        find their information
     *
     *@ErrorsHandled: N/A
     */    private Button createRemoveButton(String documentID, String first_name, String last_name)
    {
        final String userInfo = documentID + "/" + first_name + "/" + last_name;
        Button removeBut = new Button(MyTrainersActivity.this);
        removeBut.setText("Remove");
        removeBut.setTag(userInfo);
        removeBut.setTextSize(16);
        removeBut.setTransformationMethod(null);
        removeBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveAlert( (String) v.getTag());
            }
        });
        return removeBut;
    }


    /*
     *@Name: View Trainer Profile
     *
     *@Purpose: Views the trainer's profile page
     *
     *@Param in: Trainer's Tag (trainerTag)
     *
     *@Brief: Function takes user to the trainer's profile page
     *        through a intent
     *
     *@ErrorsHandled: N/A
     */
    private void viewTrainerProfile (String trainerTag)
    {
        String[] trainerData = trainerTag.split("/");
        Intent viewProfileIntent = new Intent(MyTrainersActivity.this, TrainerProfileActivity.class);
        viewProfileIntent.putExtra("first_name", trainerData[1]);
        viewProfileIntent.putExtra("last_name", trainerData[2]);
        viewProfileIntent.putExtra("isOwner", false);
        startActivity(viewProfileIntent);
    }

    /*
     *@Name: Remove Trainer
     *
     *@Purpose: Remove user as the client's existing trainer
     *
     *@Param in: Trainer's Tag (trainerTag)
     *
     *@Brief: Function deletes document that contains trainer's access
     *        to client's data and removes itself as the trainer's client
     *        from list of current clients
     *
     *@ErrorsHandled: N/A
     */
    private void removeTrainer(String trainerTag)
    {
        final long start = System.currentTimeMillis();
        String[] trainerData = trainerTag.split("/");

        //Deletes the document that is in the trainer's collection of clients
        mDatabase.collection("users").document(userID).collection("editors")
                .document(trainerData[0]).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                long end = System.currentTimeMillis();
                Log.d(TAG, "Document deleted w/ time : " + (end - start) );
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

        //Deletes the document that allows the trainer to view the user's fitness program
        mDatabase.collection("trainers").document(trainerData[0]).collection("clientsCurrent")
                .document(userID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                long end = System.currentTimeMillis();
                Log.d(TAG, "Document deleted w/ time : " + (end - start) );
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    /*
     *@Name: Show Remove Alert
     *
     *@Purpose: Show alert asking user if they want to remove trainer
     *
     *@Param in: Trainer's Tag (trainerTag)
     *
     *@Brief: Function shows alert that asks user yes or no if they want
     *        to remove trainer. If yes, it removes user as client's trainer
     *
     *@ErrorsHandled: N/A
     */
    private void showRemoveAlert(final String trainerTag)
    {
        final AlertDialog.Builder removeBuilder = new AlertDialog.Builder(MyTrainersActivity.this);
        final String[] trainerData = trainerTag.split("/");
        String trainerName = trainerData[1] + " " + trainerData[2];
        removeBuilder.setTitle("Are you sure you want to remove " + trainerName + "?");
        removeBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeTrainer(trainerTag);

                //Remake views without removed trainer
                removeFromDocuments(trainerData[0]);
                destroyViews();
                populateTrainers(documents);
            }
        });
        removeBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        removeBuilder.show();
    }

    /*
     *@Name: Remove From Documents
     *
     *@Purpose: Remove document with same id as given
     *
     *@Param in: Trainer's ID (trainerID)
     *
     *@Brief: Function loops through the list of all trainer docs and
     *        deletes the one given
     *
     *@ErrorsHandled: N/A
     */
    private void removeFromDocuments(String trainerID)
    {
        for( int i = 0; i < documents.size(); i++)
        {
            if(documents.get(i).getId().equals(trainerID))
            {
                documents.remove(i);
                break;
            }
        }
    }


    /*
     *@Name: Destroy Views
     *
     *@Purpose: Destroys the views for all the user's trainers
     *
     *@Param N/A
     *
     *@Brief: Function destroys all of the views populated to the
     *        trainers layout
     *
     *@ErrorsHandled: N/A
     */
    private void destroyViews()
    {
        while(integer > 0)
        {
            View currentView = findViewById(integer);
            trainersLayout.removeView(currentView);
            integer--;
        }
        integer++;
    }

    /*
     *@Name: Close Splash Screen
     *
     *@Purpose: Removes Splash Image to show background
     *
     *@Param N/A
     *
     *@Brief: Sets splash image visibility to gone and the other elements to visible.
     *        Also ends splash image spin animation. The result is that the layout
     *        elements are now shown.
     *
     *@ErrorsHandled: N/A
     */
    private void closeSplashScreen()
    {
        splashImage.clearAnimation();
        splashImage.setVisibility(View.GONE);
        titleText.setVisibility(View.VISIBLE);
        trainersLayout.setVisibility(View.VISIBLE);
    }

}
