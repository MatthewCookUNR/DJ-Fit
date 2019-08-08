// Program Information /////////////////////////////////////////////////////////
/*
 * @file LoginActivity.java
 *
 * @brief Login Activity is used to not only log user in but retrieve necessary
 *        information to run the app such as first/last name and trainer code
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// Login Activity Class ////////////////////////////////////////////////////////////////

public class LoginActivity extends AppCompatActivity {

    //Class variables
    private static final String TAG = "LoginActivity";
    String userID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private EditText emailText, passwordText;
    private Button btnSignIn, btnCreateAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views and variables initialization
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        //Initializing Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        //Button signs the user into the application if they have a existing account
        btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try
                {
                    String currentEmail = emailText.getText().toString();
                    if(currentEmail.isEmpty())
                    {
                        throw new Exception();
                    }

                    String currentPass = passwordText.getText().toString();
                    if(currentPass.isEmpty())
                    {
                        throw new Exception();
                    }

                    mAuth.signInWithEmailAndPassword(currentEmail, currentPass)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        Toast.makeText(LoginActivity.this, "Successfully signed in",
                                                Toast.LENGTH_SHORT).show();
                                        userID = mAuth.getUid();
                                        getUserInformation();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    // ...
                                }
                            });
                }
                catch (Exception e)
                {
                    Toast.makeText(LoginActivity.this, "Please enter a email and password.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Button takes user to create account screen
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signUpAct = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpAct);
            }
        });

    }

    // Function definitions ////////////////////////////////////////////////////////

    /*
     *@Name: Get User Information
     *
     *@Purpose: Retrieve information from database required to run the app properly
     *
     *@Param N/A
     *
     *@Brief: Function gets the user's first and last name as well as their trainer
     *        code if they are currently a trainer
     *
     *@ErrorsHandled: N/A
     */
    private void getUserInformation()
    {
        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        final long start = System.currentTimeMillis();

        //Gets document containing user's first and last name
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

        checkIfUserIsTrainer();
    }

    /*
     *@Name: Check if User is a Trainer
     *
     *@Purpose: Checks to see if user logging in is already a trainer and
     *          handles accordingly
     *
     *@Param N/A
     *
     *@Brief: Function checks to see if user has a trainer document and, if so,
     *        stores their trainer code in a shared preference
     *
     *@ErrorsHandled: N/A
     */
    private void checkIfUserIsTrainer()
    {
        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        final long start = System.currentTimeMillis();

        //Gets document containing user's trainer code if it exists
        DocumentReference docRef2 = mDatabase.collection("trainers").document(userID);
        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Log.d(TAG, "Logged at " + (end - start));
                        SharedPreferences.Editor myEditor = myPreferences.edit();
                        myEditor.putString("trainerCode", (String) document.get("trainerCode"));
                        myEditor.apply();
                        end = System.currentTimeMillis();
                        Log.d(TAG, "Set trainer code in Shared Pref logged at " + (end - start));
                    } else {
                        SharedPreferences.Editor myEditor = myPreferences.edit();
                        myEditor.putString("trainerCode", "false");
                        myEditor.apply();
                        Log.d(TAG, "No such document");
                    }
                    userSignedIn();
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /*
     *@Name: User Signed In
     *
     *@Purpose: Take user to main activity on sign in
     *
     *@Param N/A
     *
     *@Brief: Creates intent for main activity and takes user
     *        there
     *
     *@ErrorsHandled: N/A
     */
    private void userSignedIn()
    {
        Intent mainAct = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainAct);
    }

    //On start, program checks if user is logged in, if so, takes user to Main
    //Activity
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            userSignedIn();
        }
    }
}
