package com.example.dj_fit;

import android.app.ActionBar;
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    //Variables
    private FirebaseAuth mAuth;
    private EditText emailText, passwordText;
    private Button btnSignIn, btnCreateAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing layout variables
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        //Initializing Firebase variables
        mAuth = FirebaseAuth.getInstance();

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

    private void getUserInformation()
    {
        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
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
                        myEditor.putString("trainerCode", document.get("trainerCode").toString());
                        myEditor.apply();
                        end = System.currentTimeMillis();
                        Log.d(TAG, "Set trainer code in Shared Pref logged at " + (end - start));
                    } else {
                        SharedPreferences.Editor myEditor = myPreferences.edit();
                        myEditor.putString("trainerCode", "false");
                        myEditor.apply();
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        userSignedIn();
    }
    //Function takes user to main page after logging in
    private void userSignedIn()
    {
        Intent mainAct = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainAct);
    }

    //Lets the user know if they are currently logged in
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
