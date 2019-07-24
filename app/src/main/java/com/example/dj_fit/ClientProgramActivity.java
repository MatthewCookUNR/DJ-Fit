package com.example.dj_fit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ClientProgramActivity extends BaseActivity
{
    TextView titleText;
    String userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_program);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String[] clientData = getIntent().getStringExtra("clientTag").split("/");
        titleText = findViewById(R.id.titleText);
        titleText.setText(clientData[1] + " " + clientData[2]);
    }

}
