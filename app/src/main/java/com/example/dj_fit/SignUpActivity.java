// Program Information /////////////////////////////////////////////////////////
/*
 * @file SignUpActivity.java
 *
 * @brief Handles signing up for the app through Firebase
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// Sign Up Activity Class ////////////////////////////////////////////////////////////////

public class SignUpActivity extends AppCompatActivity {

    //Class variables
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private EditText passEdit, firstNameEdit, lastNameEdit, emailEdit;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views initialization
        passEdit = findViewById(R.id.passEdit);
        firstNameEdit = findViewById(R.id.firstNameEdit);
        lastNameEdit = findViewById(R.id.lastNameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        btnSignUp = findViewById(R.id.btnSignUp);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();

        //Button signs user up for a account using Firebase Authentications
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    //If any of the fields are empty, ask user to fill out
                    final String currentEmail = emailEdit.getText().toString();
                    if (currentEmail.isEmpty())
                    {
                        throw new Exception();
                    }

                    final String currentPass = passEdit.getText().toString();
                    if (currentPass.isEmpty())
                    {
                        throw new Exception();
                    }
                    final String currentFirstName = firstNameEdit.getText().toString();
                    if (currentFirstName.isEmpty())
                    {
                        throw new Exception();
                    }

                    final String currentLastName = lastNameEdit.getText().toString();
                    if (currentLastName.isEmpty())
                    {
                        throw new Exception();
                    }

                    //If all fields have text, create account with given info
                    createAccount(currentEmail, currentPass, currentFirstName, currentLastName);
                }
                catch (Exception e)
                {
                    Toast.makeText(SignUpActivity.this, "Please fill out each field.",
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Function definitions ////////////////////////////////////////////////////////

    /*
     *@Name: Create Account
     *
     *@Purpose: Create account for user
     *
     *@Param in: User's entered email (currentEmail)
     *       in: User's entered password (currentPass)
     *       in: User's entered first name (currentFirstName)
     *       in: User's entered last name (currentLastName)
     *
     *@Brief: Uses Firebase Authentication to create the account with
     *        user inputted information
     *
     *@ErrorsHandled: N/A
     */
    private void createAccount(final String currentEmail, final String currentPass,
                               final String currentFirstName,final String currentLastName)
    {
        //Button creates a account using user-inputted information
        mAuth.createUserWithEmailAndPassword(currentEmail, currentPass)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            addUserToDB(currentFirstName, currentLastName);
                            Toast.makeText(SignUpActivity.this, "Account successfully created.",
                                    Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            Intent signUpAct = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(signUpAct);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /*
     *@Name: Add User to Database
     *
     *@Purpose: Add's user's account to DB
     *
     *@Param in: User account's first name (firstName)
     *       in: User account's last name (lastName)
     *
     *@Brief: Sets a document in "Users" collection corresponding
     *        to his/her UID with their name and sets a document
     *        setting their permission as "Owner"
     *
     *@ErrorsHandled: N/A
     */
    private void addUserToDB ( String firstName, String lastName)
    {
        //Creates document for user in the database
        String userID = mAuth.getUid();
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        Map<String, Object> doctData = new HashMap<>();
        doctData.put("first_name", firstName);
        doctData.put("last_name", lastName);
        mDatabase.collection("users")
                .document(userID)
                .set(doctData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document Snapshot added");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        //Creates a document that gives user ownership of the files in the user's collection
        Map<String, Object> doctData2 = new HashMap<>();
        doctData2.put("Role", "Owner");
        doctData2.put("isTrainer", false);
        mDatabase.collection("users")
                .document(userID)
                .collection("editors")
                .document(userID)
                .set(doctData2).addOnSuccessListener(new OnSuccessListener<Void>() {
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
