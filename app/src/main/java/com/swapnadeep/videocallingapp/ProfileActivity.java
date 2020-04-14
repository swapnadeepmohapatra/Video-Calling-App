package com.swapnadeep.videocallingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID = "";
    private String receiverUserImage = "";
    private String receiverUserName = "";

    private ImageView background_profile_view;
    private TextView name_profile;
    private Button addFriend, cancelFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();
        receiverUserImage = Objects.requireNonNull(getIntent().getExtras().get("profile_image")).toString();
        receiverUserName = Objects.requireNonNull(getIntent().getExtras().get("profile_name")).toString();

        background_profile_view = findViewById(R.id.background_profile_view);
        name_profile = findViewById(R.id.name_profile);
        addFriend = findViewById(R.id.add_friend);
        cancelFriend = findViewById(R.id.request_cancel_button);

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);
    }
}
