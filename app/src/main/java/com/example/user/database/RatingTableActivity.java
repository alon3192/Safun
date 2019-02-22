package com.example.user.database;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Vector;

import de.hdodenhof.circleimageview.CircleImageView;

public class RatingTableActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    final int USERS_ON_THE_RATING_TABLE = 50;

    TableLayout table_rating;
    TextView my_score;
    DatabaseReference myRef;
    FirebaseDatabase database;
    Vector <User> users;
    private FirebaseAuth mAuth;

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
        setContentView(R.layout.activity_rating_table);





        setNavigationViewListner();
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        table_rating = (TableLayout)findViewById(R.id.table_rating);
        my_score = (TextView)findViewById(R.id.myScore);
        users = new Vector<User>();
        mAuth = FirebaseAuth.getInstance();

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

        myRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i=0;

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot child : children)
                {
                    User user = child.getValue(User.class);
                    users.add(user) ;   /*Vector of all the users*/
                }
                sortByScore();
                String userId = mAuth.getUid();
                showTheTable(userId);
                findMyScore(userId);
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)  ){
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

    private void sortByScore()
    {
        User userTmp;
        for(int i=0 ; i<users.size() ; i++)
        {
            for(int j=i ; j<users.size() ; j++)
            {
                if(users.get(i).getScoring()<users.get(j).getScoring())
                {
                    userTmp = users.get(i);
                    users.set(i,users.get(j));
                    users.set(j, userTmp);
                }
            }
        }
    }

    private void showTheTable(String userId)
    {
        showTitle();
        showData(userId);

    }

    private void showTitle()
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(0,25,0,0);
        row.setLayoutParams(tableRow);


        final TextView photoUser = new TextView(this);
        photoUser.setText("Photo     ");
        photoUser.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        photoUser.setTypeface(null, Typeface.BOLD);
        photoUser.setTextSize(18);


        final TextView rating = new TextView(this);
        rating.setText("Rating     ");
        rating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        rating.setTypeface(null, Typeface.BOLD);
        rating.setTextSize(18);



        final TextView usernameTitle = new TextView(this);
        usernameTitle.setText("Userמame     ");
        usernameTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        usernameTitle.setTypeface(null, Typeface.BOLD);
        usernameTitle.setTextSize(18);


        final TextView scoring = new TextView(this);
        scoring.setText("Scoring     ");
        scoring.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        scoring.setTypeface(null, Typeface.BOLD);
        scoring.setTextSize(18);

        row.addView(rating);
        row.addView(photoUser);
        row.addView(usernameTitle);
        row.addView(scoring);
        table_rating.addView(row);
    }

    private void showData(String userId)
    {
        for(int i=0 ; i<users.size() ; i++)
        {
            if(i==USERS_ON_THE_RATING_TABLE)  /*The top 50 positions will be displayed on the table*/
            {
                break;
            }


           final CircleImageView photo = new CircleImageView(this);
            if(users.get(i).getPhotoString().equals("No photo"))
            {
                photo.setImageResource(R.drawable.default_user_photo);
            }
            else
            {
                Uri myUri = Uri.parse(users.get(i).getPhotoString());
                Picasso.get().load(myUri).resize(100,100).into(photo);
            }

            final TableRow row = new TableRow(this);
            final TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            tableRow.setMargins(0,25,0,0);
            row.setLayoutParams(tableRow);

            if(i%2==0)
                row.setBackgroundColor(Color.parseColor("#EEEEEE"));
            else
                row.setBackgroundColor(Color.parseColor("#d1fde9"));

            final TextView rating = new TextView(this);
            rating.setText("     " + (i+1));
            rating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            rating.setTextColor(Color.parseColor("#A48C19"));



            final TextView name = new TextView(this);
            name.setText(users.get(i).getUserName());
            name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            name.setTextColor(Color.parseColor("#A48C19"));

            final TextView score = new TextView(this);
            score.setText("     " + users.get(i).getScoring());
            score.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            score.setTextColor(Color.parseColor("#A48C19"));

            if(userId.equals(users.get(i).getUserId()))
            {
                rating.setTextColor(Color.parseColor("#FF0000"));
                rating.setTypeface(null, Typeface.BOLD);
                name.setTextColor(Color.parseColor("#FF0000"));
                name.setTypeface(null, Typeface.BOLD);
                score.setTextColor(Color.parseColor("#FF0000"));
                score.setTypeface(null, Typeface.BOLD);
            }

            row.addView(rating);
            row.addView(photo);
            row.addView(name);
            row.addView(score);
            table_rating.addView(row);
        }
    }

    private void findMyScore(final String userId)   /*Each user is highlighted in the table*/
    {
        final int score;
        myRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            User myUser;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot child : children)
                {
                    if(child.getKey().equals(userId))
                    {
                        myUser = child.getValue(User.class);
                        presentMyScore(myUser.getScoring());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void presentMyScore(int myScore)  /*Below the table will be the user's score*/
    {
       my_score.setText("My Score : " + myScore);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
