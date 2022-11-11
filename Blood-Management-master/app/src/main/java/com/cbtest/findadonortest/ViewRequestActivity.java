package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ViewRequestActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView senderName, recieverName, timeStamp, tittle, description, reply, replytime,rqstStatus;
    private LinearLayout replyLayout;
    private Button SendReplyButton, viewProfile, deleteRequest,backButton;
    private TextInputEditText replyDescription;
    private DatabaseReference userRef;
    private String rid,pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        senderName = findViewById(R.id.senderName);
        recieverName = findViewById(R.id.recieverName);
        timeStamp = findViewById(R.id.timeStamp);
        tittle = findViewById(R.id.tittle);
        description = findViewById(R.id.description);
        reply = findViewById(R.id.reply);
        replytime = findViewById(R.id.replytime);
        rqstStatus = findViewById(R.id.rqstStatus);

        replyLayout = findViewById(R.id.replyLayout);

        SendReplyButton = findViewById(R.id.SendReplyButton);
        viewProfile = findViewById(R.id.viewProfile);
        deleteRequest = findViewById(R.id.deleteRequest);
        backButton = findViewById(R.id.backButton);

        replyDescription = findViewById(R.id.replyDescription);

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if(getIntent().getExtras() != null){
            rid = getIntent().getStringExtra("requestId");
            userRef= FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            readRequest();
        }

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewRequestActivity.this, ViewProfileActivity.class);
                intent.putExtra("UserID",pid);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewRequestActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        deleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    userRef.child("requests").child(rid).removeValue();
                    Intent intent = new Intent(ViewRequestActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                catch (Exception e)
                {
                    Intent intent = new Intent(ViewRequestActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });

        SendReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String replyText = replyDescription.getText().toString().trim();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy : hh:mm aaa");
                String date = df.format(Calendar.getInstance().getTime());
                if(TextUtils.isEmpty(replyText))
                {
                    replyDescription.setError("Reply Can't be blank");
                    return;
                }

                else
                {
                    //Update Reply

                    userRef.child("requests").child(rid).child("sate").setValue("Replied");
                    userRef.child("requests").child(rid).child("reply").setValue(replyText);
                    userRef.child("requests").child(rid).child("replytime").setValue(date);
                    DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference().child("users").child(pid);
                    senderReference.child("requests").child(rid).child("sate").setValue("Replied");
                    senderReference.child("requests").child(rid).child("reply").setValue(replyText);
                    senderReference.child("requests").child(rid).child("replytime").setValue(date);

                    Intent intent = new Intent(ViewRequestActivity.this, ViewRequestActivity.class);
                    intent.putExtra("requestId",rid);
                    startActivity(intent);
                    finish();

                }
            }
        });




    }

    // Read the request from database

    private void readRequest() {
        userRef.child("requests").child(rid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String sederId = snapshot.child("senderId").getValue().toString();
                    String receiverId = snapshot.child("receiverId").getValue().toString();
                    tittle.setText("Subject: "+snapshot.child("tittle").getValue().toString());
                    description.setText("Description:\n"+snapshot.child("description").getValue().toString());
                    timeStamp.setText("Sent at: " + snapshot.child("timeStamp").getValue().toString());
                    String replied = snapshot.child("reply").getValue().toString();
                    String state = snapshot.child("sate").getValue().toString();


                    if(sederId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    {
                        senderName.setText("Sent By: You");
                        pid = receiverId;
                        replyLayout.setVisibility(View.GONE);
                        rqstStatus.setText("Status: "+state);
                        DatabaseReference receiverReference = FirebaseDatabase.getInstance().getReference().child("users").child(receiverId);
                        receiverReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    recieverName.setText("Received by: "+snapshot.child("name").getValue().toString());
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        if (!replied.equals("null"))
                        {
                            reply.setText("Reply:\n"+replied);
                            replytime.setText("Replied at: "+snapshot.child("replytime").getValue().toString());
                        }

                        else
                        {
                            reply.setText("Reply: Not Replied");
                            replytime.setVisibility(View.GONE);
                        }



                    }

                    if (receiverId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    {
                        recieverName.setText("Received by: You");
                        pid = sederId;
                        DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference().child("users").child(sederId);
                        senderReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.exists()){
                                    senderName.setText("Sent By: "+snapshot.child("name").getValue().toString());
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        if(state.equals("Sent"))
                        {
                            userRef.child("requests").child(rid).child("sate").setValue("Seen");
                            senderReference.child("requests").child(rid).child("sate").setValue("Seen");
                        }

                        rqstStatus.setText("Status: "+state);

                        if (!replied.equals("null"))
                        {
                            reply.setText("Reply:\n"+replied);
                            replytime.setText("Replied at: "+snapshot.child("replytime").getValue().toString());
                            replyLayout.setVisibility(View.GONE);
                        }

                        else
                        {
                            reply.setText("Reply: Not Replied");
                            replytime.setVisibility(View.GONE);
                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}