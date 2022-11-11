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

public class ReportActivity extends AppCompatActivity {

    private TextView reciverName,reciverPhone;
    private TextView backButton;

    private TextInputEditText requestTittle, requestDescription;
    private Button sendButton;

    private String reportId;


    DatabaseReference senderRef, receiverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        reciverName = findViewById(R.id.reciverName);
        reciverPhone = findViewById(R.id.reciverPhone);



        backButton = findViewById(R.id.backButton);

        requestTittle = findViewById(R.id.requestTittle);
        requestDescription = findViewById(R.id.requestDescription);

        sendButton = findViewById(R.id.SendRequestButton);

        if (getIntent().getExtras() != null) {
            reportId = getIntent().getStringExtra("userID");
            senderRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            receiverRef = FirebaseDatabase.getInstance().getReference().child("users").child(reportId);
        }

        receiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    reciverName.setText("Report The user: "+(snapshot.child("name").getValue().toString()));
                    reciverPhone.setText("Phone Number: "+snapshot.child("phonenumber").getValue().toString());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
// add new report to the firebase database
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tittle = requestTittle.getText().toString().trim();
                final String description = requestDescription.getText().toString().trim();
                final String reportBy = (FirebaseAuth.getInstance().getCurrentUser().getUid()).toString();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy : hh:mm aaa");
                String date = df.format(Calendar.getInstance().getTime());

                if(TextUtils.isEmpty(tittle)){
                    requestTittle.setError("Subject Required");
                    return;
                }

                if(TextUtils.isEmpty(description)){
                    requestDescription.setError("Description is required");
                }

                else
                {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    HashMap report = new HashMap();

                    final String ID = reference.child("report").push().getKey();
                    report.put("id",ID);
                    report.put("reportBy",reportBy);
                    report.put("reportId", reportId);
                    report.put("timeStamp",date);
                    report.put("tittle",tittle);
                    report.put("state","initial");
                    report.put("description",description);
                    reference.child("report").child(ID).setValue(report);
                    Toast.makeText(ReportActivity.this, "Report Submitted Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ReportActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }

            }
        });

    }
}