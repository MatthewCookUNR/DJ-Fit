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

//Activity shows data on exercise when "View" is clicked on the Workout Outline page
public class PopActivity extends YouTubeBaseActivity {

    YouTubePlayerView mYouTubePlayerView;
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

        btnSaveVid = findViewById(R.id.btnSaveVid);
        linksView = findViewById(R.id.linksView);
        btnAddVideo = findViewById(R.id.btnAddVideo);
        btnClearVideos = findViewById(R.id.btnClearVideos);
        editAddVideo = findViewById(R.id.editAddVideo);
        mYouTubePlayerView = findViewById(R.id.youtubePlay);

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
                if(!wasRestored)
                {
                    mYouTubePlayer = youTubePlayer;
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
                    if (link.contains(".be/"))
                    {
                        String delims = "be/";
                        String[] tokens = link.split(delims);
                        addedVideos.add(tokens[1]);
                    }
                    else if (link.contains("v="))
                    {
                        String delims = "v=";
                        String[] tokens = link.split(delims);
                        addedVideos.add(tokens[1]);
                    }
                    else
                    {
                        Toast toast = Toast.makeText(PopActivity.this, "Please enter a valid youtube address", Toast.LENGTH_SHORT);
                        toast.show();
                        throw new Exception();
                    }

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
                intent.putExtra("key", editAddVideo.getText().toString());
                System.out.println(intent.getStringExtra("key"));
                setResult(RESULT_OK, intent);
                finish();
            }
        });

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

    private void fullScreenLayout()
    {
        linksView.setVisibility(View.GONE);
        btnAddVideo.setVisibility(View.GONE);
        editAddVideo.setVisibility(View.GONE);
        btnSaveVid.setVisibility(View.GONE);
        btnClearVideos.setVisibility(View.GONE);
    }

}
