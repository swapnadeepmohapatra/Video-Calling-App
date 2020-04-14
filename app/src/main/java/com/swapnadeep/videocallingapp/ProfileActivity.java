package com.swapnadeep.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID = "";
    private String receiverUserImage = "";
    private String receiverUserName = "";
    private String currentState = "new";

    private ImageView background_profile_view;
    private TextView name_profile;
    private Button addFriend, cancelFriend;

    private DatabaseReference friendRequestRef, contacts;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contacts = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();
        receiverUserImage = Objects.requireNonNull(getIntent().getExtras().get("profile_image")).toString();
        receiverUserName = Objects.requireNonNull(getIntent().getExtras().get("profile_name")).toString();

        background_profile_view = findViewById(R.id.background_profile_view);
        name_profile = findViewById(R.id.name_profile);
        addFriend = findViewById(R.id.add_friend);
        cancelFriend = findViewById(R.id.request_cancel_button);

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);

        manageClickEvents();
    }


    private void manageClickEvents() {

        friendRequestRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)) {
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (requestType.equals("sent")) {
                        currentState = "request_sent";
                        addFriend.setText("Cancel Friend Request");
                    } else if (requestType.equals("received")) {
                        currentState = "request_received";
                        addFriend.setText("Accept Friend Request");

                        cancelFriend.setVisibility(View.VISIBLE);
                        cancelFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest();
                            }
                        });
                    }
                } else {
                    contacts.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)) {
                                currentState = "friends";
                                addFriend.setText("Delete Contact");
                            } else {
                                currentState = "new";
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (Objects.equals(receiverUserID, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            addFriend.setVisibility(View.GONE);
        } else {
            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentState.equals("new")) {
                        sendFriendRequest();
                    } else if (currentState.equals("request_sent")) {
                        cancelFriendRequest();
                    } else if (currentState.equals("request_received")) {
                        acceptFriendRequest();
                    } else if (currentState.equals("request_sent")) {
                        cancelFriendRequest();
                    }
                }
            });
        }
    }

    private void acceptFriendRequest() {
        contacts.child(currentUserID).child(receiverUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contacts.child(receiverUserID).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendRequestRef.child(currentUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            friendRequestRef.child(receiverUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        addFriend.setText("Delete Contact");
                                                        currentState = "friends";
                                                        cancelFriend.setVisibility(View.GONE);
                                                        Toast.makeText(ProfileActivity.this, "Request Accepted", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(currentUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendRequestRef.child(receiverUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addFriend.setText("Add Friend");
                                currentState = "new";
                                Toast.makeText(ProfileActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(currentUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendRequestRef.child(receiverUserID).child(currentUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                currentState = "request_sent";
                                addFriend.setText("Cancel Friend Request");
                                Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
