package com.example.dj_fit;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

    Button btnClose, btnPlayVideos, btnAddVideo;
    EditText editAddVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        btnClose = findViewById(R.id.btnClose);
        btnPlayVideos = findViewById(R.id.btnPlayVideos);
        btnAddVideo = findViewById(R.id.btnAddVideo);
        editAddVideo = findViewById(R.id.editAddVideo);
        mYouTubePlayerView = findViewById(R.id.youtubePlay);

        //Handles playing videos for YouTube Player
        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b)
            {
                youTubePlayer.loadVideos(addedVideos);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
            {
                Toast.makeText(PopActivity.this, youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
            }
        };

        //Button plays videos saved for video player/
        btnPlayVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
            }
        });

        //Button adds a video to the list of videos for the exercise
        btnAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = editAddVideo.getText().toString();
                String delims = "be/";
                String [] tokens = link.split(delims);
                addedVideos.add(tokens[1]);
            }
        });

        //Button closes pop up activity and sends data back
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                System.out.println(editAddVideo.getText());
                intent.putExtra("key", editAddVideo.getText().toString());
                System.out.println(intent.getStringExtra("key"));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
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


    }
}
