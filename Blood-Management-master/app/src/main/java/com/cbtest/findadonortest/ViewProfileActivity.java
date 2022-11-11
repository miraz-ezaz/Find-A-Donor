package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String uid = "";
    private TextView type,name,available,lastDonation,totalDonation,region,phoneNumber,bloodGroup;
    private CircleImageView profileImage;
    private Button backButton,callNow,request,shareLocation,saveNow,reportUser;
    private DatabaseReference userRef;
    private String phone="";
    private String userName="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        type = findViewById(R.id.type);
        name = findViewById(R.id.name);
        available = findViewById(R.id.availableStatus);
        lastDonation = findViewById(R.id.lastDonation);
        totalDonation = findViewById(R.id.totalDonation);
        region = findViewById(R.id.region);
        phoneNumber = findViewById(R.id.phoneNumber);
        bloodGroup = findViewById(R.id.bloodGroup);

        profileImage = findViewById(R.id.profileImage);

        backButton = findViewById(R.id.backButton);
        callNow = findViewById(R.id.callNow);
        request = findViewById(R.id.sendRequest);
        shareLocation = findViewById(R.id.sendLocation);
        saveNow = findViewById(R.id.saveNow);
        reportUser = findViewById(R.id.repotrUser);

        if(getIntent().getExtras() != null){
            uid = getIntent().getStringExtra("UserID");
            userRef= FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            readUser();
            isSaved();
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
// dial the users number
        callNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(ViewProfileActivity.this)
                        .setTitle("Call Now")
                        .setMessage("Do you want to call "+ userName+"?")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:"+phone));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();




            }
        });

        saveNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if ((saveNow.getText()).equals("Saved")){
                    new AlertDialog.Builder(ViewProfileActivity.this)
                            .setTitle("User Already Saved")
                            .setMessage("The user is already saved. Do You want to remove?")
                            .setCancelable(false)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    userRef.child("savedUser").child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            saveNow.setText("Save User");
                                            saveNow.setBackgroundResource(R.drawable.button_background);
                                        }
                                    });



                                }
                            })
                            .setNegativeButton("No",null)
                            .show();

                }

                else
                {
                    userRef.child("savedUser").child(uid).setValue(uid);
                    isSaved();
                }



            }
        });

        shareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this,MapsViewActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);

            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this,SendRequestActivity.class);
                intent.putExtra("userID",uid);
                startActivity(intent);
            }
        });

        reportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this,ReportActivity.class);
                intent.putExtra("userID",uid);
                startActivity(intent);
            }
        });

    }

    private void isSaved() {
        userRef.child("savedUser").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot id:snapshot.getChildren()){
                    if (uid.equals(id.getValue().toString())){
                        saveNow.setText("Saved");
                        saveNow.setBackgroundResource(R.drawable.button_backgroud_saved);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void readUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("type").getValue().toString().equals("donor")) {
                        type.setText(snapshot.child("type").getValue().toString());
                        name.setText("Name: " + (snapshot.child("name").getValue().toString()));
                        available.setText("Status: " + snapshot.child("available").getValue().toString().toUpperCase());
                        lastDonation.setText("Last Donated On: " + snapshot.child("lastdonationdate").getValue().toString());
                        phoneNumber.setText("Phone Number: " + snapshot.child("phonenumber").getValue().toString());
                        bloodGroup.setText("Blood Group: " + snapshot.child("bloodgroup").getValue().toString());
                        totalDonation.setText("Total Donated: " + snapshot.child("totaldonation").getValue().toString() + " times");
                        region.setText("Region: " + snapshot.child("region").getValue().toString());
                        Glide.with(getApplicationContext()).load(snapshot.child("profilepictureurl").getValue().toString()).into(profileImage);
                        phone = snapshot.child("phonenumber").getValue().toString();
                        userName = snapshot.child("name").getValue().toString();
                        getSupportActionBar().setTitle(userName);
                    }

                    else {
                        type.setText(snapshot.child("type").getValue().toString());
                        name.setText("Name: " + (snapshot.child("name").getValue().toString()));
                        phoneNumber.setText("Phone Number: " + snapshot.child("phonenumber").getValue().toString());
                        bloodGroup.setText("Blood Group: " + snapshot.child("bloodgroup").getValue().toString());
                        region.setText("Region: " + snapshot.child("region").getValue().toString());
                        Glide.with(getApplicationContext()).load(snapshot.child("profilepictureurl").getValue().toString()).into(profileImage);
                        phone = snapshot.child("phonenumber").getValue().toString();
                        userName = snapshot.child("name").getValue().toString().toUpperCase();
                        getSupportActionBar().setTitle(userName);
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

