package com.swapnadeep.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.Objects;

import javax.crypto.interfaces.PBEKey;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChattingActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "46722892";
    private static String SESSION_ID = "1_MX40NjcyMjg5Mn5-MTU4ODc3ODEyODQ2OX45Uk9qYVBFcWl0TjNBb09ZYnZ6RXczbDh-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjcyMjg5MiZzaWc9OGU1ZDQ1YzNjNTMxNmQ0ZTNlNWFiNjFkOWJjZmJjOWQwNmIxN2JkZTpzZXNzaW9uX2lkPTFfTVg0ME5qY3lNamc1TW41LU1UVTRPRGMzT0RFeU9EUTJPWDQ1VWs5cVlWQkZjV2wwVGpOQmIwOVpZblo2UlhjemJEaC1mZyZjcmVhdGVfdGltZT0xNTg4Nzc4MTg2Jm5vbmNlPTAuMzE0Mjk1MzEwMTYyNjYyJnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE1OTEzNzAxODYmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String TAG = VideoChattingActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERMISSION = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView cancel_video_chat_button;
    private DatabaseReference userRef;
    private String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chatting);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        cancel_video_chat_button = findViewById(R.id.close_video_chat_btn);

        cancel_video_chat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing")) {
                            userRef.child(userID).child("Ringing").removeValue();

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChattingActivity.this, MainActivity.class));
                            finish();
                        }

                        if (dataSnapshot.child(userID).hasChild("Calling")) {
                            userRef.child(userID).child("Calling").removeValue();

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChattingActivity.this, MainActivity.class));
                            finish();
                        } else {

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChattingActivity.this, MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChattingActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERMISSION)
    private void requestPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.BLUETOOTH, Manifest.permission.BROADCAST_STICKY, Manifest.permission.READ_PHONE_STATE};

        if (EasyPermissions.hasPermissions(this, perms)) {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

//            1. Init & Connect to the session

            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChattingActivity.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "Please grant these permissions", RC_VIDEO_APP_PERMISSION, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //    2. Publish the stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(TAG, "onConnected: " + session);

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChattingActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TAG, "onDisconnected: ");
    }

    //    3. Subscribing to the stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TAG, "onStreamReceived: " + stream);

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TAG, "onStreamDropped: ");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(TAG, "onError: " + opentokError);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
