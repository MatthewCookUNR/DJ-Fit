package com.example.dj_fit;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class TrainerProfileActivity extends BaseActivity {

    String TAG = "Trainer Profile Activity";
    ImageView profileImageView, splashImage;
    TextView profileNameText, employerText, experienceText, aboutMeText;
    Button btnRequestTrainer, btnGetTrainerCode;
    String imageName;
    private String trainerID;
    RelativeLayout topGradLayout;
    ScrollView trainerScroll;
    String userID;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        splashImage = findViewById(R.id.splashImage);
        profileImageView = findViewById(R.id.profileImageView);
        profileNameText = findViewById(R.id.profileNameText);
        employerText = findViewById(R.id.employerText);
        experienceText = findViewById(R.id.experienceText);
        aboutMeText = findViewById(R.id.aboutMeText);
        topGradLayout = findViewById(R.id.topGradLayout);
        trainerScroll = findViewById(R.id.trainerScroll);
        btnRequestTrainer = findViewById(R.id.btnRequestTrainer);
        btnGetTrainerCode = findViewById(R.id.btnGetTrainerCode);
        imageName = null;
        trainerID = null;

        userID = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseFirestore.getInstance();
        boolean isOwner = getIntent().getBooleanExtra("isOwner", false);

        //If viewer is owner of profile, display self profile
        if(isOwner == true)
        {
            btnGetTrainerCode.setVisibility(View.VISIBLE);
            checkIfTrainerProfileExists(userID);
        }
        //If viewer is a client, adjust UI and allow them to request trainer
        else
        {
            adjustUI();
            String first_name = getIntent().getStringExtra("first_name");
            String last_name = getIntent().getStringExtra("last_name");
            findTrainerInfo(first_name, last_name);
        }

        btnGetTrainerCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTrainerCode();
            }
        });

        btnRequestTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTrainerRequest(trainerID);
            }
        });
    }

    //Functions populates the page with given information the user registered with
    private void populateProfilePage(Map<String, Object> docData)
    {
        if(docData.containsKey("experience"))
        {
            imageName = (String) docData.get("profilePic");
            String fullName = docData.get("first_name").toString() + " " + docData.get("last_name").toString();
            profileNameText.setText(fullName);
            employerText.setText(docData.get("employment").toString());
            experienceText.setText(docData.get("experience").toString());
            aboutMeText.setText(docData.get("aboutYou").toString());
            if(imageName == null)
            {
                closeSplashScreen();
                System.out.println("Image is null");
            }
            else
            {
                System.out.println(imageName);
                downloadFile();
            }
        }
        else
        {
            closeSplashScreen();
        }
    }

    //Function checks if the trainer has registered as a trainer and determines if the
    //page will be populated
    private void checkIfTrainerProfileExists(String userID)
    {
        final long start = System.currentTimeMillis();
        DocumentReference docRef = mDatabase.collection("trainers").document(userID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null)
                {
                    Log.w(TAG, "Listen failed", e);
                }

                if (documentSnapshot != null && documentSnapshot.exists())
                {
                    long end = System.currentTimeMillis();
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    Log.d(TAG, "Logged at " + (end - start));
                    populateProfilePage(documentSnapshot.getData());
                }
                else
                {
                    Log.d (TAG, "Current data: null");
                }
            }
        });
    }

    //Button downloads the user's profile image and populates in on the profile page
    private void downloadFile()
    {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imageName);

        final long TEN_MEGABYTE = 10 * 1024 * 1024;
        imageRef.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                //Toast.makeText(TrainerProfileActivity.this, "Download success", Toast.LENGTH_SHORT).show();
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                roundDrawable.setCircular(true);
                final float scale = TrainerProfileActivity.this.getResources().getDisplayMetrics().density;
                profileImageView.getLayoutParams().height = ((int) (120 * scale + 0.5f));
                profileImageView.getLayoutParams().width = ((int) (120 * scale + 0.5f));
                profileImageView.requestLayout();
                profileImageView.setImageDrawable(roundDrawable);
                closeSplashScreen();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Toast.makeText(TrainerProfileActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Function queries for profile of desired trainer
    private void findTrainerInfo(String first_name, String last_name)
    {
        CollectionReference userRef = mDatabase.collection("trainers");
        Query query = userRef.whereEqualTo("first_name", first_name).whereEqualTo("last_name", last_name);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    Log.d(TAG, "Getting documents successful");
                    populateProfilePage(documents.get(0).getData());
                    trainerID = documents.get(0).getId();
                }
                else
                {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    closeSplashScreen();
                }
            }
        });
    }

    //Functions sends a request to the trainer with his/her first and last name
    private void sendTrainerRequest(String trainerID)
    {
        final long start = System.currentTimeMillis();

        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String first_name = myPreferences.getString("first_name", "");
        String last_name = myPreferences.getString("last_name", "");

        //Part of function creates a request for training in the DB
        Map<String, Object> docData = new HashMap<>();
        docData.put("first_name", first_name);
        docData.put("last_name", last_name);

        //Sets document in DB to user inputted information
        mDatabase.collection("trainers").document(trainerID).collection("clientRequests")
                .document(userID).set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
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


        //Part of functions sets permissions to allow trainer to view user's content
        Map<String, String> editData = new HashMap<>();
        editData.put("Role", "Trainer");
        editData.put("first_name", getIntent().getStringExtra("first_name") );
        editData.put("last_name", getIntent().getStringExtra("last_name") );

        //Sets document in DB to user inputted information
        mDatabase.collection("users").document(userID).collection("editors")
                .document(getIntent().getStringExtra("trainerID")).set(editData).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void showTrainerCode()
    {
        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(TrainerProfileActivity.this);
        final String trainerCode = myPreferences.getString("trainerCode", "");
        AlertDialog.Builder codeAlert = new AlertDialog.Builder(this).setMessage(trainerCode);
        codeAlert.setNeutralButton("Copy to Clipboard", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Trainer Code", trainerCode);
                clipboard.setPrimaryClip(clip);
                Toast mToast = Toast.makeText(TrainerProfileActivity.this, "Code Copied", Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
        TextView textView = codeAlert.show().findViewById(android.R.id.message);
        textView.setTextSize(50);
    }

    private void adjustUI()
    {
        btnRequestTrainer.setVisibility(View.VISIBLE);
    }

    private void closeSplashScreen()
    {
        splashImage.setVisibility(View.INVISIBLE);
        topGradLayout.setVisibility(View.VISIBLE);
        trainerScroll.setVisibility(View.VISIBLE);
    }

}
