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

    //Function queries DB to see if their are any clients requesting the user as a trainer
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
                        populatePossibleClients(documents);
                    }
                }
                else
                {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    //Function populates the page with the list of clients requesting the trainer
    private void populatePossibleClients( List<DocumentSnapshot> documents)
    {
        for(int i = 0; i < documents.size(); i++)
        {
            Map<String, Object> docData = documents.get(i).getData();
            TextView nameText = new TextView(ClientRequestsActivity.this);
            nameText.setTextAppearance(this, android.R.style.TextAppearance_Large);
            nameText.setText(docData.get("first_name") + " " + docData.get("last_name"));
            nameText.setId(integer);
            RelativeLayout.LayoutParams paramsN = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if(integer == 1)
            {
                paramsN.addRule(RelativeLayout.BELOW, titleText.getId());
            }
            else
            {
                paramsN.addRule(RelativeLayout.BELOW, integer-1);
            }
            paramsN.leftMargin = 40;
            paramsN.rightMargin = 40;
            paramsN.topMargin = 80;
            nameText.setLayoutParams(paramsN);
            clientReqLayout.addView(nameText);
            integer++;

            LinearLayout butLayout = new LinearLayout(ClientRequestsActivity.this);
            RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsR.addRule(RelativeLayout.BELOW, integer - 1);
            paramsR.leftMargin = 40;
            butLayout.setId(integer);
            butLayout.setLayoutParams(paramsR);
            integer++;

            LinearLayout.LayoutParams paramsB = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsB.weight = 1;

            Button acceptBut = createAcceptButton(documents.get(i).getId());
            acceptBut.setLayoutParams(paramsB);

            Button declineBut = createDeclineButton(documents.get(i).getId());
            declineBut.setLayoutParams(paramsB);

            butLayout.addView(acceptBut);
            butLayout.addView(declineBut);
            clientReqLayout.addView(butLayout);
        }
    }

    private Button createAcceptButton(String documentID)
    {
        final Button acceptBut = new Button(ClientRequestsActivity.this);
        acceptBut.setText("Accept");
        acceptBut.setTextSize(16);
        acceptBut.setTag(documentID);
        acceptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserAsClient(v.getTag().toString());
            }
        });
        return acceptBut;
    }

    private Button createDeclineButton(String documentID)
    {
        Button declineBut = new Button(ClientRequestsActivity.this);
        declineBut.setText("Decline");
        declineBut.setTextSize(16);
        declineBut.setTag(documentID);
        declineBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeClientRequest(v.getTag().toString());
            }
        });
        return declineBut;
    }

    private void addUserAsClient(String clientID)
    {

    }

    private void removeClientRequest(String clientID)
    {

    }
}
