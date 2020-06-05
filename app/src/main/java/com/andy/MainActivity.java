package com.andy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    Button logoutBtn;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    GoogleSignInClient mGoogleSignInClient;
    String uID;
    ArrayList<String> tags;
    FirebaseDatabase firebaseDB, childrenDB;
    DatabaseReference current_userdb;
    Iterable<DataSnapshot> allTags;
    Set<String> tagSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loading the default fragment
        loadFragment(new FeedsFragment());

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        doThis();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.feeds:
                fragment = new FeedsFragment();
                break;

            case R.id.search:
                fragment = new SearchFragment();
                break;

            case R.id.profile:
                fragment = new ProfileFragment();
                break;
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void updatetagSet(Iterable<DataSnapshot> allTags) {
        for (DataSnapshot tag : allTags) {
            if (!tagSet.contains(tag.toString()))
                tagSet.add(tag.getValue().toString());
        }
        for (String tags : tagSet) {
            Log.d("TAGS", tags);
        }
    }

    public void notification(String tagName) {
        Toast.makeText(MainActivity.this, "notification init", Toast.LENGTH_LONG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "n")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setContentText("New Document added to " + tagName)
                .setContentTitle("ANDy - New content Alert!!")
                ;
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999, builder.build());

    }

    public void doThis() {
        //logoutBtn = findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();

        firebaseDB = FirebaseDatabase.getInstance();
        reference = firebaseDB.getReference();
        tagSet = new HashSet<>();

        if (mAuth.getCurrentUser().getUid() != null) {
            uID = mAuth.getCurrentUser().getUid();
            Log.d("UID", uID);
        } else {
            try {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                uID = acct.getId();
                Log.d("UID", uID);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }


        //tags in profile

        reference.child("Profile").child(uID).child("tags")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        allTags = dataSnapshot.getChildren();
                        //Log.d("TAGS",allTags.toString());
                        updatetagSet(allTags);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        //get topic change
        reference.child("Topics")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Toast.makeText(MainActivity.this, "Child added in" + dataSnapshot.child("topicid").getValue().toString(), Toast.LENGTH_SHORT).show();

                        Log.d("TAGS", tagSet.toString());
                        Log.d("TAGS", dataSnapshot.child("topicid").getValue().toString());
                        if (tagSet.contains(dataSnapshot.child("topicid").getValue().toString())) {
                            Toast.makeText(MainActivity.this, "Document added to " + dataSnapshot.child("topicid").getValue().toString(), Toast.LENGTH_LONG).show();
                            notification(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        Toast.makeText(MainActivity.this, "Child removed " + dataSnapshot.getKey(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
}
