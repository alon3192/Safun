package com.example.user.database;

import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ImageView image;
    ImageButton back;
    FirebaseStorage storage;
    StorageReference storageRef;
    private FirebaseAuth mAuth;
    DatabaseReference myRef;
    FirebaseDatabase database;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView nav;
    private View headerView;
    private TextView email;
    private TextView username;
    private ImageView userPhoto;

    private CountDownTimer CDT;
    private ProgressBar progressBar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);



        setNavigationViewListner();
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);





        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


        nav = (NavigationView)findViewById(R.id.nav1);
        headerView = nav.getHeaderView(0);
        email = (TextView)headerView.findViewById(R.id.email);
        username = (TextView)headerView.findViewById(R.id.username) ;
        userPhoto = (ImageView)headerView.findViewById(R.id.userPhoto);
        FirebaseUser user = mAuth.getCurrentUser();
        email.setText(user.getEmail());
        findName();             /*Display the name of the username on the sidebar*/

        Menu nav_Menu = nav.getMenu();
        nav_Menu.findItem(R.id.itemMap).setVisible(false);  /*The option to sort the map is not relevant to this page and therefore is not displayed*/


        image = (ImageView) findViewById(R.id.image);
        back = (ImageButton)findViewById(R.id.back);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Intent i = getIntent();
        String imagePath = i.getStringExtra("imagePath");

        progressBar = findViewById(R.id.progress);

        if(!imagePath.equals("No image"))   /*The address of the sent image*/
        {
            storageRef.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(image);
                }
            });


            CDT = new CountDownTimer(5000, 1000)  /*Set 5 seconds to the progressbar*/
            {
                public void onTick(long millisUntilFinished) {}

                public void onFinish()
                {
                    progressBar.setVisibility(View.GONE);
                }
            }.start();


        }
        else   /*If the user chose not to upload a picture, a default image is displayed*/
        {
            image.setImageResource(R.drawable.no_image);
            progressBar.setVisibility(View.GONE);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
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
                if(mAuth.getCurrentUser().getDisplayName()!=null)
                    startActivity(intent);
                break;
            }

            case R.id.settings_btn: {
                Intent intent = new Intent(this, SettingsActivity.class);
                if(mAuth.getCurrentUser().getDisplayName()!=null)
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
                        if(mAuth.getCurrentUser().getDisplayName()!=null && !mAuth.getCurrentUser().getDisplayName().equals(""))
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
}
