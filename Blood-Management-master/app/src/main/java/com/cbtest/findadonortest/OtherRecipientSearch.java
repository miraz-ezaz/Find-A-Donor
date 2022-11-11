package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cbtest.findadonortest.Adapter.UserAdapter;
import com.cbtest.findadonortest.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OtherRecipientSearch extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private List<User> userList;
    private UserAdapter userAdapter;

    private Spinner bloodGroupsSpinner,regionSpinner;
    private Button searchDonorButton;
    private TextView donerNumber;

    private DatabaseReference regionDatabaseReference;
    private ArrayList<String> regionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_recipient_search);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Recipient");

        bloodGroupsSpinner = findViewById(R.id.bloodGroupsSpinner);
        regionSpinner = findViewById(R.id.regionSpinner);
        searchDonorButton = findViewById(R.id.searchDonorButton);
        donerNumber = findViewById(R.id.donerNumber);

        regionDatabaseReference = FirebaseDatabase.getInstance().getReference();
        showRegionSpinner();



        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(OtherRecipientSearch.this,userList);
        recyclerView.setAdapter(userAdapter);
        // search based on location
        searchDonorButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                final String bloodGroup = bloodGroupsSpinner.getSelectedItem().toString();
                final String region = regionSpinner.getSelectedItem().toString();
                if(bloodGroup.equals("Select Your Blood Group")){
                    Toast.makeText(OtherRecipientSearch.this,"Select Blood Group",Toast.LENGTH_LONG).show();
                    return;
                }

                if(region.equals("Select Your Region")){
                    Toast.makeText(OtherRecipientSearch.this,"Select Your Region",Toast.LENGTH_LONG).show();
                    return;
                }

                else {
                    readUser(region,bloodGroup);
                }
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

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(OtherRecipientSearch.this,R.layout.style_spinner,regionList);
                regionSpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // read user data from firabase

    private void readUser(String region, String group) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                //getSupportActionBar().setTitle(title+" Blood Donors in "+region);


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("users");
                Query query = reference.orderByChild("searchRegionGroup").equalTo("recipient"+region+group);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            User user = dataSnapshot.getValue(User.class);
                            if(!uid.equals(user.getId())){
                                userList.add(user);
                            }

                        }
                        userAdapter.notifyDataSetChanged();

                        if (userList.isEmpty())
                        {
                            donerNumber.setText("No "+group+" Recipient Found in "+region);
                        }
                        else
                        {
                            donerNumber.setText((userList.size())+" "+group+" Recipient Found in "+region);
                        }




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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