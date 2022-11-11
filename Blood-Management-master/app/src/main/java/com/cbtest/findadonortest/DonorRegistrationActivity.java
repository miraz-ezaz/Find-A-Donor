package com.cbtest.findadonortest;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DonorRegistrationActivity extends AppCompatActivity {

    private TextView backButton;
    private CircleImageView profile_image;
    private TextInputEditText registerfullName, registerNIDNumber,
            registerPhoneNumber, registerEmail, registerPassword;
    private Spinner bloodGroupsSpinner,regionSpinner;
    private Button registerButton;

    private Uri resultUri;
    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private DatabaseReference regionDatabaseReference;
    private ArrayList<String> regionList = new ArrayList<>();
    private int img_set = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_registration);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DonorRegistrationActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        profile_image = findViewById(R.id.profile_image);
        registerfullName = findViewById(R.id.registerFullName);
        registerNIDNumber= findViewById(R.id.registerNIDNumber);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        bloodGroupsSpinner = findViewById(R.id.bloodGroupsSpinner);
        regionSpinner = findViewById(R.id.regionSpinner);
        registerButton = findViewById(R.id.registerButton);
        loader = new ProgressDialog(this);
        regionDatabaseReference = FirebaseDatabase.getInstance().getReference();

        showRegionSpinner();

        mAuth = FirebaseAuth.getInstance();

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
                img_set = 1;

            }
        });
        //Register user and insert data in firebase

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final String email = registerEmail.getText().toString().trim();
               final String password = registerPassword.getText().toString().trim();
               final String fullName = registerfullName.getText().toString().trim();
               final String nidNumber = registerNIDNumber.getText().toString().trim();
               final String phoneNumber = registerPhoneNumber.getText().toString().trim();
               final String bloodGroup = bloodGroupsSpinner.getSelectedItem().toString();
               final String region = regionSpinner.getSelectedItem().toString();

               if(TextUtils.isEmpty(email)){
                   registerEmail.setError("Email is Required");
                   return;
               }

               if(TextUtils.isEmpty(password)){
                   registerPassword.setError("Password is Required");
                   return;
               }

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

               if(bloodGroup.equals("Select Your Blood Group")){
                   Toast.makeText(DonorRegistrationActivity.this,"Select Blood Group",Toast.LENGTH_LONG).show();
                   return;
               }

                if(region.equals("Select Your Region")){
                    Toast.makeText(DonorRegistrationActivity.this,"Select Your Region",Toast.LENGTH_LONG).show();
                    return;
                }

               if(img_set == 0){
                   Toast.makeText(DonorRegistrationActivity.this, "Select a profile picture", Toast.LENGTH_LONG).show();
               }

               else {
                   loader.setMessage("Registering You....");
                   loader.setCanceledOnTouchOutside(false);
                   loader.show();
                   //Create new user
                   mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if(!task.isSuccessful()){
                               String error = task.getException().toString();
                               Toast.makeText(DonorRegistrationActivity.this,"Error" + error,Toast.LENGTH_SHORT).show();
                           }

                           else {
                               //Send verification email
                               FirebaseUser user = mAuth.getCurrentUser();
                               user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(@NonNull Void unused) {
                                       Toast.makeText(DonorRegistrationActivity.this, "Verification Email Has Been Sent", Toast.LENGTH_SHORT).show();

                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       Log.d(TAG,"onFailure: Email not sent " + e.getMessage());
                                   }
                               });




                               String currentUserId = mAuth.getCurrentUser().getUid();
                               userDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                       .child("users").child(currentUserId);

                               HashMap userInfo = new HashMap();
                               userInfo.put("id",currentUserId);
                               userInfo.put("name",fullName);
                               userInfo.put("email",email);
                               userInfo.put("nidnumber",nidNumber);
                               userInfo.put("phonenumber",phoneNumber);
                               userInfo.put("bloodgroup",bloodGroup);
                               userInfo.put("type","donor");
                               userInfo.put("notification","null");
                               userInfo.put("status","active");
                               userInfo.put("available","available");
                               userInfo.put("region",region);
                               userInfo.put("totaldonation","0");
                               userInfo.put("lastdonationdate","Unknown");
                               userInfo.put("search","donor"+bloodGroup);
                               userInfo.put("searchRegion","donor"+region);
                               userInfo.put("searchAvailable","donoravailable"+bloodGroup);
                               userInfo.put("searchRegionGroup","donor"+region+bloodGroup);
                               userInfo.put("searchRegionAvailable","donoravailable"+region+bloodGroup);
                               userInfo.put("searchStatus","activedonor"+bloodGroup);
                               userInfo.put("searchStatusRegion","active"+region+"donor"+bloodGroup);

                               //insert data in firebase


                               userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                   @Override
                                   public void onComplete(@NonNull Task task) {
                                       if(task.isSuccessful()){
                                           Toast.makeText(DonorRegistrationActivity.this, "Data set Sucessfully", Toast.LENGTH_SHORT).show();
                                       }
                                       else {
                                           Toast.makeText(DonorRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                       }

                                       finish();
                                       //loader.dismiss();
                                   }
                               });
                            // Upload image to firebase storage
                               if(resultUri != null){
                                   final StorageReference filepath = FirebaseStorage.getInstance().getReference()
                                           .child("profile images").child(currentUserId);

                                   Bitmap bitmap = null;

                                   try {
                                       bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);

                                   }catch (IOException e){
                                       e.printStackTrace();
                                   }

                                   ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                   bitmap.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);
                                   byte[] data = byteArrayOutputStream.toByteArray();
                                   UploadTask uploadTask = filepath.putBytes(data);

                                   uploadTask.addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           Toast.makeText(DonorRegistrationActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                                       }
                                   });

                                   uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                       @Override
                                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                           if(taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null){
                                               Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                               result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                   @Override
                                                   public void onSuccess(Uri uri) {
                                                       String imageUrl = uri.toString();
                                                       Map newimageMap = new HashMap();
                                                       newimageMap.put("profilepictureurl",imageUrl);
                                                       userDatabaseRef.updateChildren(newimageMap).addOnCompleteListener(new OnCompleteListener() {
                                                           @Override
                                                           public void onComplete(@NonNull Task task) {
                                                               if (task.isSuccessful()){
                                                                   Toast.makeText(DonorRegistrationActivity.this, "Image Url added to databse ", Toast.LENGTH_SHORT).show();
                                                               }
                                                               
                                                               else {
                                                                   Toast.makeText(DonorRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                               }
                                                           }
                                                       });

                                                       finish();
                                                   }
                                               });
                                           }
                                       }
                                   });

                                   Intent intent = new Intent(DonorRegistrationActivity.this,MainActivity.class);
                                   startActivity(intent);
                                   finish();
                                   loader.dismiss();

                               }


                           }
                       }
                   });


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

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(DonorRegistrationActivity.this,R.layout.style_spinner,regionList);
                regionSpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            resultUri = data.getData();
            profile_image.setImageURI(resultUri);
        }
    }
}