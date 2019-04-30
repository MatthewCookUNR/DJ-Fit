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

public class SignUpActivity extends AppCompatActivity {

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

        passEdit = findViewById(R.id.passEdit);
        firstNameEdit = findViewById(R.id.firstNameEdit);
        lastNameEdit = findViewById(R.id.lastNameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        btnSignUp = findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
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

                    //Button creates a account using user-inputted information
                    mAuth.createUserWithEmailAndPassword(currentEmail, currentPass)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        String id = mAuth.getCurrentUser().getUid();
                                        addUserToDB(id, currentFirstName, currentLastName);
                                        Toast.makeText(SignUpActivity.this, "Account successfully created.",
                                                Toast.LENGTH_SHORT).show();

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
                catch (Exception e)
                {
                    Toast.makeText(SignUpActivity.this, "Please fill out each field.",
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Function adds a new user's login information to the database
    private void addUserToDB ( String userID, String firstName, String lastName)
    {
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
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

}
