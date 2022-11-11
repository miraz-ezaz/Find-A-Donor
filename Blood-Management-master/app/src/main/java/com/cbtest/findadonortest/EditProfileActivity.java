package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private TextView backButton;
    private TextInputEditText registerfullName, registerNIDNumber,
            registerPhoneNumber;
    private Spinner bloodGroupsSpinner,regionSpinner;
    private Button updateButton;

    private DatabaseReference userDatabaseRef;
    private DatabaseReference regionDatabaseReference;
    private ArrayList<String> regionList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        registerfullName = findViewById(R.id.registerFullName);
        registerNIDNumber= findViewById(R.id.registerNIDNumber);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        bloodGroupsSpinner = findViewById(R.id.bloodGroupsSpinner);
        regionSpinner = findViewById(R.id.regionSpinner);
        backButton = findViewById(R.id.backButton);
        updateButton = findViewById(R.id.updateButton);
        regionDatabaseReference = FirebaseDatabase.getInstance().getReference();
        showRegionSpinner();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        readData();

        // Function for go to home activity(Main Activity)

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // write the updated data to the firebase
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = registerfullName.getText().toString().trim();
                final String nidNumber = registerNIDNumber.getText().toString().trim();
                final String phoneNumber = registerPhoneNumber.getText().toString().trim();
                final String bloodGroup = bloodGroupsSpinner.getSelectedItem().toString();
                final String region = regionSpinner.getSelectedItem().toString();
                if(TextUtils.isEmpty(fullName)){
                    registerfullName.setError("Name is Required");
                    return;
                }

                if(TextUtils.isEmpty(nidNumber)){
                    registerNIDNumber.setError("NID number is Required");
                    return;
                }

                if(TextUtils.isEmpty(phoneNumber)){
                    registerPhoneNumber.setError("Phone Number is Required");
                    return;
                }

                if (!TextUtils.isDigitsOnly(phoneNumber))
                {
                    registerPhoneNumber.setError("Enter Valid Phone Number");
                    return;
                }

                if(phoneNumber.length()<11 || phoneNumber.length()>11)
                {
                    registerPhoneNumber.setError("Phone number Should contain 11 digits only");
                    return;
                }

                if(bloodGroup.equals("Select Your Blood Group")){
                    Toast.makeText(EditProfileActivity.this,"Select Blood Group",Toast.LENGTH_LONG).show();
                    return;
                }

                if(region.equals("Select Your Region")){
                    Toast.makeText(EditProfileActivity.this,"Select Your Region",Toast.LENGTH_LONG).show();
                    return;
                }

                else
                {
                    HashMap userInfo = new HashMap();
                    userInfo.put("name",fullName);
                    userInfo.put("nidnumber",nidNumber);
                    userInfo.put("phonenumber",phoneNumber);
                    userInfo.put("bloodgroup",bloodGroup);
                    userInfo.put("region",region);

                    userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(EditProfileActivity.this, "Data set Sucessfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(EditProfileActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                            Intent intent = new Intent(EditProfileActivity.this,ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

            }
        });
        

    }

    //read current data from firebase

    private void readData() {
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    registerfullName.setText(snapshot.child("name").getValue().toString());
                    registerNIDNumber.setText(snapshot.child("nidnumber").getValue().toString());
                    registerPhoneNumber.setText(snapshot.child("phonenumber").getValue().toString());
                    String bloodGroup = snapshot.child("bloodgroup").getValue().toString();
                    String regions = snapshot.child("region").getValue().toString().trim();
                    bloodGroupsSpinner.setSelection( ((ArrayAdapter)bloodGroupsSpinner.getAdapter()).getPosition(bloodGroup));

                    ArrayList<String> regionList1 = new ArrayList<>();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("region");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            regionList1.clear();
                            for(DataSnapshot item:snapshot.getChildren()){
                                regionList1.add(item.getValue(String.class));
                            }
                            Collections.sort(regionList1, String.CASE_INSENSITIVE_ORDER);
                            regionList1.add(0,"Select Your Region");

                            ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(EditProfileActivity.this,R.layout.style_spinner,regionList1);
                            int pos = arrayAdapter1.getPosition(regions);
                            regionSpinner.setSelection(arrayAdapter1.getPosition(regions));
                            Log.e("Pain", String.valueOf(pos));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
// read regions from firebase
    private void showRegionSpinner() {
        regionDatabaseReference.child("region").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                regionList.clear();
                for(DataSnapshot item:snapshot.getChildren()){
                    regionList.add(item.getValue(String.class));
                }
                Collections.sort(regionList, String.CASE_INSENSITIVE_ORDER);
                regionList.add(0,"Select Your Region");

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EditProfileActivity.this,R.layout.style_spinner,regionList);
                regionSpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}