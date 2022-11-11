package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UpdateStatusActivity extends AppCompatActivity {
    private TextView totalDonted, plusDonate, minusDonate, theDate, backButton;
    private Button donatedToday;
    private SwitchCompat donateSwitch;
    private String dDate = "";
    private String dCount = "";
    private String dStatus = "";
    DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_status);

        totalDonted = findViewById(R.id.totalDonted);
        plusDonate = findViewById(R.id.plusDonate);
        minusDonate = findViewById(R.id.minusDonate);
        theDate = findViewById(R.id.theDate);
        backButton = findViewById(R.id.backButton);
        donateSwitch = findViewById(R.id.donateSwitch);
        donatedToday = findViewById(R.id.donatedToday);
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth
                .getInstance().getCurrentUser().getUid());
        dCount = "0";


        readStatus();

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String date = df.format(Calendar.getInstance().getTime());

        // switch to update the donation status

        donateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                    String todayDate = df.format(Calendar.getInstance().getTime());
                    if(!todayDate.equals(dDate)){
                        userRef.child("available").setValue("available");
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Your Last Donation Date is Today", Toast.LENGTH_SHORT).show();
                        donateSwitch.setChecked(false);
                    }


                }
                else {
                    userRef.child("available").setValue("notavailable");

                }
            }
        });
// increase number of donation
        plusDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count =  Integer.parseInt(dCount);
                count++;
                if(count<0){
                    count = 0;
                }
                userRef.child("totaldonation").setValue(count);

            }
        });
// decrease number of donation
        minusDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count =  Integer.parseInt(dCount);
                count--;
                if(count<0){
                    count = 0;
                }
                userRef.child("totaldonation").setValue(count);
            }
        });
        
        theDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText newDate = new EditText(v.getContext());
                newDate.setHint("dd-mm-yyyy");
                AlertDialog.Builder dateDialog = new AlertDialog.Builder(v.getContext());
                dateDialog.setTitle("Update Date");
                dateDialog.setMessage("Update your last donation date. Insert date in the following format(dd-mm-yyyy).");
                dateDialog.setView(newDate);

                dateDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String insertedDate = newDate.getText().toString().trim();
                        if (!TextUtils.isEmpty(insertedDate)){
                            userRef.child("lastdonationdate").setValue(insertedDate);

                        }
                        
                        else {
                            newDate.setError("Insert Date");
                        }
                    }
                });
                
                dateDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                });
                
                dateDialog.show();
                        
            }
        });
        // change donation status if donated today
        donatedToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String todayDate = df.format(Calendar.getInstance().getTime());
                if(!todayDate.equals(dDate)) {
                    userRef.child("available").setValue("notavailable");
                    int count = Integer.parseInt(dCount);
                    count++;
                    if (count < 0) {
                        count = 0;
                    }
                    userRef.child("totaldonation").setValue(count);
                    userRef.child("lastdonationdate").setValue(todayDate);
                }
                
                else {
                    Toast.makeText(getApplicationContext(), "Your Last Donation Date is Today", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateStatusActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        
    }
// read users donation status from firebase
    private void readStatus() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    totalDonted.setText("Total Donated: "+snapshot.child("totaldonation").getValue().toString()+" times");
                    theDate.setText(snapshot.child("lastdonationdate").getValue().toString());
                    dStatus = snapshot.child("available").getValue().toString();
                    dCount = snapshot.child("totaldonation").getValue().toString();
                    dDate = snapshot.child("lastdonationdate").getValue().toString();

                    if(dStatus.equals("available")){
                        donateSwitch.setChecked(true);
                    }
                    else {
                        donateSwitch.setChecked(false);
                    }
                    if (dDate.equals("Unknown"))
                    {
                        theDate.setText("Set Date");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}