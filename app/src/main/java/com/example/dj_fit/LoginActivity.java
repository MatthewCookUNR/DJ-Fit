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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    //Variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseUser currentUser;
    private EditText emailText, passwordText;
    private Button btnSignIn, btnSignOut, btnCreateAccount, btnMainAct;


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
        btnSignOut = findViewById(R.id.btnSignOut);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnMainAct = findViewById(R.id.btnMainAct);

        //Initializing Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null)
        {
            //userSignedIn();
        }

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
                                        Log.d("Auth", "signInWithEmail:success");
                                        currentUser = mAuth.getCurrentUser();
                                        Toast.makeText(LoginActivity.this, "Successfully signed in",
                                                Toast.LENGTH_SHORT).show();
                                        userSignedIn();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("Auth", "signInWithEmail:failure", task.getException());
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


        //Button logs user out of the application
        btnSignOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();
                Toast.makeText(LoginActivity.this, "User has signed out", Toast.LENGTH_SHORT).show();
            }
        });

        //Button handles creating a new account using the inputted email and password
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    final String currentEmail = emailText.getText().toString();
                    if (currentEmail.isEmpty())
                    {
                        throw new Exception();
                    }

                    final String currentPass = passwordText.getText().toString();
                    if (currentPass.isEmpty())
                    {
                        throw new Exception();
                    }

                    mAuth.createUserWithEmailAndPassword(currentEmail, currentPass)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("SignUp", "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        addUserToDB(currentEmail, currentPass);
                                        Toast.makeText(LoginActivity.this, "Account successfully created.",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("SignUp", "createUserWithEmail:failure", task.getException());
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

        //Takes application to main activity for testing
        btnMainAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent databaseAct = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(databaseAct);
            }
        });



        //Placeholder for floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        onStart();
    }

    //Function takes user to main page after logging in
    private void userSignedIn()
    {
        Intent mainAct = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainAct);
    }

    //Function adds a new user's login information to the database
    private void addUserToDB ( String email, String password)
    {
        Map<String, Object> doctData = new HashMap<>();
        doctData.put("email", email);
        doctData.put("password", password);
        mDatabase.collection("users")
                .add(doctData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Database", "Document Snapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Database", "Error adding document", e);
                    }
                });

    }

    //Lets the user know if they are currently logged in
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            Toast.makeText(LoginActivity.this, "No one logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
