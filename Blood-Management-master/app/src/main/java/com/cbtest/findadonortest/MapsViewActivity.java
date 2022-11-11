package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.cbtest.findadonortest.Model.MyLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.cbtest.findadonortest.databinding.ActivityMapsViewBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsViewActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsViewBinding binding;
    private DatabaseReference userReference;
    private String uid = "";
    private Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getIntent().getExtras() != null){
            uid = getIntent().getStringExtra("uid");
            userReference= FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            readChanges();
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng local = new LatLng(-34, 151);
        myMarker = mMap.addMarker(new MarkerOptions().position(local).title("User is here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(local));
    }
    // read location from firebase
    private void readChanges() {

        userReference.child("location").child("sharing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if(snapshot.exists()){
                        MyLocation location = snapshot.getValue(MyLocation.class);

                        if(location!=null){
                            LatLng currntLocation = new LatLng(location.getLatitude(),location.getLongitude());
                            myMarker.setPosition(currntLocation);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currntLocation,18f));

                        }
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(MapsViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}