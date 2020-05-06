package com.swapnadeep.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImageView;
    private ImageView cancelButton, callButton;
    private String receiverUserId = "", receiverUserImage = "", receiverUserName = "";
    private String senderUserId = "", senderUserImage = "", senderUserName = "", checker = "notClicked", callingID = "", ringingID = "";
    private DatabaseReference userRef;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        receiverUserId = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mediaPlayer = MediaPlayer.create(this, R.raw.ringing);

        nameContact = findViewById(R.id.name_calling);
        profileImageView = findViewById(R.id.profile_image_calling);
        cancelButton = findViewById(R.id.cancel_call);
        callButton = findViewById(R.id.make_call);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                checker = "clicked";

                Log.i("TAG ", "onDataChange: " + checker);
                cancelCallingUser();

                checker = "clicked";

                Log.i("TAG ", "onDataChange: " + checker);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();

                final HashMap<String, Object> callingPickupMap = new HashMap<>();
                callingPickupMap.put("picked","picked");

                userRef.child(senderUserId).child("Ringing").updateChildren(callingPickupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()){
                            Intent intent = new Intent(CallingActivity.this,VideoChattingActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        getAndSetUserProfileInfo();

    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("", "onDataChange: " + dataSnapshot);
                if (dataSnapshot.child(receiverUserId).exists()) {

                    Log.i("", "onDataChange: " + dataSnapshot.child(receiverUserId));
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

    @Override
    protected void onStart() {
        super.onStart();

        mediaPlayer.start();

        if (checker.equals("notClicked")) {
            userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i("TAG ", "onDataChange: " + checker);
                    if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")) {

                        final HashMap<String, Object> callingInfo = new HashMap<>();
                        callingInfo.put("calling", receiverUserId);

                        userRef.child(senderUserId).child("Calling").updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    final HashMap<String, Object> ringingInfo = new HashMap<>();
                                    ringingInfo.put("ringing", senderUserId);

                                    userRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling")) {
                    callButton.setVisibility(View.VISIBLE);
                }
                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")){
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this,VideoChattingActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling") && !dataSnapshot.child(receiverUserId).hasChild("Ringing") && !dataSnapshot.child(receiverUserId).hasChild("Calling")) {
                    startActivity(new Intent(CallingActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void cancelCallingUser() {

//        FIXME: FROM SENDER

        userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userRef.child(receiverUserId).child("Ringing").removeValue();
                }
            }
        });


        userRef.child(receiverUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userRef.child(senderUserId).child("Ringing").removeValue();
                }
            }
        });

        startActivity(new Intent(CallingActivity.this, LoginActivity.class));
        finish();

//        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")) {
//                    callingID = Objects.requireNonNull(dataSnapshot.child("calling").getValue()).toString();
//
//                    userRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
////                                        TODO: LoginActivity.class
//                                        startActivity(new Intent(CallingActivity.this, LoginActivity.class));
//                                        finish();
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } else {
//                    startActivity(new Intent(CallingActivity.this, LoginActivity.class));
//                    finish();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
////        FIXME: FROM RECEIVER
//
//        userRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")) {
//                    ringingID = Objects.requireNonNull(dataSnapshot.child("ringing").getValue()).toString();
//
//                    userRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                userRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
////                                        TODO: LoginActivity.class
//                                        startActivity(new Intent(CallingActivity.this, LoginActivity.class));
//                                        finish();
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } else {
//                    startActivity(new Intent(CallingActivity.this, LoginActivity.class));
//                    finish();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

    }

}