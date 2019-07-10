package com.example.dj_fit;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class TrainerRegisterActivity extends BaseActivity
{
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String TAG = "TrainerRegisterActivity";
    Uri imageToUpload;
    ImageView mImage, splashImage;
    RelativeLayout botButtons, registerInfo;
    ScrollView trainerScroll;
    EditText experienceEdit, employmentEdit, aboutYouEdit;
    Button btnUploadImage, btnBecomeTrainer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private StorageReference mStorageRef;
    String imageName, uploadedImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        experienceEdit = findViewById(R.id.experienceEdit);
        employmentEdit = findViewById(R.id.employmentEdit);
        aboutYouEdit = findViewById(R.id.aboutYouEdit);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnBecomeTrainer = findViewById(R.id.btnBecomeTrainer);
        mImage = findViewById(R.id.profileImageView);
        splashImage = findViewById(R.id.splashImage);
        botButtons = findViewById(R.id.botButtons);
        registerInfo = findViewById(R.id.registerInfo);
        trainerScroll = findViewById(R.id.trainerScroll);
        uploadedImageName = null;
        imageToUpload = null;
        imageName = null;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        mStorageRef= FirebaseStorage.getInstance().getReference("trainerPics");
        System.out.println("On create stuff");
        checkIfTrainerRegisterExists();

        //Button causes the activity to open up Android Gallery to select a image for uploading
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        //Button registers the user as a trainer, uploading the given data on the page for use in their profile
        btnBecomeTrainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageToUpload != null)
                {
                    uploadImage();
                }
                else
                {
                    System.out.println("No image");
                }
                setTrainerStatusInDB();
                uploadToDB();
            }
        });
    }

    //Function handles the user selecting a desired image as their profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK)
        {
            imageToUpload = data.getData();
            if(imageToUpload != null)
            {
                try
                {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageToUpload);
                    RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    final float scale = this.getResources().getDisplayMetrics().density;
                    roundDrawable.setCircular(true);
                    mImage.getLayoutParams().height = ((int) (120 * scale + 0.5f));
                    mImage.getLayoutParams().width = ((int) (120 * scale + 0.5f));
                    mImage.requestLayout();
                    mImage.setImageDrawable(roundDrawable);
                }
                catch (Exception e)
                {
                    System.out.println("Bitmap exception");
                }
            }
        }
    }

    //Function gets a files extension and returns it
    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Function uploads a image to Firebase Storage
    private void uploadImage()
    {
        if(imageToUpload != null)
        {
            imageName = System.currentTimeMillis() + "." +
                    getFileExtension(imageToUpload);
            StorageReference fileRef = mStorageRef.child(imageName);

            fileRef.putFile(imageToUpload).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(TrainerRegisterActivity.this, "File upload success", Toast.LENGTH_SHORT).show();
                    System.out.println("Before: " + uploadedImageName);
                    if(uploadedImageName != null)
                    {
                        deleteCurrentProfilePic();
                    }
                    uploadedImageName = "trainerPics/" + imageName;
                    System.out.println("After: " + uploadedImageName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(TrainerRegisterActivity.this, "File upload failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
        }
        else
        {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToDB()
    {
        String imageLink;
        if(imageName != null)
        {
            imageLink = "trainerPics/" + imageName;
        }
        else if(uploadedImageName != null)
        {
            imageLink = uploadedImageName;
        }
        else
        {
            imageLink = null;
        }
        String experience = experienceEdit.getText().toString();
        String employment = employmentEdit.getText().toString();
        String aboutYou = aboutYouEdit.getText().toString();
        String userID = mAuth.getCurrentUser().getUid();

        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String first_name = myPreferences.getString("first_name", "");
        String last_name = myPreferences.getString("last_name", "");

        final long start = System.currentTimeMillis();
        Map<String, Object> doctData = new HashMap<>();
        doctData.put("first_name", first_name);
        doctData.put("last_name", last_name);
        doctData.put("experience", experience);
        doctData.put("employment", employment);
        doctData.put("aboutYou", aboutYou);
        doctData.put("profilePic", imageLink);

        mDatabase.collection("trainers").document(userID)
                .set(doctData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
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

    //Function populates the activity with values stored in the Firestore DB
    private void populateTrainerRegister(Map<String, Object> docData)
    {
        if(docData.containsKey("experience"))
        {
            experienceEdit.setText(docData.get("experience").toString());
            employmentEdit.setText(docData.get("employment").toString());
            aboutYouEdit.setText(docData.get("aboutYou").toString());
            Object name = docData.get("profilePic");

            if(name != null)
            {
                uploadedImageName = name.toString();
                System.out.println("Image is not null");
                downloadFile();
            }
            else
            {
                System.out.println("Image is null");
                closeSplashScreen();
            }
        }
        else
        {
            closeSplashScreen();
        }
    }

    //Function checks to see if user has registered as a trainer
    //If so it cause the activity to try and populate it with existing values
    private void checkIfTrainerRegisterExists()
    {
        final long start = System.currentTimeMillis();
        String userID = mAuth.getCurrentUser().getUid();

        DocumentReference docRef = mDatabase.collection("trainers").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Log.d(TAG, "Logged at " + (end - start));
                        populateTrainerRegister(document.getData());
                        end = System.currentTimeMillis();
                        Log.d(TAG, "Populate Logged at " + (end - start));
                    } else {
                        closeSplashScreen();
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //Function deletes the current profile picture from the database if it is changes
    private void deleteCurrentProfilePic()
    {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(uploadedImageName);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(TrainerRegisterActivity.this, "Delete success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TrainerRegisterActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Function downloads the profile pic image from the Firestore DB
    private void downloadFile()
    {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(uploadedImageName);

        final long TEN_MEGABYTE = 10 * 1024 * 1024;
        imageRef.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                roundDrawable.setCircular(true);
                final float scale = TrainerRegisterActivity.this.getResources().getDisplayMetrics().density;
                mImage.getLayoutParams().height = ((int) (120 * scale + 0.5f));
                mImage.getLayoutParams().width = ((int) (120 * scale + 0.5f));
                mImage.requestLayout();
                mImage.setImageDrawable(roundDrawable);
                closeSplashScreen();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Toast.makeText(TrainerRegisterActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Function sets the user's status as a trainer to true upon registration
    private void setTrainerStatusInDB()
    {
        String userID = mAuth.getCurrentUser().getUid();
        Map<String, Object> doctData2 = new HashMap<>();
        doctData2.put("isTrainer", true);
        mDatabase.collection("users")
                .document(userID)
                .collection("editors")
                .document(userID)
                .update(doctData2).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    //Function closes the splash image covering the screen
    private void closeSplashScreen()
    {
       splashImage.setVisibility(View.INVISIBLE);
       botButtons.setVisibility(View.VISIBLE);
       registerInfo.setVisibility(View.VISIBLE);
       trainerScroll.setVisibility(View.VISIBLE);
    }
}
