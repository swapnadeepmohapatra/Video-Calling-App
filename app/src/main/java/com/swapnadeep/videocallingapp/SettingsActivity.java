package com.swapnadeep.videocallingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;

public class SettingsActivity extends AppCompatActivity {

    private Button saveButton;
    private EditText userName, bioText;
    private ImageView imageViewProfile;
    private ProgressDialog progressDialog;

    private static int GalleryPick = 1;
    private Uri uri;
    private String downloadUrl;

    private StorageReference userProfilePicRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveButton = findViewById(R.id.save_button);
        userName = findViewById(R.id.username_settings);
        bioText = findViewById(R.id.bio_settings);
        imageViewProfile = findViewById(R.id.settings_profile_image);

        userProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(this);


        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallerIntent = new Intent();
                gallerIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallerIntent.setType("image/*");
                startActivityForResult(gallerIntent, GalleryPick);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        getUserInfo();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            imageViewProfile.setImageURI(uri);
        }
    }

    private void saveUserData() {
        final String userNameVal = userName.getText().toString();
        final String bioVal = bioText.getText().toString();

        if (uri == null) {
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show();
        } else if (userNameVal.equals("")) {
            userName.setError("User Name is Required");
        } else if (bioVal.equals("")) {
            bioText.setError("Bio is Required");
        } else {
            progressDialog.setTitle("Please Wait...");
            progressDialog.setMessage("Profile Updating");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            final StorageReference filePath = userProfilePicRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

            final UploadTask uploadTask = filePath.putFile(uri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    } else {
                        downloadUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadUrl = task.getResult().toString();

                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name", userNameVal);
                        profileMap.put("status", bioVal);
                        profileMap.put("image", downloadUrl);

                        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void getUserInfo() {
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imageDb = dataSnapshot.child("image").getValue().toString();
                    String nameDb = dataSnapshot.child("name").getValue().toString();
                    String bioDb = dataSnapshot.child("status").getValue().toString();

                    userName.setText(nameDb);
                    bioText.setText(bioDb);
                    Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(imageViewProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}