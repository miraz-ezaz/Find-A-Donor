package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cbtest.findadonortest.Adapter.UserAdapter;
import com.cbtest.findadonortest.Model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchPhoneActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private List<User> userList;
    private UserAdapter userAdapter;

    private Button searchDonorButton;
    private TextView donerNumber;

    private TextInputEditText registerPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_phone);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search By Phone");

        searchDonorButton = findViewById(R.id.searchDonorButton);
        donerNumber = findViewById(R.id.donerNumber);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(SearchPhoneActivity.this,userList);
        recyclerView.setAdapter(userAdapter);
        searchDonorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phoneNumber = registerPhoneNumber.getText().toString().trim();

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

                else {
                    readUser(phoneNumber);
                }

            }
        });

    }

    // read the user data from firebase based on phone number

    private void readUser(String phone) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                //getSupportActionBar().setTitle(title+" Blood Donors in "+region);


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("users");
                Query query = reference.orderByChild("phonenumber").equalTo(phone);
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
                            donerNumber.setText("User Not Found in");
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