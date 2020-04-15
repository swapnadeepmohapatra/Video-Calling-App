package com.swapnadeep.videocallingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navView;
    private RecyclerView recyclerViewContactList;
    private ImageView findPeopleButton;
    private DatabaseReference contactsRef;
    private DatabaseReference userRef;
    private String userName, profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerViewContactList = findViewById(R.id.contact_list);
        findPeopleButton = findViewById(R.id.find_people_btn);
        recyclerViewContactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findPeopleIntent = new Intent(MainActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_home:
                    Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.navigation_settings:
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case R.id.navigation_notifications:
                    Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
                    startActivity(notificationIntent);
                    break;
                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
            }

            return false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(contactsRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()), Contact.class).build();

        FirebaseRecyclerAdapter<Contact, ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contact contact) {
                final String listUserId = getRef(i).getKey();

                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            userName = dataSnapshot.child("name").getValue().toString();
                            profileImage = dataSnapshot.child("image").getValue().toString();

                            contactsViewHolder.userNameText.setText(userName);
                            Picasso.get().load(profileImage).into(contactsViewHolder.contactImageView);
                        }

                        contactsViewHolder.callButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callingIntent = new Intent(MainActivity.this, CallingActivity.class);
                                callingIntent.putExtra("visit_user_id", listUserId);
                                startActivity(callingIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        recyclerViewContactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText;
        ImageView contactImageView;
        Button callButton;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            contactImageView = itemView.findViewById(R.id.image_contact);
            userNameText = itemView.findViewById(R.id.name_contact);
            callButton = itemView.findViewById(R.id.call_button);
        }
    }

}
