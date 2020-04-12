package com.swapnadeep.videocallingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button saveButton;
    private EditText userName, bioText;
    private ImageView imageViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveButton = findViewById(R.id.save_button);
        userName = findViewById(R.id.username_settings);
        bioText = findViewById(R.id.bio_settings);
        imageViewProfile = findViewById(R.id.settings_profile_image);

    }
}
