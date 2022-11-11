package com.cbtest.findadonortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationActivity extends AppCompatActivity {

    private TextView notificationPersonal,notificationGlobal;
    private DatabaseReference pNotification,gNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationPersonal = findViewById(R.id.notificationPersonal);
        notificationGlobal = findViewById(R.id.notificationGlobal);

        pNotification = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("notification");

        gNotification = FirebaseDatabase.getInstance().getReference().child("news");
// read personal notification
        pNotification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String n = snapshot.getValue().toString();
                    if (n.equals("null"))
                    {
                        notificationPersonal.setText("You have no Notification");
                    }

                    else
                    {
                        notificationPersonal.setText(n);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
// read global notification
        gNotification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String n = snapshot.getValue().toString();
                    if(n.equals("null"))
                    {
                        notificationGlobal.setText("No Update News!");
                    }

                    else {
                        notificationGlobal.setText(n);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}