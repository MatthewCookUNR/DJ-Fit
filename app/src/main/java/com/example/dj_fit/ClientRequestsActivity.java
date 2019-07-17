package com.example.dj_fit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

public class ClientRequestsActivity extends BaseActivity
{
    private static final String TAG = "ClientRequestsActivity";
    private int integer = 1;
    RelativeLayout clientReqLayout;
    TextView titleText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_requests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        clientReqLayout = findViewById(R.id.clientReqLayout);
        titleText = findViewById(R.id.titleText);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        checkForNewClients();

    }

    private void checkForNewClients()
    {
        String userID = mAuth.getUid();
        CollectionReference userRef = mDatabase.collection("trainers").document(userID).collection("clientRequests");
        Query query = userRef.limit(50);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    Log.d(TAG, "Getting documents successful");
                    if(documents.size() != 0)
                    {
                        //populatePossibleClients(documents);
                    }
                }
                else
                {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void populatePossibleClients( List<DocumentSnapshot> documents)
    {
        //for(int i = 0; i < documents.size(); i++)
        //{
            Map<String, Object> docData = documents.get(0).getData();
            TextView nameText = new TextView(ClientRequestsActivity.this);
            nameText.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
            nameText.setText(docData.get("first_name") + " " + docData.get("last_name"));
            RelativeLayout.LayoutParams paramsN = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if(integer == 1)
            {
                paramsN.addRule(RelativeLayout.BELOW, titleText.getId());
            }
            else
            {
                paramsN.addRule(RelativeLayout.BELOW, integer);
            }
            paramsN.leftMargin = 40;
            paramsN.rightMargin = 40;
            paramsN.topMargin = 80;
            nameText.setLayoutParams(paramsN);
            clientReqLayout.addView(nameText);
            integer++;

            LinearLayout butLayout = new LinearLayout(ClientRequestsActivity.this);
            LinearLayout.LayoutParams paramsA = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            Button acceptBut = new Button(ClientRequestsActivity.this);
            acceptBut.setText("Accept");
            acceptBut.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);


        //}
    }
}
