package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShareLocationActivity extends AppCompatActivity {

    private Button shareLocationButton;
    private TextView backButton;

    private DatabaseReference userReference;
    private Location lastLocation;

    private LocationManager manager;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);

        shareLocationButton = findViewById(R.id.shareLocationButton);
        backButton = findViewById(R.id.backButton);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ShareLocationActivity.this);
        getLocationUpdates();
        userReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShareLocationActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
// insert  users last knows location to firebase database
        shareLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastLocation!=null){
                    userReference.child("location").child("sharing").setValue(lastLocation);
                    Intent intent = new Intent(ShareLocationActivity.this,MapsShareActivity.class);
                    startActivity(intent);
                }

                else {
                    Toast.makeText(ShareLocationActivity.this, "Error Sharing Location! Enable Location & Try again.", Toast.LENGTH_SHORT).show();
                    getLocationUpdates();
                }

            }
        });

    }

    // get user's last known location

    private void getLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED) {

            if (manager != null) {
                if ((manager.isProviderEnabled(LocationManager.GPS_PROVIDER))||(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        lastLocation = location;
                                    }
                                    else {
                                        Toast.makeText(ShareLocationActivity.this, "Enable Location", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            });
                }

                 else {
                    Toast.makeText(ShareLocationActivity.this, "No Provider Enabled! Enable Location.", Toast.LENGTH_SHORT).show();
                }
            }


        }

        else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocationUpdates();
            }

            else {
                Toast.makeText(ShareLocationActivity.this, "Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }


}