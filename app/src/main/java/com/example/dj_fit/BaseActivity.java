// Program Information /////////////////////////////////////////////////////////
/*
 * @file BaseActivity.java
 *
 * @brief Base Activity is used extended by most activities to allow them to use
 *        the options menu at the top right of the screen
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

// Base Activity Class ////////////////////////////////////////////////////////////////

public class BaseActivity extends AppCompatActivity
{
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id)
        {
            //Logs the user out of the application
            case R.id.action_signOut:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                final SharedPreferences myPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor myEditor = myPreferences.edit();
                myEditor.putString("first_name", "");
                myEditor.putString("last_name", "");
                myEditor.putString("trainerCode", "");
                myEditor.apply();
                Toast.makeText(this, "User has signed out", Toast.LENGTH_SHORT).show();
                Intent loginAct = new Intent(this, LoginActivity.class);
                startActivity(loginAct);
                return true;
            //Takes user to the main menu
            case R.id.action_mainMenu:
                Intent mainAct = new Intent(this, MainActivity.class);
                startActivity(mainAct);
                return true;
            //Displays a small about message
            case R.id.action_about:
                AlertDialog aboutDialog = new AlertDialog.Builder(this).create();
                aboutDialog.setTitle("About DJ FIT");
                aboutDialog.setMessage("Application is being developed by Matthew Cook and Collin Potter.");
                aboutDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                aboutDialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
