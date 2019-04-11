package com.example.user.database;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.security.AccessController.getContext;

public class ReportActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    List<User> users;
    String userName="";
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    private StorageReference mStorageRef;

    DatabaseReference myRef;
    FirebaseDatabase database;

    String checkMusic ="No music";

    private Button send;
    private Switch parking;
    private RadioButton radioButton;
    private EditText summary;

    private Switch music;
    private RadioGroup radioMusic;
    private RadioButton israeli;
    private RadioButton hipHop;
    private RadioButton mainstram;
    private RadioButton other;
    private EditText musicText;
    private ImageButton musicSend;







    private SmileRating vibe, prices, crowding;



    private ImageView addImage;
    FirebaseStorage storage;
    StorageReference storageRef;
    Integer REQUEST_CAMERA=0;
    boolean imageFlag=false;

    private ImageView bigImage;

    String placeNameString="";

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
        setContentView(R.layout.activity_report);


        setNavigationViewListner();
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent i = getIntent();
        placeNameString = i.getStringExtra("placeName");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("report_" + placeNameString);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        nav = (NavigationView)findViewById(R.id.nav1);
        headerView = nav.getHeaderView(0);
        email = (TextView)headerView.findViewById(R.id.email);
        username = (TextView)headerView.findViewById(R.id.username) ;
        userPhoto = (ImageView)headerView.findViewById(R.id.userPhoto);
        FirebaseUser user = mAuth.getCurrentUser();
        email.setText(user.getEmail());
        findName();             /*Display the name of the username*/

        Menu nav_Menu = nav.getMenu();
        nav_Menu.findItem(R.id.itemMap).setVisible(false);



        send = (Button) findViewById(R.id.send);
        parking = (Switch)findViewById(R.id.parking);
        music = (Switch) findViewById(R.id.music);
        summary = (EditText)findViewById(R.id.summary);

        vibe = (SmileRating)findViewById(R.id.vibe);
        vibe.setSelectedSmile(BaseRating.OKAY);


        prices = (SmileRating) findViewById(R.id.prices);
        prices.setSelectedSmile(BaseRating.OKAY);

        crowding = (SmileRating) findViewById(R.id.crowding);
        crowding.setSelectedSmile(BaseRating.OKAY);

        crowding.setNameForSmile(BaseRating.TERRIBLE, "v Crowded");
        crowding.setNameForSmile(BaseRating.BAD, "Crowded");
        crowding.setNameForSmile(BaseRating.OKAY, "Average");
        crowding.setNameForSmile(BaseRating.GOOD, "Spacious");
        crowding.setNameForSmile(BaseRating.GREAT, "v Spacious");



        radioMusic = (RadioGroup)findViewById(R.id.radioMusic);
        israeli = (RadioButton)findViewById(R.id.israeli);
        hipHop = (RadioButton)findViewById(R.id.hipHop);
        mainstram = (RadioButton)findViewById(R.id.mainstream);
        other = (RadioButton)findViewById(R.id.other);
        musicText = (EditText)findViewById(R.id.musicText);
        musicSend = (ImageButton)findViewById(R.id.musicSend);


        addImage = (ImageView)findViewById(R.id.image);

        bigImage = (ImageView)findViewById(R.id.imageView);





        music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    music.setText("Choose :");
                    radioMusic.setVisibility(View.VISIBLE);
                }
                else
                {
                    music.setText("No music");
                    if(radioMusic.getVisibility()==View.VISIBLE)
                    {
                        TranslateAnimation animate = new TranslateAnimation(0, radioMusic.getWidth() * 2, 0, 0);
                        animate.setDuration(500);
                        animate.setFillAfter(false);
                        radioMusic.startAnimation(animate);

                        animate = new TranslateAnimation(0, musicText.getWidth() * -4, 0, 0);
                        animate.setDuration(500);
                        animate.setFillAfter(false);
                        musicText.startAnimation(animate);

                        animate = new TranslateAnimation(0, 0, 0, musicSend.getHeight() * -30);
                        animate.setDuration(500);
                        animate.setFillAfter(false);
                        musicSend.startAnimation(animate);
                        radioMusic.setVisibility(View.GONE);
                        musicText.setVisibility(View.GONE);
                        musicSend.setVisibility(View.GONE);
                    }

                }
                addImage.setVisibility(View.VISIBLE);

            }
        });

        israeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TranslateAnimation animate = new TranslateAnimation(0,radioMusic.getWidth()*2,0,0);
                animate.setDuration(500);
                animate.setFillAfter(false);
                radioMusic.startAnimation(animate);

                radioMusic.setVisibility(View.GONE);
                musicText.setVisibility(View.GONE);
                musicSend.setVisibility(View.GONE);
                addImage.setVisibility(View.VISIBLE);
                checkMusic = "Israeli";
                music.setText(checkMusic);
            }
        });

        hipHop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TranslateAnimation animate = new TranslateAnimation(0,radioMusic.getWidth()*-2,0,0);
                animate.setDuration(500);
                animate.setFillAfter(false);
                radioMusic.startAnimation(animate);

                radioMusic.setVisibility(View.GONE);
                musicText.setVisibility(View.GONE);
                musicSend.setVisibility(View.GONE);
                addImage.setVisibility(View.VISIBLE);
                checkMusic = "Hip Hop";
                music.setText(checkMusic);
            }
        });

        mainstram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TranslateAnimation animate = new TranslateAnimation(0,0,0,radioMusic.getHeight()*4);
                animate.setDuration(500);
                animate.setFillAfter(false);
                radioMusic.startAnimation(animate);

                radioMusic.setVisibility(View.GONE);
                musicText.setVisibility(View.GONE);
                musicSend.setVisibility(View.GONE);
                addImage.setVisibility(View.VISIBLE);
                checkMusic = "Mainstream";
                music.setText(checkMusic);
            }
        });

        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicText.setVisibility(View.VISIBLE);
                musicSend.setVisibility(View.VISIBLE);
                addImage.setVisibility(View.GONE);
                checkMusic = "Other";
            }
        });

        musicSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                music.setText(musicText.getText().toString());

                TranslateAnimation animate = new TranslateAnimation(0,radioMusic.getWidth()*2,0,0);
                animate.setDuration(500);
                animate.setFillAfter(false);
                radioMusic.startAnimation(animate);

                animate = new TranslateAnimation(0,musicText.getWidth()*-4,0,0);
                animate.setDuration(500);
                animate.setFillAfter(false);
                musicText.startAnimation(animate);

                animate = new TranslateAnimation(0,0,0,musicSend.getHeight()*-30);
                animate.setDuration(500);
                animate.setFillAfter(false);
                musicSend.startAnimation(animate);

                radioMusic.setVisibility(View.GONE);
                musicText.setVisibility(View.GONE);
                musicSend.setVisibility(View.GONE);
                addImage.setVisibility(View.VISIBLE);
            }
        });



        parking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    parking.setText("There is parking");  //To change the text near to switch
                    Log.d("You are :", "Checked");
                }
                else {
                    parking.setText("No parking");  //To change the text near to switch
                    Log.d("You are :", " Not Checked");
                }
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               int vibeCheck =  vibe.getRating();
               int pricesCheck =  prices.getRating();
               int crowdingCheck = crowding.getRating();


              boolean parkingTmp =  parking.isChecked();
              int parkingCheck = parkingGrade(parkingTmp);   /*The score for the "parking" parameter is converted to a number*/


                if(checkMusic.equals("Other"))
                {
                    checkMusic = musicText.getText().toString();   /*The type of music the user has written will be displayed next to the switch*/
                }

                String summaryCheck = summary.getText().toString();

                String imagePath="No image";
                if(imageFlag)  /*If a picture is uploaded with the report, it loads and returns its path*/
                {
                    imagePath = uploadImage();
                }


                    Intent i = getIntent();
                    userName = i.getStringExtra("userName");

                String key = databaseReference.push().getKey();


                Report report = new Report(key, userName, vibeCheck, pricesCheck, crowdingCheck, checkMusic, parkingCheck, summaryCheck, imagePath);


                databaseReference.child(key).setValue(report);   /*After you submit the report, a Report object is created. It is saved in the database*/

                uploadNewReport();  /*Upload a report to the common object of all places*/

                String userId = mAuth.getUid();


                updateScoring(userId);  /*The reported user score is updated*/

                Toast.makeText(ReportActivity.this, "Your report has been submitted", Toast.LENGTH_LONG).show();
                onBackPressed();

            }
        });




        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   /*If the user chose to upload an image*/
                add_image();
                imageFlag=true;

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

        myRef.child("user").addValueEventListener(new ValueEventListener() {   /*A new user is registered in the database*/
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

        static final int MY_PERMISSIONS_REQUEST_CAMERA=1;
    private void add_image()   /*Activate the device camera*/

    {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    this, Manifest.permission.CAMERA)) {


            } else {
                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }

        }
        else
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent)
    {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if(requestCode == REQUEST_CAMERA)
        {
            if(resultCode == RESULT_OK){
                Bundle bundle = imageReturnedIntent.getExtras();
                final Bitmap bitmap = (Bitmap) bundle.get("data");
                addImage.setImageBitmap(bitmap);
                bigImage.setImageBitmap(bitmap);
            }
        }
    }

    private String uploadImage()   /*If a picture is uploaded with the report, it loads and returns its path*/
    {
        bigImage.setDrawingCacheEnabled(true);
        bigImage.buildDrawingCache();
        Bitmap bitmap = bigImage.getDrawingCache();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        bigImage.setDrawingCacheEnabled(false);
        byte[] data = byteArrayOutputStream.toByteArray();

        String path = "reportsImages/" + UUID.randomUUID() + ".png";
        StorageReference firememesRef = storage.getReference(path);

        UploadTask uploadTask = firememesRef.putBytes(data);


        uploadTask.addOnSuccessListener(ReportActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ReportActivity.this, "You'r picture has been uploaded" , Toast.LENGTH_SHORT).show();
            }
        });

        uploadTask.addOnFailureListener(ReportActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ReportActivity.this, "The upload has failed" , Toast.LENGTH_SHORT).show();
            }
        });
        return path;
    }


    public int crowdingGrade (String crowding)
    {
        int grade = 0;
        switch(crowding)
        {
            case "Empty" : grade=5;
            break;
            case "Spacious" : grade=4;
            break;
            case "Medium" : grade=3;
            break;
            case "Crowded" : grade=2;
            break;
            case "Full" : grade=1;
            break;
        }
        return grade;
    }


    public int parkingGrade(boolean parking)
    {
       if(parking) return 5;
        else return 1;
    }

    private void updateScoring(final String userId)  /*The reported user score is updated*/
    {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();



        databaseReference.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            User user;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for(DataSnapshot child : children)
                {
                    if(child.getKey().equals(userId))
                    {
                        user = child.getValue(User.class);
                        user.setScoring(user.getScoring() + 2);
                        databaseReference.child("user").child(userId).setValue(user);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void uploadNewReport()  /*Upload a report to the common object of all places*/
    {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        String key = databaseReference.push().getKey();
        RecentlyUpload toUpload = new RecentlyUpload(placeNameString, mAuth.getUid());
        databaseReference.child("recentlyUpload").child(key).setValue(toUpload);




    }





}
