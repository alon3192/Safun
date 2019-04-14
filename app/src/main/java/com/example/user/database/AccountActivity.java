package com.example.user.database;


import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    private CardView signOut;
    private CardView ratingTable;
    private CardView settings;
    private CardView map;
    private CardView favoritePlaces;

    DatabaseReference myRef;
    FirebaseDatabase database;
    List<User> users;

    CollapsingToolbarLayout collapsingToolbarLayout;


    String photoString="No photo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        startService(new Intent(AccountActivity.this, notificationService.class));



        mAuth = FirebaseAuth.getInstance();


        signOut = (CardView) findViewById(R.id.logout);
        ratingTable = (CardView) findViewById(R.id.rating);
        settings = (CardView)findViewById(R.id.settings);
        map = (CardView) findViewById(R.id.map);
        favoritePlaces = (CardView)findViewById(R.id.favorite);



        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        users = new ArrayList<User>();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collaps);




        if(mAuth.getCurrentUser().getDisplayName()!=null && !mAuth.getCurrentUser().getDisplayName().equals("") )
        {
            Intent i = getIntent();
            photoString = i.getStringExtra("personPhoto");
        }



        myRef.child("user").addValueEventListener(new ValueEventListener() {   /*A new user is registered in the database*/
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    User user = child.getValue(User.class);
                    users.add(user);
                }

                if (!findUser())   /*If the user is not found, this is a new user and must be registered*/
                    addUser();
                showName();   /*A user-specific message*/
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);




        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mAuth.getCurrentUser().getDisplayName()!=null && !mAuth.getCurrentUser().getDisplayName().equals(""))
                {
                    String userId = mAuth.getUid();
                    Map<String, Object> data = new HashMap<>();
                    data.put("online", false);
                    myRef.child("user").child(userId).updateChildren(data);

                    mGoogleSignInClient.signOut();
                    System.exit(0);


                }
                else
                {
                    String userId = mAuth.getUid();
                    Map<String, Object> data = new HashMap<>();
                    data.put("online", false);
                    myRef.child("user").child(userId).updateChildren(data);


                    mAuth.signOut();
                    Intent intent = new Intent(AccountActivity.this, RegistrationActivity.class);
                    intent.putExtra("logout", true);
                    startActivity(intent);
                    System.exit(0);
                }
            }
        });



        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        favoritePlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, favoritePlacesActivity.class);
                startActivity(intent);
            }
        });

        ratingTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, RatingTableActivity.class);
                startActivity(intent);

            }
        });


    }




    private void showName()  /*A user-specific message*/
    {
        String name = "";
        String userId = mAuth.getUid();
        for (User tmpUser : users) {
            if (userId.equals(tmpUser.getUserId()))
            {
                name = tmpUser.getUserName();
                break;
            }
        }
        collapsingToolbarLayout.setTitle("Hi " + name);

    }



    private boolean findUser()  /*Check if a logged-on user is already registered in the database*/
    {

        FirebaseUser user = mAuth.getCurrentUser();
        String userId = mAuth.getUid();



        for (User tmpUser : users) {
            if (userId.equals(tmpUser.getUserId())) {

                Map<String, Object> data = new HashMap<>();
                data.put("online", true);
                myRef.child("user").child(userId).updateChildren(data);

                return true;
            }
        }
        return false;

    }

    private void addUser()  /*In case of registration from Google Account*/
    {
        String userId = mAuth.getUid();
        if(mAuth.getCurrentUser().getDisplayName()!=null && !mAuth.getCurrentUser().getDisplayName().equals(""))  /*In case of registration not from Google Account*/
        {
            FirebaseUser user = mAuth.getCurrentUser();
            String userName = user.getDisplayName();
            User newUser = User.getInstance(userId, userName, photoString);
            myRef.child("user").child(userId).setValue(newUser);
        }

        else   /*If the user is not registered the function registers it in the database*/
        {
            Intent i = getIntent();
            String firstName = i.getStringExtra("firstName");
            String lastName = i.getStringExtra("lastName");


            User newUser = User.getInstance(userId, firstName + " " + lastName, photoString);
            myRef.child("user").child(userId).setValue(newUser);
        }

    }

}

