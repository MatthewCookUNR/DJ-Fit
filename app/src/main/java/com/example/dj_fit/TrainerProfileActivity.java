package com.example.dj_fit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import javax.annotation.Nullable;

public class TrainerProfileActivity extends BaseActivity {

    String TAG = "Trainer Profile Activity";
    ImageView profileImageView, splashImage;
    TextView profileNameText, employerText, experienceText, aboutMeText;
    String imageName;
    RelativeLayout topGradLayout;
    ScrollView trainerScroll;
    private FirebaseAuth mAuth;
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
        imageName = null;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        checkIfTrainerProfileExists();

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
    private void checkIfTrainerProfileExists()
    {
        final long start = System.currentTimeMillis();
        String userID = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = mDatabase.collection("users").document(userID);
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


    private void closeSplashScreen()
    {
        splashImage.setVisibility(View.INVISIBLE);
        topGradLayout.setVisibility(View.VISIBLE);
        trainerScroll.setVisibility(View.VISIBLE);
    }

}
