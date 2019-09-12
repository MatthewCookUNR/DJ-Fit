// Program Information /////////////////////////////////////////////////////////
/*
 * @file PrivacyPolicyActivity.java
 *
 * @brief Shows the user the privacy policy and allows them to finish signing up
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.fitness.dj_fit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

// Privacy Policy Activity Class ////////////////////////////////////////////////////////////////

public class PrivacyPolicyActivity extends AppCompatActivity {

    //Class variables
    private static final String TAG = "PrivacyPolicyActivity";
    private FirebaseAuth mAuth;
    private Button btnSignUp;
    private TextView privacyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        privacyText = findViewById(R.id.privacyText);
        btnSignUp = findViewById(R.id.btnSignUp);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();

        String text = "By tapping Sign Up, you agree to our Privacy Policy. You may also receive SMS notifications from us.";

        SpannableString spanString = new SpannableString(text);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget)
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://dj-fit.flycricket.io/privacy.html")));
            }
        };
        spanString.setSpan(clickableSpan1, 37, 51, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        privacyText.setText(spanString);
        privacyText.setMovementMethod(LinkMovementMethod.getInstance());

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName, lastName, emailAddress, password;
                firstName = getIntent().getStringExtra("firstName");
                lastName = getIntent().getStringExtra("lastName");
                emailAddress = getIntent().getStringExtra("emailAddress");
                password = getIntent().getStringExtra("password");
                createAccount(emailAddress, password, firstName, lastName);
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
                .addOnCompleteListener(PrivacyPolicyActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            addUserToDB(currentFirstName, currentLastName);
                            Toast.makeText(PrivacyPolicyActivity.this, "Account successfully created.",
                                    Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            Intent signUpAct = new Intent(PrivacyPolicyActivity.this, LoginActivity.class);
                            startActivity(signUpAct);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(PrivacyPolicyActivity.this, "Authentication failed.",
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
        final long start = System.currentTimeMillis();

        //Creates document for user in the database
        String userID = mAuth.getUid();
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();

        Map<String, Object> doctData = new HashMap<>();
        doctData.put("first_name", firstName);
        doctData.put("last_name", lastName);

        Map<String, Object> doctData2 = new HashMap<>();
        doctData2.put("Role", "Owner");
        doctData2.put("isTrainer", false);

        WriteBatch batch = mDatabase.batch();

        DocumentReference userDocRef = mDatabase.collection("users")
                .document(userID);

        DocumentReference userEditorsRef = mDatabase.collection("users")
                .document(userID)
                .collection("editors")
                .document(userID);

        //First part adds user as the trainer's new client
        batch.set(userDocRef, doctData);

        //Second part deletes the user's current request
        batch.set(userEditorsRef, doctData2);

        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                long end = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Batch success w/ time : " + (end - start) );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                long end = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Batch failure w/ time : " + (end - start) );
            }
        });
    }

}
