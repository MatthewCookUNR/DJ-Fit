// Program Information /////////////////////////////////////////////////////////
/*
 * @file TrainerRegisterActivity.java
 *
 * @brief Allow trainers to register or modify their trainer information/status
 *
 * @author Collin Potter
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.fitness.dj_fit

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.BottomNavigationView
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import java.util.ArrayList
import java.util.HashMap

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Trainer Register Activity Class ////////////////////////////////////////////////////////////////

class TrainerRegisterActivity : BaseActivity() {
    private var imageToUpload: Uri? = null
    private var titleText: TextView? = null
    private var mImage: ImageView? = null
    private var splashImage: ImageView? = null
    private var botButtons: RelativeLayout? = null
    private var registerInfo: RelativeLayout? = null
    private var trainerScroll: ScrollView? = null
    private var experienceEdit: EditText? = null
    private var employmentEdit: EditText? = null
    private var aboutYouEdit: EditText? = null
    private var btnUploadImage: Button? = null
    private var btnBecomeTrainer: Button? = null
    private var btnUnregister: Button? = null
    private var btnRemovePic: Button? = null
    private var mDatabase: FirebaseFirestore? = null
    private var mStorageRef: StorageReference? = null
    private var imageName: String? = null
    private var uploadedImageName: String? = ""
    private var userID: String? = null
    private var signUp: Boolean? = true
    private lateinit var functions: FirebaseFunctions



    // Function to generate a random string of length 8
    private// chose a Character random from this String
    // create StringBuffer size of AlphaNumericString
    // generate a random number between
    // 0 to AlphaNumericString variable length
    // add Character one by one in end of sb
    //Checks to see if generated trainer code is already in use
    val alphaNumericString: String
        get() {
            val alphaNumericString = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "0123456789"
                    + "abcdefghijklmnopqrstuvxyz")
            val buildString = StringBuilder(8)

            for (i in 0..7) {
                val index = (alphaNumericString.length * Math.random()).toInt()
                buildString.append(alphaNumericString[index])
            }
            val alphaString = buildString.toString()
            checkIfTrainerCodeExists(alphaString)


            return buildString.toString()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_register)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Views and parameter initialization
        val splashLocal : ImageView? = findViewById(R.id.splashImage)
        titleText = findViewById(R.id.titleText)
        experienceEdit = findViewById(R.id.experienceEdit)
        employmentEdit = findViewById(R.id.employmentEdit)
        aboutYouEdit = findViewById(R.id.aboutYouEdit)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnBecomeTrainer = findViewById(R.id.btnBecomeTrainer)
        mImage = findViewById(R.id.profileImageView)
        splashImage = findViewById(R.id.splashImage)
        botButtons = findViewById(R.id.botButtons)
        registerInfo = findViewById(R.id.registerInfo)
        trainerScroll = findViewById(R.id.trainerScroll)
        btnUnregister = findViewById(R.id.btnUnregister)
        btnRemovePic = findViewById(R.id.btnRemovePic)

        val rotateAnimation = RotateAnimation(0f, 720f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.setDuration(5000)
        rotateAnimation.setInterpolator(LinearInterpolator())

        //Firebase parameters
        functions = FirebaseFunctions.getInstance()
        userID = FirebaseAuth.getInstance().uid
        mDatabase = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().getReference("trainerPics")

        splashLocal?.startAnimation(rotateAnimation)
        checkIfTrainerRegisterExists(splashLocal)

        //Button causes the activity to open up Android Gallery to select a image for uploading
        btnUploadImage!!.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
        }

        //Button removes old profile image
        btnRemovePic!!.setOnClickListener {
            showRemovePicAlert()
        }

        //Button registers the user as a trainer, uploading the given data on the page for use in their profile
        btnBecomeTrainer!!.setOnClickListener {
            println(imageToUpload)
            if (imageToUpload != null)
            {
                uploadImage()
            }
            else if (imageToUpload == null && uploadedImageName != "")
            {
                println("No image to upload")
                if(mImage!!.visibility == View.GONE)
                {
                    deleteCurrentProfilePic()
                    uploadedImageName = ""
                }
            }
            uploadToDB()
        }

        btnUnregister!!.setOnClickListener { showUnregisterAlert() }

        val bottomNavigationItemView : BottomNavigationView = findViewById(R.id.bottomNavigationItemView)
        bottomNavigationItemView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
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
     *@Name: On Activity Result
     *
     *@Purpose: Handles receiving image from the gallery intent
     *
     *@Param in: Integer code telling what request was for (requestCode)
     *       in: Integer code telling status of result (resultCode)
     *       in: Data received from the intent (data)
     *
     *@Brief: Function checks what kind of request was received and if the result
     *        returned OK. If a image is returned, the function puts the image on
     *        screen.
     *
     *@ErrorsHandled: N/A
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            imageToUpload = data!!.data
            //If image returned is not null, set image on screen
            if (imageToUpload != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageToUpload)
                    val roundDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                    val scale = this.resources.displayMetrics.density
                    roundDrawable.isCircular = true
                    mImage!!.layoutParams.height = (120 * scale + 0.5f).toInt()
                    mImage!!.layoutParams.width = (120 * scale + 0.5f).toInt()
                    mImage!!.requestLayout()
                    mImage!!.setImageDrawable(roundDrawable)
                    mImage!!.visibility = View.VISIBLE
                } catch (e: Exception) {
                    println("Bitmap exception")
                }

            }
        }
    }

    /*
     *@Name: Get File Extension
     *
     *@Purpose: Gets the file extension from the given URI
     *
     *@Param in: URI that will be translated to file extension (Uri)
     *       out: String representing file extension of given Uri
     *
     *@Brief: N/A
     *
     *@ErrorsHandled: N/A
     */
    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    /*
     *@Name: Upload Image
     *
     *@Purpose: Upload Image to Firebase Storage
     *
     *@Param N/A
     *
     *@Brief: Function puts the image file that is received from the
     *        gallery onto Firebase Cloud Storage for later retrieval
     *
     *@ErrorsHandled: N/A
     */
    private fun uploadImage() {
        if (imageToUpload != null) {
            val tempImage = imageToUpload
            imageName = System.currentTimeMillis().toString() + "." +
                    getFileExtension(tempImage!!)
            val fileRef = mStorageRef!!.child(imageName!!)

            fileRef.putFile(imageToUpload!!).addOnSuccessListener {
                Toast.makeText(this@TrainerRegisterActivity, "File upload success", Toast.LENGTH_SHORT).show()
                println("Before: " + uploadedImageName!!)
                if (uploadedImageName != "") {
                    deleteCurrentProfilePic()
                    uploadedImageName = ""
                }
                uploadedImageName = "trainerPics/" + imageName!!
                println("After: " + uploadedImageName!!)
                if( signUp == true)
                {
                    val trainerIntent = Intent(applicationContext, TrainerProfileActivity::class.java)
                    trainerIntent.putExtra("signUp", true)
                    startActivity(trainerIntent)
                }
            }.addOnFailureListener { Toast.makeText(this@TrainerRegisterActivity, "File upload failed", Toast.LENGTH_SHORT).show() }.addOnProgressListener { }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    /*
     *@Name: Upload to Database
     *
     *@Purpose: Uploads trainer's inputted information to the DB
     *
     *@Param N/A
     *
     *@Brief: Function puts all information shown on their profile into
     *        a Map and then puts it onto the Firestore DB
     *
     *@ErrorsHandled: N/A
     */
    private fun uploadToDB() {
        val start = System.currentTimeMillis()

        //Checks to see if a image is currently exists for profile
        val imageLink: String?
        if (imageToUpload != null)
        {
            println("Case 1")
            imageLink = "trainerPics/" + imageName!!
        } else if (uploadedImageName != "")
        {
            println("Case 2")
            imageLink = uploadedImageName
        } else
        {
            println("Case 3")
            imageLink = ""
        }

        //Get information entered manually
        val experience = experienceEdit!!.text.toString()
        val employment = employmentEdit!!.text.toString()
        val aboutYou = aboutYouEdit!!.text.toString()

        //Get locally stored user information for use in trainer profile
        //i.e. user's name and (if already registered) trainer ID
        val myPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val first_name = myPreferences.getString("first_name", "")
        val last_name = myPreferences.getString("last_name", "")
        var trainerCode = myPreferences.getString("trainerCode", "")

        //If not trainer ID exists, create a random ID of 8 length
        if (trainerCode == "false") {
            trainerCode = alphaNumericString
        }

        //Put all data about trainer into a map for upload to DB
        val doctData = HashMap<String, Any>()
        doctData["first_name"] = first_name
        doctData["last_name"] = last_name
        doctData["experience"] = experience
        doctData["employment"] = employment
        doctData["aboutYou"] = aboutYou
        doctData["profilePic"] = imageLink!!
        doctData["trainerCode"] = trainerCode

        val batch = mDatabase!!.batch()

        val trainerColDocRef = mDatabase!!.collection("trainers").document(userID!!)

        val trainerCodeDocRef = mDatabase!!.collection("trainers")
                .document("0eh3S7vf62XX4DB2dsTG")

        val editorsDocRef = mDatabase!!.collection("users").document(userID!!)
                .collection("editors").document(userID!!)

        //First part create document for trainer using given data
        batch.set(trainerColDocRef, doctData)

        //Second part adds the new trainer's code to list of trainer codes
        batch.update(trainerCodeDocRef, "trainerCodes", FieldValue.arrayUnion(trainerCode))

        //Last part sets user as a trainer in his/her editors doc
        batch.update(editorsDocRef, "isTrainer", true)

        batch.commit().addOnSuccessListener {
            val end = System.currentTimeMillis()
            Log.d(TAG, "Batch success w/ time : " + (end - start))
            val myEditor = myPreferences.edit()
            myEditor.putString("trainerCode", trainerCode)
            myEditor.apply()

            if( imageToUpload == null && signUp == true)
            {
                val trainerIntent = Intent(applicationContext, TrainerProfileActivity::class.java)
                trainerIntent.putExtra("signUp", true)
                startActivity(trainerIntent)
            }
            Toast.makeText(applicationContext, "Success!", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {
            val end = System.currentTimeMillis()
            Log.d(TAG, "Batch failure w/ time : " + (end - start))
            Toast.makeText(applicationContext, "Failure!", Toast.LENGTH_SHORT).show()

        }
    }

    /*
     *@Name: Populate Trainer Register
     *
     *@Purpose: Populates page with information saved in the DB
     *
     *@Param in: Map containing data used for population (docData)
     *
     *@Brief: Function sets various fields to previous data and downloads
     *        the user's profile picture if there is one
     *
     *@ErrorsHandled: N/A
     */
    private fun populateTrainerRegister(docData: Map<String, Any>) {
        //If user is currently registered, populate the page with info
        if (docData.containsKey("experience")) {
            experienceEdit!!.setText(docData["experience"].toString())
            employmentEdit!!.setText(docData["employment"].toString())
            aboutYouEdit!!.setText(docData["aboutYou"].toString())
            val name = docData["profilePic"]
            closeSplashScreen()

            //If they have a profile picture, download it
            if (name != "") {
                uploadedImageName = name.toString()
                println("Image is not null")
                // Call function with kotlin's coroutines to remove possibility of halting other processes during load
                GlobalScope.launch {
                    //Thread to download profile pic if it exists
                    runOnUiThread(Runnable
                    {
                        downloadFile()
                    })
                }
            } else {
                println("Image is null")
                closeSplashScreen()
            }
        } else {
            closeSplashScreen()
        }
    }

    /*
     *@Name: Check if Trainer Register Exists
     *
     *@Purpose: Checks database to see if trainer is already registered
     *
     *@Param N/A
     *
     *@Brief: Functions tries to get the trainer's document from
     *        trainer sub-collection and, if so, populates the activity
     *
     *@ErrorsHandled: N/A
     */
    private fun checkIfTrainerRegisterExists(splashLocal: ImageView?) {
        val start = System.currentTimeMillis()
        val docRef = mDatabase!!.collection("trainers").document(userID!!)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document!!.exists()) {
                    var end = System.currentTimeMillis()
                    Log.d(TAG, "DocumentSnapshot data: " + document.data!!)
                    Log.d(TAG, "Logged at " + (end - start))
                    adjustUI()
                    signUp = false
                    populateTrainerRegister(document.data!!)
                    splashLocal?.clearAnimation()
                    closeSplashScreen()
                    end = System.currentTimeMillis()
                    Log.d(TAG, "Populate Logged at " + (end - start))
                } else {
                    splashLocal?.clearAnimation()
                    closeSplashScreen()
                    Log.d(TAG, "No such document")
                }
            } else {
                Log.d(TAG, "get failed with ", task.exception)
            }
        }
    }

    /*
     *@Name: Delete Current Profile Pic
     *
     *@Purpose: Deletes user's current profile picture
     *
     *@Param N/A
     *
     *@Brief: Function uses image's name to find it in Cloud Storage
     *        and then deletes it.
     *
     *@ErrorsHandled: N/A
     */
    private fun deleteCurrentProfilePic() {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child(uploadedImageName!!)
        imageRef.delete().addOnSuccessListener { Toast.makeText(this@TrainerRegisterActivity, "Delete success", Toast.LENGTH_SHORT).show() }.addOnFailureListener { Toast.makeText(this@TrainerRegisterActivity, "Delete failed", Toast.LENGTH_SHORT).show() }
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
        val imageRef = storageRef.child(uploadedImageName!!)

        val TEN_MEGABYTE = (10 * 1024 * 1024).toLong()
        imageRef.getBytes(TEN_MEGABYTE).addOnSuccessListener { bytes ->
            // Data for "images/island.jpg" is returns, use this as needed
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val roundDrawable = RoundedBitmapDrawableFactory.create(resources, bmp)
            roundDrawable.isCircular = true
            val scale = this@TrainerRegisterActivity.resources.displayMetrics.density
            mImage!!.layoutParams.height = (120 * scale + 0.5f).toInt()
            mImage!!.layoutParams.width = (120 * scale + 0.5f).toInt()
            mImage!!.requestLayout()
            mImage!!.setImageDrawable(roundDrawable)
        }.addOnFailureListener {
            //Toast.makeText(TrainerRegisterActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     *@Name: Unregister Trainer
     *
     *@Purpose: Unregisters the user as a trainer
     *
     *@Param N/A
     *
     *@Brief: Deletes user's trainer data atomically using a Cloud
     *        Function
     *
     *@ErrorsHandled: N/A
     */
    private fun unRegisterTrainer()
    {
        val start = System.currentTimeMillis()

        val batch = getUpdateTrainerData()

        pushUnregisterToClients(batch, start)

    }

    /*
     *@Name: Show Unregister Alert
     *
     *@Purpose: Displays a alert that allows the user to unregister
     *
     *@Param N/A
     *
     *@Brief: N/A
     *
     *@ErrorsHandled: N/A
     */
    private fun showUnregisterAlert() {
        val unregisterBuilder = AlertDialog.Builder(this@TrainerRegisterActivity)
        unregisterBuilder.setTitle("Are you sure you want to unregister as a trainer?")

        unregisterBuilder.setPositiveButton("Yes") { dialog, which -> unRegisterTrainer() }
        unregisterBuilder.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
        unregisterBuilder.show()
    }

    /*
     *@Name: Show Unregister Alert
     *
     *@Purpose: Displays a alert that allows the user to unregister
     *
     *@Param N/A
     *
     *@Brief: N/A
     *
     *@ErrorsHandled: N/A
     */
    private fun showRemovePicAlert() {
        val removePicBuilder = AlertDialog.Builder(this@TrainerRegisterActivity)
        removePicBuilder.setTitle("Are you sure you want to remove your profile picture?")

        removePicBuilder.setPositiveButton("Yes") { dialog, which ->
            mImage?.layout(0, 0, 0, 0)
            mImage?.setImageDrawable(null)
            mImage!!.visibility = View.GONE
            imageToUpload = null }
        removePicBuilder.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
        removePicBuilder.show()
    }

    /*
     *@Name: Check if Trainer Code Exists
     *
     *@Purpose: Checks to see if the generated trainer code is already
     *          in use by another trainer
     *
     *@Param N/A
     *
     *@Brief: Function downloads a document containing all the trainer codes
     *        and then checks to see if the given one is in the document
     *
     *@ErrorsHandled: N/A
     */
    private fun checkIfTrainerCodeExists(alphaString: String) {
        //Part of function checks to make sure the generated string is not already used by another user
        val start = System.currentTimeMillis()
        val docRef = mDatabase!!.collection("trainers").document("0eh3S7vf62XX4DB2dsTG")
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document!!.exists()) {
                    val end = System.currentTimeMillis()
                    Log.d(TAG, "DocumentSnapshot data: " + document.data!!)
                    Log.d(TAG, "Logged at " + (end - start))
                    val list = document.data!!["trainerCodes"] as ArrayList<String>
                    val index = list.indexOf(alphaString)

                    //Checks to see if generated string is already in use
                    if (index != -1) {
                        alphaNumericString
                    } else
                    {

                    }//Else, puts new on in list of trainer codes and uploads the new list
                } else {
                    Log.d(TAG, "No such document")
                }
            } else {
                Log.d(TAG, "get failed with ", task.exception)
            }
        }
    }

    /*
     *@Name: Adjust UI
     *
     *@Purpose: Change UI if the user is already a trainer
     *
     *@Param N/A
     *
     *@Brief: Function changes various text on screen and
     *        displays a new button that allows them to
     *        unregister
     *
     *@ErrorsHandled: N/A
     */

    private fun adjustUI() {
        titleText!!.text = "Trainer Settings"
        btnUnregister!!.visibility = View.VISIBLE
        val params = btnBecomeTrainer!!.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.ABOVE, R.id.btnUnregister)
        val params2 = botButtons!!.layoutParams as RelativeLayout.LayoutParams
        params2.height = params2.height * 2
        btnBecomeTrainer!!.layoutParams = params
        botButtons!!.layoutParams = params2
        btnBecomeTrainer!!.text = "Save"
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
        splashImage!!.visibility = View.INVISIBLE
        botButtons!!.visibility = View.VISIBLE
        registerInfo!!.visibility = View.VISIBLE
        trainerScroll!!.visibility = View.VISIBLE
    }


    /*
     *@Name: Update Trainer Data
     *
     *@Purpose: Updates data related to unregistering as a trainer
     *
     *@Param out: Batch containing some extra required updates for unregister
     *
     *@Brief: Batches two requests to update data that will cause the
     *        trainer to be successfully unregistered on completion
     *
     *@ErrorsHandled: N/A
     */
    private fun getUpdateTrainerData(): WriteBatch
    {

        val batch = mDatabase!!.batch()

        val myPreferences = PreferenceManager.getDefaultSharedPreferences(this@TrainerRegisterActivity)
        val trainerCode = myPreferences.getString("trainerCode", "")

        val trainerCodeDocRef = mDatabase!!.collection("trainers")
                .document("0eh3S7vf62XX4DB2dsTG")

        val editorsDocRef = mDatabase!!.collection("users").document(userID!!)
                .collection("editors").document(userID!!)

        //First write is to update the Trainer codes document with the new trainer's code
        batch.update(trainerCodeDocRef, "trainerCodes", FieldValue.arrayRemove(trainerCode))

        //Second write is to set the user as a trainer in his/her owner editors doc
        batch.update(editorsDocRef, "isTrainer", true)

        return batch
    }

    /*
     *@Name: Push Unregister To Clients
     *
     *@Purpose: Unregister clients with trainer for safe unregistration
     *
     *@Param in: Batch containing some necessary updates when unregistering (batch)
     *       in: Long indicating time expired since trying to unregister
     *
     *@Brief: Function gets list of clients and updates their editors data with the trainer
     *        no longer being a current trainer and then deletes the trainers profile since it
     *        is then safe to delete
     *
     *@ErrorsHandled: N/A
     */
    private fun pushUnregisterToClients(batch: WriteBatch, start: Long)
    {
        val myPreferences = PreferenceManager.getDefaultSharedPreferences(this@TrainerRegisterActivity)
        val userRef = mDatabase!!.collection("trainers").document(userID!!).collection("clientsCurrent")
        val query = userRef.limit(50)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful)
            {
                val documents = task.result!!.documents
                Log.d(TAG, "Getting documents successful")
                if (documents.size != 0)
                {
                    for (i in documents.indices)
                    {
                        Log.d(TAG, documents.get(i).id)
                        val trainerEditorDoc = mDatabase!!.collection("users")
                                .document(documents.get(i).id).collection("editors")
                                .document(userID!!)

                        batch.update(trainerEditorDoc, "isAccepted", false)
                    }

                    //Commits the batch with all required write requests to unregister
                    //On success, trainer profile is finally deleted
                    batch.commit().addOnSuccessListener {
                        val end = System.currentTimeMillis()
                        Log.d(TAG, "Batch success w/ time : " + (end - start))

                        //Delete user's profile picture if they have one
                        if (uploadedImageName != "")
                        {
                            deleteCurrentProfilePic()
                        }
                        //Set trainer code as false in shared preferences
                        val myEditor = myPreferences.edit()
                        myEditor.putString("trainerCode", "false")
                        myEditor.apply()
                        val trainerRegisterIntent = Intent(this@TrainerRegisterActivity, MainActivity::class.java)
                        deleteTrainerProfile()
                        Toast.makeText(applicationContext, "Success!", Toast.LENGTH_SHORT).show()
                        startActivity(trainerRegisterIntent)

                    }.addOnFailureListener {
                        val end = System.currentTimeMillis()
                        Log.d(TAG, "Batch failure w/ time : " + (end - start))
                        Toast.makeText(applicationContext, "Failure!", Toast.LENGTH_SHORT).show()
                    }
                }
                else
                {
                    Log.d(TAG, "No documents")
                }
            } else
            {
                Log.d(TAG, "Error getting documents: ", task.exception)
            }
        }
    }

    /*
 *@Name: Delete Trainer Profile
 *
 *@Purpose: Deletes the user's trainer profile
 *
 *@Param N/A
 *
 *@Brief: Cloud Function is called to recursively delete the user's
 *        trainer document so that its subcollections/docs are all deleted
 *        as well
 *
 *@ErrorsHandled: N/A
 */
    private fun deleteTrainerProfile()
    {
        val userID = userID

        val data = hashMapOf(
                "userID" to userID
        )

        functions.getHttpsCallable("recursiveDeleteTrainer")
                .call(data)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful)
                    {
                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val code = e.code
                            val details = e.details
                        }
                    }
                    else
                    {

                    }
                })
    }

    companion object {
        //Class variables
        private val RESULT_LOAD_IMAGE = 1
        private val TAG = "TrainerRegisterActivity"
    }
}
