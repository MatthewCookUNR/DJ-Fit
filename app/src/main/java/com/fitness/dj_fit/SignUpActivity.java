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

package com.fitness.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

// Sign Up Activity Class ////////////////////////////////////////////////////////////////

public class SignUpActivity extends AppCompatActivity {

    //Class variables
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private EditText passEdit, firstNameEdit, lastNameEdit, emailEdit;
    private Button btnNext;

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
        btnNext = findViewById(R.id.btnNext);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();

        //Button signs user up for a account using Firebase Authentications
        btnNext.setOnClickListener(new View.OnClickListener() {
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
                    Intent privacyIntent = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
                    privacyIntent.putExtra("firstName", currentFirstName);
                    privacyIntent.putExtra("lastName", currentLastName);
                    privacyIntent.putExtra("emailAddress", currentEmail);
                    privacyIntent.putExtra("password", currentPass);
                    startActivity(privacyIntent);
                }
                catch (Exception e)
                {
                    Toast.makeText(SignUpActivity.this, "Please fill out each field.",
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
