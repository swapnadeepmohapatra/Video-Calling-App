package com.swapnadeep.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {

    private RecyclerView findFriendsList;
    private EditText searchFriendsEditText;
    private String string = "";
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        findFriendsList = findViewById(R.id.find_friends_list);
        searchFriendsEditText = findViewById(R.id.search_user_text);
        findFriendsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchFriendsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (searchFriendsEditText.getText().toString().equals("")) {
                    searchFriendsEditText.setError("Please enter something");
                } else {
                    string = charSequence.toString();
                    onStart();

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = null;
        if (string.equals("")) {
            options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(usersRef, Contact.class).build();
        } else {
            options = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(usersRef.orderByChild("name").startAt(string).endAt(string + "\uf8ff"), Contact.class).build();
        }

        FirebaseRecyclerAdapter<Contact, FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, final int i, @NonNull final Contact contact) {
                findFriendsViewHolder.userNameText.setText(contact.getName());
                Picasso.get().load(contact.getImage()).into(findFriendsViewHolder.profileImageView);

                findFriendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(i).getKey();

                        Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class);
                        intent.putExtra("visit_user_id", visit_user_id);
                        intent.putExtra("profile_image", contact.getImage());
                        intent.putExtra("profile_name", contact.getName());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);

                return viewHolder;
            }
        };

        findFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText;
        Button videoCallBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameText = itemView.findViewById(R.id.name_contact);
            videoCallBtn = itemView.findViewById(R.id.call_button);
            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.cardview1);

            videoCallBtn.setVisibility(View.GONE);
        }
    }

}
