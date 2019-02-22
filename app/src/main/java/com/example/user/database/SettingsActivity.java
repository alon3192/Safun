package com.example.user.database;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DatabaseReference myRef;
    FirebaseDatabase database;
    private FirebaseAuth mAuth;

    Button edit_name;
    EditText edit_first_name;
    EditText edit_last_name;
    private ImageView checked;
    private Switch notification;
    private TextView notificationText;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private NavigationView nav;
    private View headerView;
    private TextView email;
    private TextView username;
    private ImageView userPhoto;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        setNavigationViewListner();
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();


        edit_name = (Button)findViewById(R.id.edit_name);
        edit_first_name = (EditText)findViewById(R.id.edit_first_name);
        edit_last_name = (EditText) findViewById(R.id.edit_last_name);
        checked = (ImageView) findViewById(R.id.checked);
        notification = (Switch)findViewById(R.id.notification);
        notificationText = (TextView)findViewById(R.id.notificationText);

        final String userId = mAuth.getUid();

        nav = (NavigationView)findViewById(R.id.nav1);
        headerView = nav.getHeaderView(0);
        email = (TextView)headerView.findViewById(R.id.email);
        username = (TextView)headerView.findViewById(R.id.username) ;
        userPhoto = (ImageView)headerView.findViewById(R.id.userPhoto);
        FirebaseUser user = mAuth.getCurrentUser();
        email.setText(user.getEmail());
        findName();             /*Display the name of the username on the sidebar*/

        Menu nav_Menu = nav.getMenu();
        nav_Menu.findItem(R.id.itemMap).setVisible(false);

        myRef.child("user").addValueEventListener(new ValueEventListener() {  /*Displays the name First and last name in case the user wants to change*/
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if(child.getKey().equals(userId))
                    {
                        User user = child.getValue(User.class);
                        String name[] = new String[2];
                        name = user.getUserName().split(" ");
                        edit_first_name.setText(name[0]);
                        edit_last_name.setText(name[1]);

                        notification.setTag("TAG");   /*To avoid running the method 'setOnCheckedChangeListener' on the notification Switch*/
                        if(user.isNotification())
                        {
                            notification.setChecked(true);
                            notificationText.setText("Notification service is working");
                        }
                        else
                        {
                            notificationText.setText("Notification service is not working");
                        }
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {   /*Add a place to the list of favorite places*/


                if (notification.getTag() != null) {
                    notification.setTag(null);
                    return;
                }

                if(isChecked)
                {
                    Map <String, Object> data = new HashMap<>();
                    data.put("notification", true);
                    myRef.child("user").child(userId).updateChildren(data);
                }
                else
                {
                    Map <String, Object> data = new HashMap<>();
                    data.put("notification", false);
                    myRef.child("user").child(userId).updateChildren(data);
                }
            }
        });




        notification.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                notification.setTag(null);
                return false;
            }
        });

        setNotificationSwitchState();


        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   /*Change the details and save them in the database*/

                myRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    User user;
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                        for(DataSnapshot child : children)
                        {
                            if(child.getKey().equals(userId))
                            {
                                user = child.getValue(User.class);
                                user.setUserName(edit_first_name.getText().toString() + " " + edit_last_name.getText().toString());
                                myRef.child("user").child(userId).setValue(user);


                                AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
                                animation1.setDuration(1000);
                                animation1.setFillAfter(true);
                                checked.startAnimation(animation1);
                                checked.setVisibility(View.VISIBLE);


                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
        {
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setNavigationViewListner() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav1);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.map_btn: {
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.rating_table_btn: {
                Intent intent = new Intent(this, RatingTableActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.favorite_places_btn: {
                Intent intent = new Intent(this, favoritePlacesActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.settings_btn: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }



        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void findName()  /*A user-specific*/
    {
        final String userId = mAuth.getUid();

        myRef.child("user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if(child.getKey().equals(userId))
                    {
                        User user = child.getValue(User.class);
                        username.setText(user.getUserName());
                        if(mAuth.getCurrentUser().getDisplayName()!=null)
                        {
                            Uri myUri = Uri.parse(user.getPhotoString());
                            Picasso.get().load(myUri).into(userPhoto);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setNotificationSwitchState()
    {

    }

}
