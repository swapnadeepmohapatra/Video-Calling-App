package com.swapnadeep.videocallingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImageView;
    private ImageView cancelButton, callButton;
    private String receiverUserId, receiverUserImage, receiverUserName;
    private String senderUserId, senderUserImage, senderUserName;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        receiverUserId = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        nameContact = findViewById(R.id.name_calling);
        profileImageView = findViewById(R.id.profile_image_calling);
        cancelButton = findViewById(R.id.cancel_call);
        callButton = findViewById(R.id.make_call);

        getAndSetUserProfileInfo();

    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("", "onDataChange: "+dataSnapshot);
                if (dataSnapshot.child(receiverUserId).exists()) {

                    Log.i("", "onDataChange: "+dataSnapshot.child(receiverUserId));
                    receiverUserImage = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImageView);
                }

                if (dataSnapshot.child(senderUserId).exists()) {
                    senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName = dataSnapshot.child(senderUserId).child("name").getValue().toString();

//                    nameContact.setText(receiverUserName);
//                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}