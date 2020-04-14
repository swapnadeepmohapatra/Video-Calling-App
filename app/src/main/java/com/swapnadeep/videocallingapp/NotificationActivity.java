package com.swapnadeep.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class NotificationActivity extends AppCompatActivity {

    RecyclerView notificationList;
    private DatabaseReference friendRequestRef, contacts, userRef;

    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contacts = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        notificationList = findViewById(R.id.notification_list);
        notificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(friendRequestRef.child(currentUserID), Contact.class).build();

        FirebaseRecyclerAdapter<Contact, NotificationViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, NotificationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationViewHolder notificationViewHolder, int i, @NonNull Contact contact) {
                notificationViewHolder.acceptBtn.setVisibility(View.VISIBLE);
                notificationViewHolder.cancelBtn.setVisibility(View.VISIBLE);

                final String listUserId = getRef(i).getKey();

                DatabaseReference requestTypeRef = getRef(i).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String type = Objects.requireNonNull(dataSnapshot.getValue()).toString();

                            if (type.equals("received")) {
                                notificationViewHolder.cardView.setVisibility(View.VISIBLE);

                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            final String imgLnk = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(imgLnk).into(notificationViewHolder.profileImageView);
                                        }

                                        final String nameStr = dataSnapshot.child("name").getValue().toString();
                                        notificationViewHolder.userNameTExt.setText(nameStr);

                                        notificationViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                acceptFriendRequest(listUserId);
                                            }
                                        });

                                        notificationViewHolder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                cancelFriendRequest(listUserId);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                notificationViewHolder.cardView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_item, parent, false);
                NotificationViewHolder viewHolder = new NotificationViewHolder(view);
                return viewHolder;
            }
        };

        notificationList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTExt;
        Button acceptBtn, cancelBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameTExt = itemView.findViewById(R.id.name_notification);
            acceptBtn = itemView.findViewById(R.id.request_accept_button);
            cancelBtn = itemView.findViewById(R.id.request_cancel_button);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardView = itemView.findViewById(R.id.cardview);
        }
    }

    private void acceptFriendRequest(final String receiverUserID) {
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
                                                        Toast.makeText(NotificationActivity.this, "New Contact Saved", Toast.LENGTH_SHORT).show();
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


    private void cancelFriendRequest(final String receiverUserID) {
        friendRequestRef.child(currentUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendRequestRef.child(receiverUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(NotificationActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

}
