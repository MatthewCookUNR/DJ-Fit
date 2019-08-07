// Program Information /////////////////////////////////////////////////////////
/*
 * @file PopActivity.java
 *
 * @brief Shows the pop activity for watching youtube videos attached to exercises
 *        when the user clicks "View"
 *
 * @author Matthew Cook
 *
 */

// PACKAGE AND IMPORTED FILES ////////////////////////////////////////////////////////////////

package com.example.dj_fit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

// Pop Activity Class ////////////////////////////////////////////////////////////////

public class PopActivity extends YouTubeBaseActivity {

    //Class variables
    private YouTubePlayerView mYouTubePlayerView;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    List<String> addedVideos = new ArrayList<>();
    TextView linksView;
    Button btnSaveVid, btnAddVideo, btnClearVideos;
    EditText editAddVideo;
    YouTubePlayer mYouTubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        //Views and variables initialization
        btnSaveVid = findViewById(R.id.btnSaveVid);
        linksView = findViewById(R.id.linksView);
        btnAddVideo = findViewById(R.id.btnAddVideo);
        btnClearVideos = findViewById(R.id.btnClearVideos);
        editAddVideo = findViewById(R.id.editAddVideo);
        mYouTubePlayerView = findViewById(R.id.youtubePlay);

        addedVideos = getIntent().getStringArrayListExtra("videos");
        System.out.println("Videos to popup " + addedVideos);

        //Display metric changes to make it appear as a pop up
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.8), (int)(height*.7));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 20;
        getWindow().setAttributes(params);

        //Handles playing videos for YouTube Player
        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean wasRestored)
            {
                mYouTubePlayer = youTubePlayer;
                //Cue videos if video player was not restored and there are videos to play
                if(!wasRestored)
                {
                    if(!addedVideos.isEmpty())
                    {
                        youTubePlayer.cueVideos(addedVideos);
                    }
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
            {
                System.out.println("Initialize failed...");
            }
        };

        //Button adds a video to the list of videos for the exercise
        btnAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String link = editAddVideo.getText().toString();
                editAddVideo.setText("");
                try
                {
                    //Case that youtube link comes from mobile android
                    if (link.contains(".be/"))
                    {
                        String delims = "be/";
                        String[] tokens = link.split(delims);
                        addedVideos.add(tokens[1]);
                    }
                    //Case copied link comes from PC player
                    else if (link.contains("v="))
                    {
                        String delims = "v=";
                        String[] tokens = link.split(delims);
                        addedVideos.add(tokens[1]);
                    }
                    //Case that youtube link is a bad link
                    else
                    {
                        Toast toast = Toast.makeText(PopActivity.this, "Please enter a valid youtube address", Toast.LENGTH_SHORT);
                        toast.show();
                        throw new Exception();
                    }

                    //If youtube player is not null, release so it can restarted with new video added
                    if (mYouTubePlayer != null)
                    {
                        mYouTubePlayer.release();
                    }
                    mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
                }
                catch (Exception e)
                {
                    System.out.println("Error in adding video button functionality");
                }
            }
        });

        //Button closes pop up activity and sends data back
        btnSaveVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("id", getIntent().getIntExtra("id", 0));
                intent.putStringArrayListExtra("videos", (ArrayList<String>) addedVideos);
                System.out.println("Video list in popup: " + addedVideos );
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //Clears all of the added videos
        btnClearVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder askClear = new AlertDialog.Builder(PopActivity.this);
                askClear.setTitle("Are you sure you wish to clear your saved videos?");
                askClear.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        addedVideos.clear();
                        mYouTubePlayer.release();
                    }
                });
                askClear.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog mDialog = askClear.create();
                mDialog.show();

                final Button positiveButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                LinearLayout.LayoutParams positiveParam = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                positiveParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
                positiveButton.setLayoutParams(positiveParam);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //If orientation is landscape, show the fullscreen version of activity
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            fullScreenLayout();
            mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
        }
        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
        }
    }

    // Function definitions ////////////////////////////////////////////////////////

    /*
     *@Name: Fullscreen Layout
     *
     *@Purpose: Updates UI to make it fullscreen compatible
     *
     *@Param N/A
     *
     *@Brief: Clears the layout of views so that fullscreen on YouTube
     *        Player can be shown
     *
     *@ErrorsHandled: N/A
     */
    private void fullScreenLayout()
    {
        linksView.setVisibility(View.GONE);
        btnAddVideo.setVisibility(View.GONE);
        editAddVideo.setVisibility(View.GONE);
        btnSaveVid.setVisibility(View.GONE);
        btnClearVideos.setVisibility(View.GONE);
    }

}
