package com.example.user.database;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.List;
import java.util.Vector;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    final int MAX_DISTANCE_METERS=100;
    private GoogleMap mMap;
    private EditText addressField;
    private ImageButton search;
    Geocoder geocoder;

    private FirebaseAuth mAuth;
    DatabaseReference myRef;
    FirebaseDatabase database;

    FirebaseStorage storage;
    StorageReference storageRef;

    Vector<Place> places;
    Vector<Marker> markers;

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
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        setNavigationViewListner();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        places = new Vector<Place>();
        markers = new Vector<Marker>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addressField = (EditText) findViewById(R.id.location_search);
        search = (ImageButton) findViewById(R.id.button_search);

        geocoder = new Geocoder(this);


        nav = (NavigationView) findViewById(R.id.nav1);
        headerView = nav.getHeaderView(0);
        email = (TextView) headerView.findViewById(R.id.email);
        username = (TextView) headerView.findViewById(R.id.username);
        userPhoto = (ImageView)headerView.findViewById(R.id.userPhoto);
        FirebaseUser user = mAuth.getCurrentUser();
        email.setText(user.getEmail());
        findName();             /*Display the name of the username on the sidebar*/



        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {      /*Quick search option*/

                String address = addressField.getText().toString();

                List<Address> adressList = null;
                MarkerOptions userMarkerOptions = new MarkerOptions();

                if (!TextUtils.isEmpty(address)) {
                    try {
                        adressList = geocoder.getFromLocationName(address, 6);

                        if(adressList.size()==0)
                        {
                            Toast.makeText(MapsActivity.this, "Location not found", Toast.LENGTH_LONG).show();
                        }

                        if (adressList != null) {
                            for (int i = 0; i < adressList.size(); i++) {
                                Address userAddress = adressList.get(i);
                                LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());

                                userMarkerOptions.position(latLng);
                                userMarkerOptions.title(address);
                                userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                                mMap.addMarker(userMarkerOptions);
                                float zoomLevel = 16.0f; //This goes up to 21
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                            }
                        } else {
                        }
                    } catch (Exception e) {
                        Toast.makeText(MapsActivity.this, "Location not found", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Please write any location name", Toast.LENGTH_LONG).show();
                }
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

            case R.id.only_restaurants: {
                Intent intent = new Intent(this, MapsRestaurantsActivity.class);

                startActivity(intent);
                break;
            }
            case R.id.only_clubs: {
                Intent intent = new Intent(this, MapsClubsActivity.class);

                startActivity(intent);
                break;
            }
            case R.id.only_bars: {
                Intent intent = new Intent(this, MapsBarsActivity.class);

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


    static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 9;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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


        }
        else {

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            /*Toast.makeText(MapsActivity.this, "Current location activation required", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            */

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                final String message = "Some app services require GPS operation.\n" +
                        "Do you want to turn on a GPS sensor?";

                builder.setMessage(message)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        startActivity(new Intent(action));
                                        d.dismiss();
                                    }

                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        d.cancel();
                                    }
                                });
                builder.create().show();
            } else {
                mMap.setMyLocationEnabled(true);    /*The current location is displayed on the map*/
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    /*What should I write here so that the program will not collapse?*/
                } else {
                    LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());     /*Current location*/
                    float zoomLevel = 16.0f; //This goes up to 21
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, zoomLevel));  /*The map will focus on the current location*/
                }

            }


            myRef.child("place").addListenerForSingleValueEvent(new ValueEventListener() {   /*Sort places by color*/
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for (DataSnapshot child : children) {
                        Place place = child.getValue(Place.class);
                        LatLng placeOnMap = new LatLng(place.getLatitude(), place.getLongitude());
                        MarkerOptions placeType = new MarkerOptions();
                        placeType.position(placeOnMap);
                        placeType.title(place.getName());
                        if (place.getType().equals("Restaurant")) {
                            placeType.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        } else if (place.getType().equals("Bar")) {
                            placeType.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        } else if (place.getType().equals("Club")) {
                            placeType.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        }

                        markers.add(mMap.addMarker(placeType));
                        places.add(place);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                    //  mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                    // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    for (int i = 0; i < markers.size(); i++) {
                        if (markers.get(i).equals(marker)) {   /*Finding the chosen place and all its details*/
                            Intent intent = new Intent(MapsActivity.this, PlaceActivity.class);
                            intent.putExtra("placeName", places.get(i).getName());
                            intent.putExtra("placeAddress", places.get(i).getAddress());
                            intent.putExtra("placeTelephone", places.get(i).getTelephone());
                            intent.putExtra("placeImagePath", places.get(i).getImagePath());
                            intent.putExtra("placeType", places.get(i).getType());


                            LocationManager locationManager = (LocationManager)
                                    getSystemService(Context.LOCATION_SERVICE);
                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling

                            }
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location == null) {
                                intent.putExtra("closeEnough", false);
                                Toast.makeText(MapsActivity.this, "No GPS provider", Toast.LENGTH_SHORT).show();
                            } else {
                                LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());  /*Current location*/
                                float metersAway = calcDistance(myLoc.latitude, myLoc.longitude, markers.get(i).getPosition().latitude, markers.get(i).getPosition().longitude);

                                if (metersAway <= MAX_DISTANCE_METERS)   /*Check whether the user can accept the privileges of a user present at this place*/ {
                                    intent.putExtra("closeEnough", true);
                                } else {
                                    intent.putExtra("closeEnough", false);
                                }
                            }
                            startActivity(intent);

                        }
                    }
                    return false;
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
