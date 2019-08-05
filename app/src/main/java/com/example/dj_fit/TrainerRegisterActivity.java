package com.example.dj_fit;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrainerRegisterActivity extends BaseActivity
{
    //Class variables
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String TAG = "TrainerRegisterActivity";
    private Uri imageToUpload;
    private TextView titleText;
    private ImageView mImage, splashImage;
    private RelativeLayout botButtons, registerInfo;
    private ScrollView trainerScroll;
    private EditText experienceEdit, employmentEdit, aboutYouEdit;
    private Button btnUploadImage, btnBecomeTrainer, btnUnregister;
    private FirebaseFirestore mDatabase;
    private StorageReference mStorageRef;
    private String imageName, uploadedImageName;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Views and parameter initialization
        titleText = findViewById(R.id.titleText);
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
        btnUnregister = findViewById(R.id.btnUnregister);
        uploadedImageName = null;
        imageToUpload = null;
        imageName = null;

        //Firebase parameters
        userID = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseFirestore.getInstance();
        mStorageRef= FirebaseStorage.getInstance().getReference("trainerPics");
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
                setTrainerStatusInDB(true);
                uploadToDB();
            }
        });

        btnUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showUnregisterAlert();
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
            //If image returned is not null, set image on screen
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

    //Function uploads the inputted information to the database
    private void uploadToDB()
    {
        boolean signedUp = true;
        final long start = System.currentTimeMillis();

        //Checks to see if a image is currently exists for profile
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

        //Get information entered manually
        String experience = experienceEdit.getText().toString();
        String employment = employmentEdit.getText().toString();
        String aboutYou = aboutYouEdit.getText().toString();

        //Get locally stored user information for use in trainer profile
        //i.e. user's name and (if already registered) trainer ID
        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String first_name = myPreferences.getString("first_name", "");
        String last_name = myPreferences.getString("last_name", "");
        String trainerCode = myPreferences.getString("trainerCode", "");

        //If not trainer ID exists, create a random ID of 8 length
        if(trainerCode.equals("false"))
        {
            signedUp = false;
            trainerCode = getAlphaNumericString();
            SharedPreferences.Editor myEditor = myPreferences.edit();
            myEditor.putString("trainerCode", trainerCode);
            myEditor.apply();
        }

        //Put all data about trainer into a map for upload to DB
        Map<String, Object> doctData = new HashMap<>();
        doctData.put("first_name", first_name);
        doctData.put("last_name", last_name);
        doctData.put("experience", experience);
        doctData.put("employment", employment);
        doctData.put("aboutYou", aboutYou);
        doctData.put("profilePic", imageLink);
        doctData.put("trainerCode", trainerCode);

        //Sets document in DB to user inputted information
        mDatabase.collection("trainers").document(userID)
                .set(doctData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                long end = System.currentTimeMillis();
                Log.d(TAG, "Document Snapshot added w/ time : " + (end - start) );
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getApplicationContext(), "Failure!", Toast.LENGTH_SHORT).show();
                    }
                });

        //If signing up and not updating, display message
        if(!signedUp)
        {
            Intent trainerProfileIntent = new Intent(TrainerRegisterActivity.this, TrainerProfileActivity.class);
            showSignedUpMessage(trainerProfileIntent);
        }
    }

    //Function populates the activity with values stored in the Firestore DB
    private void populateTrainerRegister(Map<String, Object> docData)
    {
        //If user is currently registered, populate the page with info
        if(docData.containsKey("experience"))
        {
            experienceEdit.setText(docData.get("experience").toString());
            employmentEdit.setText(docData.get("employment").toString());
            aboutYouEdit.setText(docData.get("aboutYou").toString());
            Object name = docData.get("profilePic");

            //If they have a profile picture, download it
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
                        adjustUI();
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

    //Function sets the user's status as a trainer to true/false in the DB
    private void setTrainerStatusInDB( boolean status)
    {
        Map<String, Object> doctData2 = new HashMap<>();
        doctData2.put("isTrainer", status);
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

    //Function unregisters the user as a trainer, deleting stored information on the database
    private void UnregisterTrainer()
    {
        mDatabase.collection("trainers").document(userID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");

                        //Set's status as trainer to false in DB
                        setTrainerStatusInDB(false);

                        //Delete user's profile picture if they have one
                        if(uploadedImageName != null)
                        {
                            deleteCurrentProfilePic();
                        }
                        final SharedPreferences myPreferences =
                                PreferenceManager.getDefaultSharedPreferences(TrainerRegisterActivity.this);
                        String trainerCode = myPreferences.getString("trainerCode", "");

                        //Remove trainer from list of trainer codes in DB
                        removeTrainerIdDB(trainerCode);

                        //Set trainer code as false in shared preferences
                        SharedPreferences.Editor myEditor = myPreferences.edit();
                        myEditor.putString("trainerCode", "false");
                        myEditor.apply();
                        Intent trainerRegisterIntent = new Intent(TrainerRegisterActivity.this, MainActivity.class);
                        startActivity(trainerRegisterIntent);

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    //Function handles alert shown when pressing deregister button
    private void showUnregisterAlert()
    {
        AlertDialog.Builder dayBuilder = new AlertDialog.Builder(TrainerRegisterActivity.this);
        dayBuilder.setTitle("Are you sure you want to unregister as a trainer?");

        dayBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                UnregisterTrainer();
            }
        });
        dayBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dayBuilder.show();
    }

    //Function shows the user their trainer code and allows them to copy it
    private void showSignedUpMessage(final Intent trainerProfileIntent)
    {
        final SharedPreferences myPreferences =
                PreferenceManager.getDefaultSharedPreferences(TrainerRegisterActivity.this);
        final String trainerCode = myPreferences.getString("trainerCode", "");
        AlertDialog.Builder codeAlert = new AlertDialog.Builder(this).setMessage(trainerCode);
        codeAlert.setTitle("Here is a code used by clients to connect with you.");
        codeAlert.setNeutralButton("Copy to Clipboard", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Trainer Code", trainerCode);
                clipboard.setPrimaryClip(clip);
                Toast mToast = Toast.makeText(TrainerRegisterActivity.this, "Code Copied", Toast.LENGTH_SHORT);
                mToast.show();
                startActivity(trainerProfileIntent);
            }
        });
        TextView textView = codeAlert.show().findViewById(android.R.id.message);
        textView.setTextSize(50);
    }

    // Function to generate a random string of length 8
    private String getAlphaNumericString()
    {

        // chose a Character random from this String
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder buildString = new StringBuilder(8);

        for (int i = 0; i < 8; i++)
        {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index = (int)(alphaNumericString.length() * Math.random());

            // add Character one by one in end of sb
            buildString.append(alphaNumericString.charAt(index));
        }

        //Checks to see if generated trainer code is already in use
        final String alphaString = buildString.toString();
        checkIfTrainerCodeExists(alphaString);


        return buildString.toString();
    }

    //Function checks if trainer code is already in use
    //Note: Runs function to generate a new one if that is the case
    private void checkIfTrainerCodeExists(final String alphaString)
    {
        //Part of function checks to make sure the generated string is not already used by another user
        final long start = System.currentTimeMillis();
        DocumentReference docRef = mDatabase.collection("trainers").document("0eh3S7vf62XX4DB2dsTG");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Log.d(TAG, "Logged at " + (end - start));
                        ArrayList<String> list = (ArrayList) document.getData().get("trainerCodes");
                        int index = list.indexOf(alphaString);

                        //Checks to see if generated string is already in use
                        if(index != -1)
                        {
                            getAlphaNumericString();
                        }
                        //Else, puts new on in list of trainer codes and uploads the new list
                        else
                        {
                            list.add(alphaString);
                            setTrainerCodesDB(list);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //Function sets the list of all trainer codes with a new, updated list
    private void setTrainerCodesDB(ArrayList<String> list)
    {
        final long start = System.currentTimeMillis();
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        map.put("trainerCodes", list);

        //Sets document in DB to user inputted information
        mDatabase.collection("trainers").document("0eh3S7vf62XX4DB2dsTG")
                .set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    //Function removes user's trainer code from list of all trainer codes in DB
    private void removeTrainerIdDB(final String trainerCode)
    {
        //Part of function checks to make sure the generated string is not already used by another user
        final long start = System.currentTimeMillis();
        DocumentReference docRef = mDatabase.collection("trainers").document("0eh3S7vf62XX4DB2dsTG");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long end = System.currentTimeMillis();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Log.d(TAG, "Logged at " + (end - start));
                        ArrayList<String> list = (ArrayList) document.getData().get("trainerCodes");
                        int index = list.indexOf(trainerCode);
                        if(index != -1)
                        {
                            list.remove(index);
                            setTrainerCodesDB(list);
                        }
                        else
                        {
                            Log.d(TAG, "Trainer code does not exist");
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //Adjusts UI if user is already registered as a trainer
    //Button appears that allows user to unregister
    private void adjustUI()
    {
        titleText.setText("Modify Trainer Information");
        btnUnregister.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams)  btnBecomeTrainer.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, R.id.btnUnregister);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) botButtons.getLayoutParams();
        params2.height = params2.height*2;
        btnBecomeTrainer.setLayoutParams(params);
        botButtons.setLayoutParams(params2);
        btnBecomeTrainer.setText("Save");
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
