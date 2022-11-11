package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

public class SendRequestActivity extends AppCompatActivity {

    private TextView reciverName, reciverBloodGroup, reciverPhone;
    private TextView backButton;

    private TextInputEditText requestTittle, requestDescription;
    private Button sendButton;

    private String receiverId;


    DatabaseReference senderRef, receiverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);

        reciverName = findViewById(R.id.reciverName);
        reciverPhone = findViewById(R.id.reciverPhone);
        reciverBloodGroup = findViewById(R.id.reciverBloodGroup);


        backButton = findViewById(R.id.backButton);

        requestTittle = findViewById(R.id.requestTittle);
        requestDescription = findViewById(R.id.requestDescription);

        sendButton = findViewById(R.id.SendRequestButton);

        if (getIntent().getExtras() != null) {
            receiverId = getIntent().getStringExtra("userID");
            senderRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            receiverRef = FirebaseDatabase.getInstance().getReference().child("users").child(receiverId);
        }

        receiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    reciverName.setText("Send Request To: "+(snapshot.child("name").getValue().toString()));
                    reciverPhone.setText("Phone Number: "+snapshot.child("phonenumber").getValue().toString());
                    reciverBloodGroup.setText("Blood Group: " +snapshot.child("bloodgroup").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
// insert new request data to the firebase database
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tittle = requestTittle.getText().toString().trim();
                final String description = requestDescription.getText().toString().trim();
                final String senderID = (FirebaseAuth.getInstance().getCurrentUser().getUid()).toString();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy : hh:mm aaa");
                String date = df.format(Calendar.getInstance().getTime());

                if(TextUtils.isEmpty(tittle)){
                    requestTittle.setError("Subject Required");
                    return;
                }

                if(TextUtils.isEmpty(description)){
                    requestDescription.setError("Description is required");
                }

                else {
                    HashMap sender = new HashMap();
                    HashMap receiver = new HashMap();

                    sender.put("receiverId",receiverId);
                    sender.put("senderId",senderID);
                    sender.put("tittle",tittle);
                    sender.put("description",description);
                    sender.put("sate","sent");
                    sender.put("reply","null");
                    sender.put("timeStamp",date);
                    sender.put("replytime","null");


                    receiver.put("senderId",senderID);
                    receiver.put("receiverId",receiverId);
                    receiver.put("tittle",tittle);
                    receiver.put("description",description);
                    receiver.put("sate","Sent");
                    receiver.put("reply","null");
                    receiver.put("timeStamp",date);
                    receiver.put("replytime","null");


                    final String requestID = senderRef.child("requests").push().getKey();
                    sender.put("requestID",requestID);
                    receiver.put("requestID",requestID);

                    senderRef.child("requests").child(requestID).setValue(sender);
                    receiverRef.child("requests").child(requestID).setValue(receiver);

                    Toast.makeText(SendRequestActivity.this, "Request send Successfully", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SendRequestActivity.this,SentRequestActivity.class);
                    startActivity(intent);
                    finish();

                }






            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendRequestActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

}