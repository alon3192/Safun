package com.example.user.database;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static com.example.user.database.MapsActivity.MY_PERMISSIONS_REQUEST_FINE_LOCATION;

public class favoritePlacesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    final int MAX_DISTANCE_METERS=100;

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

    TableLayout table;
    List<String> favoritePlacesKeys;
    User myUser;
    List<Place> places;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private boolean locationNull = false;
    LatLng myLoc;
    int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);

        setNavigationViewListner();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        table = (TableLayout) findViewById(R.id.favorite_places_table);
        favoritePlacesKeys = new Vector<String>();
        places = new ArrayList<Place>();
        count=0;


        nav = (NavigationView) findViewById(R.id.nav1);
        headerView = nav.getHeaderView(0);
        email = (TextView) headerView.findViewById(R.id.email);
        username = (TextView) headerView.findViewById(R.id.username);
        userPhoto = (ImageView) headerView.findViewById(R.id.userPhoto);
        FirebaseUser user = mAuth.getCurrentUser();
        email.setText(user.getEmail());
        findName();         /*Display the name of the username on the sidebar*/

        Menu nav_Menu = nav.getMenu();
        nav_Menu.findItem(R.id.itemMap).setVisible(false);  /*The option to sort the map is not relevant to this page and therefore is not displayed*/


        myRef.child("place").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Place place = child.getValue(Place.class);
                    places.add(place);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        myRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userId = mAuth.getUid();

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    myUser = child.getValue(User.class);
                    if (myUser.getUserId().equals(userId)) {
                        favoritePlacesKeys = myUser.getFavoritePlacesKey();   /*Retrieves the user's favorite places from the database*/
                        putOnTheTable();   /*Placing the user's favorite places on a table*/
                        break;
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
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

    private void findName()  /*A user-specific*/ {
        final String userId = mAuth.getUid();

        myRef.child("user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getKey().equals(userId)) {
                        User user = child.getValue(User.class);
                        username.setText(user.getUserName());    /*The user name will be displayed on the sidebar*/
                        if (mAuth.getCurrentUser().getDisplayName() != null && !mAuth.getCurrentUser().getDisplayName().equals("")) {
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


    private void putOnTheTable() {
        Collections.reverse(favoritePlacesKeys);   /*The last place added to favorites will be the first on the list*/
        //   showTitle();
        for (int i = 0; i < favoritePlacesKeys.size(); i++) {
            showData(favoritePlacesKeys.get(i));
        }
    }

    private void showTitle() {

        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(20, 25, 0, 0);
        row.setLayoutParams(tableRow);


        final TextView placeNameTitle = new TextView(this);
        placeNameTitle.setText("Place Name    ");
        placeNameTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        placeNameTitle.setTypeface(null, Typeface.BOLD);
        placeNameTitle.setTextSize(18);

        final TextView placeAddress = new TextView(this);
        placeAddress.setText("Address    ");
        placeAddress.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        placeAddress.setTypeface(null, Typeface.BOLD);
        placeAddress.setTextSize(18);

        final TextView placeTelephone = new TextView(this);
        placeTelephone.setText("Telephone    ");
        placeTelephone.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        placeTelephone.setTypeface(null, Typeface.BOLD);
        placeTelephone.setTextSize(18);

        final TextView placeType = new TextView(this);
        placeType.setText("Type    ");
        placeType.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        placeType.setTypeface(null, Typeface.BOLD);
        placeType.setTextSize(18);


        row.addView(placeNameTitle);
        row.addView(placeAddress);
        row.addView(placeTelephone);
        row.addView(placeType);
        table.addView(row);

    }

    private void showData(final String key) {   /*Find all the details of the place with the name of the place*/
        myRef.child("place").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    Place place = child.getValue(Place.class);
                    if (place.getName().equals(key)) {
                        showTheDetails(place.getName(), place.getAddress(), place.getTelephone(), count++);   /*Send all place information*/
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    private void showTheDetails(final String name, String address, String telephone, int count)   /*A method that adds the place to the table plus a button to delete the place from the favorites*/ {
        Resources resource = getResources();

        final TableRow row = new TableRow(this);
        TableLayout.LayoutParams tableRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRow.setMargins(0, 45, 0, 0);
        row.setLayoutParams(tableRow);


        final TextView placeNameTitle = new TextView(this);
        placeNameTitle.setText("      " + name + "   ");
        placeNameTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        placeNameTitle.setTextColor(Color.parseColor("#4444FF"));
        placeNameTitle.setTextSize(15);
        placeNameTitle.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD_ITALIC));


        final TextView placeAddress = new TextView(this);
        placeAddress.setText(address);
        placeAddress.setTextSize(12);
        placeAddress.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


        final TextView placeTelephone = new TextView(this);
        placeTelephone.setText(" " + telephone + "  ");
        placeTelephone.setTextSize(12);
        placeTelephone.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


        final ImageButton remove = new ImageButton(this);
        //remove.setText("X");
        remove.setImageResource(R.drawable.garbage_icon);
        //  remove.setTextSize(20);
        remove.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        remove.getLayoutParams().width = 110;
        remove.setBackgroundColor(resource.getColor(R.color.white));


        row.addView(placeNameTitle);
        row.addView(placeAddress);
        row.addView(placeTelephone);
        row.addView(remove);
        row.setBackgroundColor(resource.getColor(R.color.white));
        table.addView(row);


        remove.setOnClickListener(new View.OnClickListener() {  /*Delete the place from favorites*/
            @Override
            public void onClick(View v) {

                myUser.removeFromFavoritePlaces(name);
                Map<String, Object> data = new HashMap<>();
                data.put("favoritePlacesKey", myUser.getFavoritePlacesKey());
                myRef.child("user").child(mAuth.getUid()).updateChildren(data);

                finish();
                startActivity(getIntent());


            }
        });


            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return;
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }


            } else {

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationNull = true;

                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location == null) {
                        /*What should I write here so that the program will not collapse?*/
                        locationNull = true;
                    } else {
                        myLoc = new LatLng(location.getLatitude(), location.getLongitude());     /*Current location*/

                    }

                }




            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for (int i = 0; i < places.size(); i++) {
                        if (places.get(i).getName().equals(name)) {
                            Intent intent = new Intent(favoritePlacesActivity.this, PlaceActivity.class);
                            intent.putExtra("placeName", places.get(i).getName());
                            intent.putExtra("placeAddress", places.get(i).getAddress());
                            intent.putExtra("placeTelephone", places.get(i).getTelephone());
                            intent.putExtra("placeImagePath", places.get(i).getImagePath());
                            intent.putExtra("placeType", places.get(i).getType());

                            if (locationNull) {
                                intent.putExtra("closeEnough", false);
                                Toast.makeText(favoritePlacesActivity.this, "No GPS provider", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                float metersAway = calcDistance(myLoc.latitude, myLoc.longitude, places.get(i).getLatitude(), places.get(i).getLongitude());

                                if (metersAway <= MAX_DISTANCE_METERS)   /*Check whether the user can accept the privileges of a user present at this place*/ {
                                    intent.putExtra("closeEnough", true);
                                }
                                else {
                                    intent.putExtra("closeEnough", false);
                                }
                            }
                            startActivity(intent);
                        }
                    }
                }
            });
        }
    }
    private float calcDistance(double latitude1, double longitude1, double latitude2, double longitude2)   /*Calculate the distance in meters of the user from the place he chose*/
    {
        Location loc1 = new Location("");
        loc1.setLatitude(latitude1);
        loc1.setLongitude(longitude1);

        Location loc2 = new Location("");
        loc2.setLatitude(latitude2);
        loc2.setLongitude(longitude2);

        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }
}

