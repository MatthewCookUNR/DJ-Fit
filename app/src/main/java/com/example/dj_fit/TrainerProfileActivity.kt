// Program Information /////////////////////////////////////////////////////////
/*
 * @file TrainerProfileActivity.java
 *
 * @brief Displays trainer profile for both the trainer or their potential clients
 *
 * @author Collin Potter
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit

import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.util.HashMap

// Trainer Profile Activity Class ////////////////////////////////////////////////////////////////

class TrainerProfileActivity : BaseActivity() {
    private var profileImageView: ImageView? = null
    private var splashImage: ImageView? = null
    private var profileNameText: TextView? = null
    private var employerText: TextView? = null
    private var experienceText: TextView? = null
    private var aboutMeText: TextView? = null
    private var btnRequestTrainer: Button? = null
    private var btnGetTrainerCode: Button? = null
    private var imageName: String? = null
    private var trainerID: String? = null
    private var topGradLayout: RelativeLayout? = null
    private var trainerScroll: ScrollView? = null
    private var userID: String? = null
    private var mDatabase: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_profile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Views and variables initialization
        val splashLocal : ImageView? = findViewById(R.id.splashImage)
        splashImage = findViewById(R.id.splashImage)
        profileImageView = findViewById(R.id.profileImageView)
        profileNameText = findViewById(R.id.profileNameText)
        employerText = findViewById(R.id.employerText)
        experienceText = findViewById(R.id.experienceText)
        aboutMeText = findViewById(R.id.aboutMeText)
        topGradLayout = findViewById(R.id.topGradLayout)
        trainerScroll = findViewById(R.id.trainerScroll)
        btnRequestTrainer = findViewById(R.id.btnRequestTrainer)
        btnGetTrainerCode = findViewById(R.id.btnGetTrainerCode)
        val isOwner = intent.getBooleanExtra("isOwner", true)
        imageName = null
        trainerID = null

        val rotateAnimation = RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.setDuration(5000)
        rotateAnimation.setInterpolator(LinearInterpolator())


        //Firebase parameters
        userID = FirebaseAuth.getInstance().uid
        mDatabase = FirebaseFirestore.getInstance()

        splashLocal?.startAnimation(rotateAnimation)


        //If viewer is owner of profile, display self profile
        if (isOwner) {
            checkIfTrainerProfileExists(splashLocal)
            btnGetTrainerCode!!.visibility = View.VISIBLE
        } else {
            adjustUI()
            val first_name = intent.getStringExtra("first_name")
            val last_name = intent.getStringExtra("last_name")
            findTrainerInfo(first_name, last_name)
        }//If viewer is a client, adjust UI and allow them to request trainer

        btnGetTrainerCode!!.setOnClickListener { showTrainerCode() }

        btnRequestTrainer!!.setOnClickListener {
            sendTrainerRequest(trainerID)
            btnRequestTrainer!!.text = "Request Sent"
            btnRequestTrainer!!.isClickable = false
        }

        val bottomNavigationItemView : BottomNavigationView = findViewById(R.id.bottomNavigationItemView)
        bottomNavigationItemView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.ic_back -> {
                    if(isOwner)
                    {
                        val myProfileIntent = Intent(applicationContext, TrainerMenuActivity::class.java)
                        startActivity(myProfileIntent)
                    }
                    else
                    {
                        val clientProfileIntent = Intent(applicationContext, FindTrainerActivity::class.java)
                        startActivity(clientProfileIntent)
                    }
                }
                R.id.ic_home -> {
                    val homeIntent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(homeIntent)
                }
                R.id.ic_training -> {
                    //Checks to see if the user is currently a trainer
                    val myPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val trainerCode = myPreferences.getString("trainerCode", "")
                    if (trainerCode != "false") {
                        val trainerIntent = Intent(applicationContext, TrainerMenuActivity::class.java)
                        startActivity(trainerIntent)
                    } else {
                        val becomeTrainerIntent = Intent(applicationContext, BecomeTrainerActivity::class.java)
                        startActivity(becomeTrainerIntent)
                    }
                }
            }
            false
        })
    }

    // Function definitions ////////////////////////////////////////////////////////

    /*
     *@Name: Populate Profile Page
     *
     *@Purpose: Populates the profile page with existing information
     *
     *@Param in: Map of document containing profile information (docData)
     *
     *@Brief: Different fields on screen are filled with previous information
     *        obtained from Firestore and the profile picture is downloaded if
     *        one exists
     *
     *@ErrorsHandled: N/A
     */
    private fun populateProfilePage(docData: Map<String, Any>) {
        if (docData.containsKey("experience")) {
            imageName = docData["profilePic"] as String
            val fullName = docData["first_name"].toString() + " " + docData["last_name"].toString()
            profileNameText!!.text = fullName
            employerText!!.text = docData["employment"].toString()
            experienceText!!.text = docData["experience"].toString()
            aboutMeText!!.text = docData["aboutYou"].toString()
            closeSplashScreen()
            if (imageName == null) {
                println("Image is null")
            } else {
                println(imageName)
                // Call function with kotlin's coroutines to remove possibility of stopping main thread during load
                GlobalScope.launch {
                    //Thread to download profile pic if it exists
                    runOnUiThread(Runnable
                    {
                        downloadFile()
                    })
                }
            }
        } else {
            closeSplashScreen()
        }
    }

    /*
     *@Name: Check if Trainer Profile Exists
     *
     *@Purpose: Get file with user's profile is it exists in the DB
     *
     *@Param N/A
     *
     *@Brief: The function uses a snapshot listener that checks to see if
     *        the user is a trainer, if so, attempts to populate the data
     *
     *@ErrorsHandled: N/A
     */
    private fun checkIfTrainerProfileExists(splashLocal: ImageView?) {
        val start = System.currentTimeMillis()
        val docRef = mDatabase!!.collection("trainers").document(userID!!)
        docRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed", e)
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val end = System.currentTimeMillis()
                Log.d(TAG, "Current data: " + documentSnapshot.data!!)
                Log.d(TAG, "Logged at " + (end - start))
                populateProfilePage(documentSnapshot.data!!)
                splashLocal?.clearAnimation()
            } else {
                splashLocal?.clearAnimation()
                closeSplashScreen()
                Log.d(TAG, "Current data: null")
            }
        }
    }

    /*
     *@Name: Download File
     *
     *@Purpose: Download profile picture for Firebase Cloud Storage
     *
     *@Param N/A
     *
     *@Brief: Downloads file corresponding to the imageName retrieved
     *        from Firestore, which contains the user's profile picture
     *        Picture is altered to show in a small circle on-screen
     *
     *@ErrorsHandled: N/A
     */
    private fun downloadFile() {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child(imageName!!)

        val TEN_MEGABYTE = (10 * 1024 * 1024).toLong()
        imageRef.getBytes(TEN_MEGABYTE).addOnSuccessListener { bytes ->
            // Data for "images/island.jpg" is returns, use this as needed
            //Toast.makeText(TrainerProfileActivity.this, "Download success", Toast.LENGTH_SHORT).show();
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val roundDrawable = RoundedBitmapDrawableFactory.create(resources, bmp)
            roundDrawable.isCircular = true
            val scale = this@TrainerProfileActivity.resources.displayMetrics.density
            profileImageView!!.layoutParams.height = (120 * scale + 0.5f).toInt()
            profileImageView!!.layoutParams.width = (120 * scale + 0.5f).toInt()
            profileImageView!!.requestLayout()
            profileImageView!!.setImageDrawable(roundDrawable)
        }.addOnFailureListener {
            //Toast.makeText(TrainerProfileActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     *@Name: Find Trainer Info
     *
     *@Purpose: Gets a trainer's profile data based on given first
     *          and last name
     *
     *@Param in: Trainer's first name (first_name)
     *       in: Trainer's last name (last_name)
     *
     *@Brief: Gets database document corresponding to the trainer's
     *        first and last name and then populates the page with
     *        it
     *
     *@ErrorsHandled: N/A
     */
    private fun findTrainerInfo(first_name: String, last_name: String) {
        val userRef = mDatabase!!.collection("trainers")
        val query = userRef.whereEqualTo("first_name", first_name).whereEqualTo("last_name", last_name)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result!!.documents
                Log.d(TAG, "Getting documents successful")
                populateProfilePage(documents[0].data!!)
                trainerID = documents[0].id
            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
                closeSplashScreen()
            }
        }
    }

    /*
     *@Name: Send Trainer Request
     *
     *@Purpose: Send request to trainer to become their client
     *
     *@Param in: Trainer's unique ID (trainerID)
     *
     *@Brief: Sets document in clientRequests sub-collection under the given trainer
     *        with client's first and last name
     *
     *@ErrorsHandled: N/A
     */
    private fun sendTrainerRequest(trainerID: String?) {
        val start = System.currentTimeMillis()

        val myPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val first_name = myPreferences.getString("first_name", "")
        val last_name = myPreferences.getString("last_name", "")

        //Creates a request for training in the DB
        val docData = HashMap<String, Any>()
        docData["first_name"] = first_name
        docData["last_name"] = last_name

        //Sets document in DB to user inputted information
        mDatabase!!.collection("trainers").document(trainerID!!).collection("clientRequests")
                .document(userID!!).set(docData).addOnSuccessListener {
                    val end = System.currentTimeMillis()
                    Log.d(TAG, "Document Snapshot added w/ time : " + (end - start))
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }

        giveTrainerAccessInDB()
    }

    /*
     *@Name: Give Trainer Access in Database
     *
     *@Purpose: Gives trainer access to view client's fitness data
     *
     *@Param N/A
     *
     *@Brief: Sets document in clientRequests sub-collection under the given trainer
     *        with client's first and last name
     *
     *@ErrorsHandled: N/A
     */
    private fun giveTrainerAccessInDB() {
        val start = System.currentTimeMillis()

        //Sets permissions to allow trainer to view user's content
        val editData = HashMap<String, String>()
        editData["Role"] = "Trainer"
        editData["first_name"] = intent.getStringExtra("first_name")
        editData["last_name"] = intent.getStringExtra("last_name")

        //Sets document in DB to user inputted information
        mDatabase!!.collection("users").document(userID!!).collection("editors")
                .document(intent.getStringExtra("trainerID")).set(editData).addOnSuccessListener {
                    val end = System.currentTimeMillis()
                    Log.d(TAG, "Document Snapshot added w/ time : " + (end - start))
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    /*
     *@Name: Show Trainer Code
     *
     *@Purpose: Display's trainer's unique code
     *
     *@Param N/A
     *
     *@Brief: Opens up a alert with the trainer's code (stored in SharedPreferences)
     *        and allows them to copy to clipboard
     *
     *@ErrorsHandled: N/A
     */
    private fun showTrainerCode() {
        val myPreferences = PreferenceManager.getDefaultSharedPreferences(this@TrainerProfileActivity)
        val trainerCode = myPreferences.getString("trainerCode", "")
        val codeAlert = AlertDialog.Builder(this).setMessage(trainerCode)
        codeAlert.setNeutralButton("Copy to Clipboard") { dialog, id ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Trainer Code", trainerCode)
            clipboard.primaryClip = clip
            val mToast = Toast.makeText(this@TrainerProfileActivity, "Code Copied", Toast.LENGTH_SHORT)
            mToast.show()
        }
        val textView = codeAlert.show().findViewById<TextView>(android.R.id.message)
        textView.textSize = 50f
    }

    /*
     *@Name: Adjust UI
     *
     *@Purpose: Change UI to display client options
     *
     *@Param N/A
     *
     *@Brief: Makes the button to request trainer visible to user
     *
     *@ErrorsHandled: N/A
     */
    private fun adjustUI() {
        btnRequestTrainer!!.visibility = View.VISIBLE
    }

    /*
     *@Name: Close Splash Screen
     *
     *@Purpose: Remove Splash Screen and make profile UI visible
     *
     *@Param N/A
     *
     *@Brief: Makes the Splash Image view invisible and the other views visible
     *
     *@ErrorsHandled: N/A
     */
    private fun closeSplashScreen() {
        splashImage!!.clearAnimation()
        splashImage!!.visibility = View.INVISIBLE
        topGradLayout!!.visibility = View.VISIBLE
        trainerScroll!!.visibility = View.VISIBLE
    }

    companion object {

        //Class variables
        private val TAG = "TrainerProfileActivity"
    }

}
