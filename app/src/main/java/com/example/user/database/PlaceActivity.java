package com.example.user.database;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    TextView name;
    TextView address;
    TextView telephone;
    TextView type;
    ImageView imagePlace;
    Button add_report;


    String placeNameString;
    String userName="";
    String placeAddress="";
    String placeTelephone="";
    String placeType="";

    FirebaseStorage storage;
    StorageReference storageRef;

    DatabaseReference myRef;
    FirebaseDatabase database;
    List<Report> reports;
    List<User> users;
    private FirebaseAuth mAuth;

    TableLayout table_reports;
    Switch favoritePlaces;
    CompoundButton.OnCheckedChangeListener switchListener;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private NavigationView nav;
    private View headerView;
    private TextView email;
    private TextView username;
    private ImageView userPhoto;

    boolean closeEnough=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);



        setNavigationViewListner();
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
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


        name = (TextView) findViewById(R.id.name);
        address = (TextView) findViewById(R.id.address);
        telephone = (TextView) findViewById(R.id.telephone);
        type = (TextView) findViewById(R.id.type);
        imagePlace = (ImageView) findViewById(R.id.imagePlace);
        add_report = (Button)findViewById(R.id.add_report);
        table_reports = (TableLayout)findViewById(R.id.table_reports_place);
        favoritePlaces = (Switch)findViewById(R.id.favorite_places) ;




        reports = new ArrayList<Report>();
        users = new ArrayList<User>();

        Intent i = getIntent();
       placeNameString = i.getStringExtra("placeName");
       placeAddress = i.getStringExtra("placeAddress");
       placeTelephone = i.getStringExtra("placeTelephone");
       placeType = i.getStringExtra("placeType");

       name.setText("Place Name : " + placeNameString);
        address.setText("Address : " + placeAddress);
        telephone.setText("Telephone : " + placeTelephone);
        type.setText("Type : " + placeType);

        String imagePath = i.getStringExtra("placeImagePath");
        closeEnough = i.getBooleanExtra("closeEnough", true);


        storageRef.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {    /*The place image appears on the page*/
                Picasso.get().load(uri).resize(187, 139).into(imagePlace);
            }
        });


        myRef.child("report_" + placeNameString).addListenerForSingleValueEvent(new ValueEventListener() {    /*Any reporting of a particular place will be under the same place name*/
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot child : children)
                {
                    Report report = child.getValue(Report.class);
                    reports.add(report);   /*List of all the reports*/
                }
                showData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        add_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   /*Option to add a report if the user is in place*/

                if(!closeEnough)
                {
                    Toast.makeText(PlaceActivity.this, "Only users who are in the place can send reports", Toast.LENGTH_SHORT).show();
                }

                else
                {

                    if(confirmReporting())   /*A test that has been more than 3 hours since the user last reported for this place*/
                    {
                        Intent repIntent = new Intent(PlaceActivity.this, ReportActivity.class);
                        repIntent.putExtra("userName", userName);  /*Send the user name to reportActivity*/
                        repIntent.putExtra("placeName", placeNameString);
                        startActivity(repIntent);
                    }
                    else
                    {
                        Toast.makeText(PlaceActivity.this, "It's been 3 hours since you sent your last report for this place", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        myRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userId = mAuth.getUid();

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    User myUser = child.getValue(User.class);
                    if(myUser.getUserId().equals(userId))
                    {
                        userName = myUser.getUserName();    /*Find the username to send a possible report*/
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        favoritePlaces.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {   /*Add a place to the list of favorite places*/


                if (favoritePlaces.getTag() != null) {
                    favoritePlaces.setTag(null);
                    return;
                }

                if(isChecked)   /*Add the place to the favorite places list*/
                {
                    myRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        User myUser;
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String userId = mAuth.getUid();

                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                if (child.getKey().equals(userId))
                                {
                                    myUser = child.getValue(User.class);
                                    myUser.addToFavoritePlace(placeNameString);
                                    favoritePlaces.setText("The place is in your\n favorites list");

                                    Map <String, Object> data = new HashMap<>();
                                    data.put("favoritePlacesKey", myUser.getFavoritePlacesKey());
                                    myRef.child("user").child(userId).updateChildren(data);
                                    break;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
                else   /*Remove the place from the favorite places list*/
                {
                    myRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        User myUser;
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String userId = mAuth.getUid();

                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                if (child.getKey().equals(userId))
                                {
                                    myUser = child.getValue(User.class);
                                    myUser.removeFromFavoritePlaces(placeNameString);
                                    favoritePlaces.setText("   Add to your\n   favorite places");

                                    Map <String, Object> data = new HashMap<>();
                                    data.put("favoritePlacesKey", myUser.getFavoritePlacesKey());
                                    myRef.child("user").child(userId).updateChildren(data);
                                    break;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        });

        favoritePlaces.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                favoritePlaces.setTag(null);
                return false;
            }
        });



        setfavoritePlaceSwitchState();  /*Check whether the user has already set the place as a favorite. If the place is set as a favorite the switch will be on*/

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
                        username.setText(user.getUserName());    /*Display the name of the username on the sidebar*/
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

    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        finish();
        startActivity(getIntent());
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

    private void showData() {  /*Reports received about the place*/

        Collections.reverse(reports);
        showTitle();  /*Build the header row. Performs only once*/
        for(Report report : reports)  /*Displays the reports on a table*/
        {
            if(report.isRelevant())
            {
                showTheHead(report);     /*Title row for each report*/
                showHeadParameters(report);
                showTheVibe(report);
                showTheCrowding(report);
                showTheParking(report);
                showTheMusic(report);
                showThePrices(report);
                showTheSummary(report);
                showTheImage(report);
            }
        }

    }

    private void showTitle()
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);



        final TextView usernameTitle = new TextView(this);
        usernameTitle.setText("  User Name    ");
        usernameTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        usernameTitle.setTypeface(null, Typeface.BOLD);
        usernameTitle.setTextSize(15);



        final TextView dateTimeTitle = new TextView(this);
        dateTimeTitle.setText("Date/Time    ");
        dateTimeTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        dateTimeTitle.setTypeface(null, Typeface.BOLD);
        dateTimeTitle.setTextSize(15);

        final TextView statusTitle = new TextView(this);
        statusTitle.setText("Status    ");
        statusTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        statusTitle.setTypeface(null, Typeface.BOLD);
        statusTitle.setTextSize(15);



        row.addView(usernameTitle);
        row.addView(dateTimeTitle);
        row.addView(statusTitle);
        table_reports.addView(row);

    }


    private void showTheHead(final Report report)
    {
        final TableRow row = new TableRow(this);
        final TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.MATCH_PARENT);
        tableRow.setMargins(0,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#EEEEEE"));


        final TextView name = new TextView(this);
        name.setMaxEms(14);
        String username = report.getUserName();
        if(username.length()>14)
        {
            String fullname[] = username.split(" ");
            username = fullname[0];
        }
        name.setText("   "   + username+ "     " );
        name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        name.setTypeface(null, Typeface.BOLD);
        name.setTextSize(13);
        name.setTextColor(Color.parseColor("#A48C19"));


        final TextView time = new TextView(this);
        String currentTime;
        if(report.getDate().getHours()<10)
            currentTime="0" + report.getDate().getHours() + " : ";
        else
            currentTime ="" + report.getDate().getHours() + " : ";

        if(report.getDate().getMinutes()<10)
            currentTime+="0" + report.getDate().getMinutes() + " : ";
        else
            currentTime+=report.getDate().getMinutes() + " : ";

        if(report.getDate().getSeconds()<10)
            currentTime+="0" + report.getDate().getSeconds();
        else
            currentTime+=report.getDate().getSeconds();

        String currentDate="";
        int day = report.getDate().getDate();
        int month = report.getDate().getMonth()+1;
        int year = report.getDate().getYear()+1900;
        currentDate+=day + "/" + month + "/" + year;


        time.setText(currentTime + "     \n\n" + currentDate);
        time.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        time.setTypeface(null, Typeface.BOLD);
        time.setTextSize(13);
        time.setTextColor(Color.parseColor("#A48C19"));





        final ImageView status = new ImageView(PlaceActivity.this);
        status.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        if(report.getStatus().equals("Fun"))   /*set the status image according to the reporting status.*/
        {
            status.setImageResource(R.drawable.smile);
        }
        else if(report.getStatus().equals("Suffering"))
        {
            status.setImageResource(R.drawable.sad);
        }
        else
        {
            status.setImageResource(R.drawable.natural);
        }

        final TextView numOfLikes = new TextView(this);   /*The number of users who liked the report*/
        numOfLikes.setText("    \n   " + report.getLikes());
        numOfLikes.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        numOfLikes.setTypeface(null, Typeface.BOLD);
        numOfLikes.setTextColor(Color.parseColor("#A48C19"));




        final ImageView like = new ImageView(PlaceActivity.this);  /*The default choice for this ImageView*/
        like.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        like.setImageResource(R.drawable.like_before);

        final TextView spaces = new TextView(this);
        spaces.setText("    ");
        spaces.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        spaces.setTextColor(Color.parseColor("#A48C19"));


        final ImageView unlike = new ImageView(PlaceActivity.this);  /*The default choice for this ImageView*/
        unlike.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        unlike.setImageResource(R.drawable.unlike_before);



        final TextView numOfUnlikes = new TextView(this);   /*The number of users who didnt liked the report*/
        numOfUnlikes.setText("      \n  " + report.getUnlikes());
        numOfUnlikes.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        numOfUnlikes.setTypeface(null, Typeface.BOLD);
        numOfUnlikes.setTextColor(Color.parseColor("#A48C19"));



        String userId = mAuth.getUid();
        final boolean[] pushedLike = {false};
        final boolean[] pushedUnlike = {false};

        setLikeState(report, userId, like, pushedLike);   /*If the user has already liked the report, when he re-enters the reporting page, it will be marked.*/
        setUnlikeState(report, userId, unlike, pushedUnlike);  /*If the user does not like the report, when he re-enters the reporting page, it will be marked.*/


        row.addView(name);
        row.addView(time);
        row.addView(status);
        row.addView(numOfLikes);
        row.addView(like);
        row.addView(spaces);
        row.addView(unlike);
        row.addView(numOfUnlikes);
        table_reports.addView(row);


        row.setOnClickListener(new View.OnClickListener() {  /*When you click the title row, the reporting information will appear*/
            @Override
            public void onClick(View v) {

                int currentIndex = table_reports.indexOfChild(v);
                for(int i=currentIndex+1 ; i<currentIndex+9 ; i++) {

                    View child = table_reports.getChildAt(i);

                    if (child instanceof TableRow) {
                        TableRow rowTarget = (TableRow) child;
                        if (rowTarget.getVisibility() == TableRow.GONE) {
                            rowTarget.setVisibility(TableRow.VISIBLE);
                        } else {
                            rowTarget.setVisibility(TableRow.GONE);
                        }
                    }
                }
            }
        });



        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!closeEnough)
                {
                    Toast.makeText(PlaceActivity.this, "Only users who are in the place can rate the reports", Toast.LENGTH_SHORT).show();
                }
                else {

                    if (!pushedLike[0] && !pushedUnlike[0])  /*If the user liked the report*/ {
                        numOfLikes.setText("    \n   " + (report.getLikes() + 1));
                        like.setImageResource(R.drawable.like_after);

                        pushedLike[0] = !pushedLike[0];
                        String userId = mAuth.getUid();


                        updateLikeToReport(report, 1);
                        updateUserScore(report.getUserId(), 1);  /*Add a score to the user who sent the report*/
                        addReportKeyToLikesList(report, userId);   /*Adds the reporting key to a list of reports the user liked*/

                    } else if (pushedLike[0] && !pushedUnlike[0])   /*If the user chooses to cancel the "Like"*/ {
                        numOfLikes.setText("    \n   " + (report.getLikes() - 1));
                        like.setImageResource(R.drawable.like_before);
                        pushedLike[0] = !pushedLike[0];
                        String userId = mAuth.getUid();


                        updateLikeToReport(report, -1);
                        updateUserScore(report.getUserId(), -1);   /*Decreasing a score for the user who sent the report*/
                        removeReportKeyFromLikesList(report, userId);   /*Delete the reporting key for a list of reports that the user liked*/

                    } else {
                        Toast.makeText(PlaceActivity.this, "Conflicting ratings can not be performed for the same reporting", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });


        unlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!closeEnough)
                {
                    Toast.makeText(PlaceActivity.this, "Only users who are in the place can rate the reports", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!pushedLike[0] && !pushedUnlike[0])  /*If the user did not like the report*/ {
                        numOfUnlikes.setText("      \n  " + (report.getUnlikes() + 1));
                        unlike.setImageResource(R.drawable.unlike_after);
                        pushedUnlike[0] = !pushedUnlike[0];
                        String userId = mAuth.getUid();

                        updateUserScore(report.getUserId(), -1);   /*Decreasing a score for the user who sent the report*/
                        updateUnLikeToReport(report, 1);
                        addReportKeyToUnlikesList(report, userId);  /*Adds the reporting key to a list of reports the user did not like*/

                    } else if (!pushedLike[0] && pushedUnlike[0])  /*If the user chooses to cancel the "Unlike"*/ {
                        numOfUnlikes.setText("      \n  " + (report.getUnlikes() - 1));
                        unlike.setImageResource(R.drawable.unlike_before);
                        pushedUnlike[0] = !pushedUnlike[0];
                        String userId = mAuth.getUid();

                        updateUserScore(report.getUserId(), 1);   /*Add a score to the user who sent the report*/
                        updateUnLikeToReport(report, -1);
                        removeReportKeyFromUnlikesList(report, userId);   /*Delete the reporting key for a list of reports the user did not like*/
                    } else {
                        Toast.makeText(PlaceActivity.this, "Conflicting ratings can not be performed for the same reporting", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }


    private void showHeadParameters(Report report)
    {

        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#fff68f"));


        TextView space = new TextView(this);
        space.setText("");
        space.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));




        TextView head_parameters = new TextView(this);
        head_parameters.setText("Type :");
        head_parameters.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        head_parameters.setTypeface(null, Typeface.BOLD);
        head_parameters.setTextColor(Color.parseColor("#0000F0"));


        TextView head_rating = new TextView(this);
        head_rating.setText("Rating  :");
        head_rating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        head_rating.setTypeface(null, Typeface.BOLD);
        head_rating.setTextColor(Color.parseColor("#0000F0"));


        row.addView(head_parameters);
        row.addView(head_rating);
        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);
    }


    private void showTheVibe(Report report)
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#EEEEEE"));

        String vibeString = getVibeString(report.getVibe());  /*A method that returns the grade as a string*/


        TextView grade_vibe = new TextView(this);
        grade_vibe.setText(vibeString);
        grade_vibe.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView vibe = new TextView(this);
        vibe.setText("Vibe: ");
        vibe.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


        row.addView(grade_vibe);
        row.addView(vibe);
        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);
    }


    private void showTheCrowding(Report report)
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#fff68f"));

        String crowdingString = getCrowdingString(report.getCrowding());    /*A method that returns the grade as a string*/

        TextView grade_crowding = new TextView(this);
        grade_crowding.setText(crowdingString);
        grade_crowding.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView crowding = new TextView(this);
        crowding.setText("Crowding: ");
        crowding.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        row.addView(grade_crowding);
        row.addView(crowding);
        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);
    }


    private void showTheParking(Report report)
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#EEEEEE"));

        String parkingString = getParkingString(report.getParking());    /*A method that returns the grade as a string*/

        TextView grade_parking = new TextView(this);
        grade_parking.setText(parkingString);
        grade_parking.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


        TextView parking = new TextView(this);
        parking.setText("Parking: ");
        parking.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


        row.addView(grade_parking);
        row.addView(parking);
        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);
    }


    private void showTheMusic(Report report)
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#fff68f"));

        String musicString = report.getMusic();    /*A method that returns the grade as a string*/

        TextView grade_music = new TextView(this);
        grade_music.setText(musicString);
        grade_music.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView music = new TextView(this);
        music.setText("Music: ");
        music.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


        row.addView(grade_music);
        row.addView(music);
        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);
    }


    private void showThePrices(Report report)
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#EEEEEE"));

        String pricesString = getPricesString(report.getPrices());     /*A method that returns the grade as a string*/

        TextView grade_prices = new TextView(this);
        grade_prices.setText(pricesString);
        grade_prices.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView prices = new TextView(this);
        prices.setText("Prices: ");
        prices.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


        row.addView(grade_prices);
        row.addView(prices);
        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);
    }


    private void showTheSummary(Report report)
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#fff68f"));

        TextView grade_summary = new TextView(this);
        grade_summary.setText(report.getSummary() + "       ");
        grade_summary.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView summary = new TextView(this);
        summary.setText("Tweet: ");
        summary.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));




        row.addView(grade_summary);
        row.addView(summary);
        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);
    }

    private void showTheImage(final Report report)
    {
        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20,25,0,0);
        row.setLayoutParams(tableRow);
        row.setBackgroundColor(Color.parseColor("#EEEEEE"));



        final Switch imageSwitch = new Switch(this);
        imageSwitch.setText("Off      ");
        imageSwitch.setChecked(false);

        TextView image = new TextView(this);
        image.setText("Image: ");
        image.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));



        imageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    imageSwitch.setText("On       ");
                    Intent imageIntent = new Intent(PlaceActivity.this, ImageActivity.class);
                    imageIntent.putExtra("imagePath", report.getImagePath());

                    startActivity(imageIntent);

                    imageSwitch.setChecked(false);
                    imageSwitch.setText("Off      ");
                }
                else
                {
                    imageSwitch.setText("Off      ");
                }
            }
        });

        row.addView(imageSwitch);
        row.addView(image);

        row.setVisibility(TableRow.GONE);
        table_reports.addView(row);

    }


    public void updateUserScore(final String userId, final int n)  /*Finding the user using the ID and updating his score according to what the method received*/
    {
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
                        user.setScoring(user.getScoring() + n);
                        myRef.child("user").child(userId).setValue(user);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


    public void updateLikeToReport(Report report, int n)   /*Update the number of users who liked the report*/
    {

        report.setLikes((report.getLikes()+n));
        myRef.child("report_" + placeNameString).child(report.getReportKey()).setValue(report);
    }



    public void updateUnLikeToReport(Report report, int n)  /*Update the number of users who did not like reporting*/
    {
        report.setUnlikes((report.getUnlikes()+n));
        myRef.child("report_" + placeNameString).child(report.getReportKey()).setValue(report);
    }


    public void addReportKeyToLikesList(final Report report, final String userId)   /*If the user likes the report, the key of the report will be saved by the user*/
    {
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
                        user.addToLikeReportsKey(report.getReportKey());

                        Map<String, Object> data = new HashMap<>();
                        data.put("likeReportsKey", user.getLikeReportsKey());
                        myRef.child("user").child(userId).updateChildren(data);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void removeReportKeyFromLikesList(final Report report, final String userId)   /*If the user chooses to remove the LIKE from the report, the key of the report will be deleted from the user*/
    {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

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
                        user.removeFromLikeReportsKey(report.getReportKey());
                        Map<String, Object> data = new HashMap<>();
                        data.put("likeReportsKey", user.getLikeReportsKey());
                        myRef.child("user").child(userId).updateChildren(data);
                        break;
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void addReportKeyToUnlikesList(final Report report, final String userId)    /*If the user didn't likes the report, the key of the report will be saved by the user*/
    {
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
                        user.addToUnlikeReportsKey(report.getReportKey());

                        Map<String, Object> data = new HashMap<>();
                        data.put("unlikeReportsKey", user.getUnlikeReportsKey());
                        myRef.child("user").child(userId).updateChildren(data);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void removeReportKeyFromUnlikesList(final Report report, final String userId)   /*If the user chooses to remove the UNLIKE from the report, the key of the report will be deleted from the user*/
    {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

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
                        user.removeFromUnlikeReportsKey(report.getReportKey());
                        Map<String, Object> data = new HashMap<>();
                        data.put("unlikeReportsKey", user.getUnlikeReportsKey());
                        myRef.child("user").child(userId).updateChildren(data);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void setLikeState(final Report report, final String userId, final ImageView like, final boolean[] pushedLike)   /*If the user likes to report in the past, he will see it*/
    {
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
                        for(String key : user.getLikeReportsKey())
                        {
                            if(key.equals(report.getReportKey()))
                            {
                                pushedLike[0] = true;
                                like.setImageResource(R.drawable.like_after);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }


    private void setUnlikeState(final Report report, final String userId, final ImageView unlike, final boolean[] pushedUnlike)  /*If the user didn't likes to report in the past, he will see it*/
    {
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
                        for(String key : user.getUnlikeReportsKey())
                        {
                            if(key.equals(report.getReportKey()))
                            {
                                pushedUnlike[0] = true;
                                unlike.setImageResource(R.drawable.unlike_after);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


    private String getVibeString(float vibe)   /*A method that returns the grade as a string*/
    {
        String grade="";
        switch((int) vibe)
        {
            case 1:  grade = "Terrible";
                break;
            case 2: grade = "Bad";
                break;
            case 3: grade = "Okay";
                break;
            case 4: grade = "Good";
                break;
            case 5: grade = "Great";
                break;
        }
        return grade;
    }

    private String getCrowdingString(float crowding)   /*A method that returns the grade as a string*/
    {
        String grade="";
        switch((int)crowding)
        {
            case 1:  grade = "v Crowded";
                break;
            case 2: grade = "Crowded";
                break;
            case 3: grade = "Average";
                break;
            case 4: grade = "Spacious";
                break;
            case 5: grade = "v Spacious";
                break;
        }
        return grade;
    }

    private String getParkingString(int parking)    /*A method that returns the grade as a string*/
    {
        String grade="";
        switch(parking)
        {
            case 1:  grade = "No parking";
                break;
            case 5: grade = "There is parking      ";
                break;
        }
        return grade;
    }



    private String getPricesString(float prices)   /*A method that returns the grade as a string*/
    {
        String grade="";
        switch((int)prices)
        {
            case 1:  grade = "Terrible";
                break;
            case 2: grade = "Bad";
                break;
            case 3: grade = "Okay";
                break;
            case 4: grade = "Good";
                break;
            case 5: grade = "Great";
                break;
        }
        return grade;
    }

    private void setfavoritePlaceSwitchState()   /*If the user has already defined the place as a favorite, the switch will be turned on*/
    {
        favoritePlaces.setTag("TAG");   /*To avoid running the method 'setOnCheckedChangeListener' on the favoritePlaces Switch*/
        myRef.child("user").addValueEventListener(new ValueEventListener() {
            User myUser;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userId = mAuth.getUid();

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(userId)) {
                        myUser = child.getValue(User.class);
                        for (int i = 0; i < myUser.getFavoritePlacesKey().size(); i++) {
                            if (placeNameString.equals(myUser.getFavoritePlacesKey().get(i))) {
                                favoritePlaces.setChecked(true);
                                favoritePlaces.setText("The place is in your\n favorites list");
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private boolean confirmReporting()   /*A test that has been more than 3 hours since the user last reported for this place*/
    {
        int MILLI_TO_HOUR = 1000 * 60 * 60;
        Date currentDate = new Date();
        String userId = mAuth.getUid();
        long difference=0;


        for(Report report : reports)
        {
            if(report.getUserId().equals(userId))
            {
               difference =(currentDate.getTime() - report.getDate().getTime())/MILLI_TO_HOUR;

               if(difference<3 && difference>=0)
               {
                   return false;
               }
            }
        }
       return true;
    }
}


