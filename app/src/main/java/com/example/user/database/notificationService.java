package com.example.user.database;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class notificationService extends Service {

    DatabaseReference myRef;
    DatabaseReference myRef2;
    private FirebaseAuth mAuth;

    List <String> recentlyUploadesKey;  /*Will keep all keys of the RecentUpload object in the database*/

    @Override

    public void onCreate() {
        super.onCreate();

        initNotificationChannel();

        myRef = FirebaseDatabase.getInstance().getReference();
        recentlyUploadesKey = new ArrayList<String>();

        myRef.child("recentlyUpload").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                mAuth = FirebaseAuth.getInstance();


                    myRef = FirebaseDatabase.getInstance().getReference().child("recentlyUpload");

                    myRef.addChildEventListener(new ChildEventListener() {
                        RecentlyUpload recentlyUpload;
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {



                            Intent intent = new Intent(getBaseContext(), favoritePlacesActivity.class);   /*The notification link will be in the app's favorite places*/
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            final PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


                            recentlyUpload = dataSnapshot.getValue(RecentlyUpload.class);
                            if(checkProximityDates(recentlyUpload.getDate())) {   /*Check whether the date the report was filed overlaps with the current date*/

                                myRef2 = FirebaseDatabase.getInstance().getReference();

                                myRef2.child("user").addListenerForSingleValueEvent(new ValueEventListener() {

                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                        for (DataSnapshot child : children) {
                                            if (child.getKey().equals(mAuth.getUid())) {   /*Find myself*/
                                                User myUser = child.getValue(User.class);
                                                if (myUser.isOnline() && myUser.isNotification() && !myUser.getUserId().equals(recentlyUpload.getUserId())) {   /*send notification conditions*/
                                                    if (myUser.findFavoritePlace(recentlyUpload.getPlaceName())) {
                                                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), "SIN")
                                                                .setContentTitle("Hello from Safun")
                                                                .setContentText("Someone reported about " + recentlyUpload.getPlaceName())
                                                                .setSmallIcon(R.drawable.safun_icon)
                                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                                .setContentIntent(pendingIntent)
                                                                .setAutoCancel(true);
                                                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                                                        notificationManagerCompat.notify(0, notificationBuilder.build());
                                                    }
                                                    break;
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

                        private boolean checkProximityDates(Date dateReport)/*Check whether the date the report was filed overlaps with the current date*/
                        {
                            Date currentDate = new Date();
                            int MILLI_TO_SECOND = 1000;
                            int difference=0;


                            difference =  (int)(currentDate.getTime() - dateReport.getTime())/MILLI_TO_SECOND;
                            if(difference<20)  /*Until 5 seconds difference between the dates will send notification to the user*/
                            {
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       // return super.onStartCommand(intent, flags, startId);


        myRef = FirebaseDatabase.getInstance().getReference();
        recentlyUploadesKey = new ArrayList<String>();

        myRef.child("recentlyUpload").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                mAuth = FirebaseAuth.getInstance();


                myRef = FirebaseDatabase.getInstance().getReference().child("recentlyUpload");

                myRef.addChildEventListener(new ChildEventListener() {
                    RecentlyUpload recentlyUpload;
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {



                        Intent intent = new Intent(getBaseContext(), favoritePlacesActivity.class);   /*The notification link will be in the app's favorite places*/
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        final PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


                        recentlyUpload = dataSnapshot.getValue(RecentlyUpload.class);
                        if(checkProximityDates(recentlyUpload.getDate())) {   /*Check whether the date the report was filed overlaps with the current date*/

                            myRef2 = FirebaseDatabase.getInstance().getReference();

                            myRef2.child("user").addListenerForSingleValueEvent(new ValueEventListener() {

                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                    for (DataSnapshot child : children) {
                                        if (child.getKey().equals(mAuth.getUid())) {   /*Find myself*/
                                            User myUser = child.getValue(User.class);
                                            if (myUser.isOnline() && myUser.isNotification() && !myUser.getUserId().equals(recentlyUpload.getUserId())) {   /*send notification conditions*/
                                                if (myUser.findFavoritePlace(recentlyUpload.getPlaceName())) {


                                                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), "SIN")
                                                            .setContentTitle("Hello from Safun")
                                                            .setContentText("Someone reported about " + recentlyUpload.getPlaceName())
                                                            .setSmallIcon(R.drawable.safun_icon)
                                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                            .setContentIntent(pendingIntent)
                                                            .setAutoCancel(true);
                                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                                                    notificationManagerCompat.notify(0, notificationBuilder.build());


                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });
                        }
                    }

                    private boolean checkProximityDates(Date dateReport)/*Check whether the date the report was filed overlaps with the current date*/
                    {
                        Date currentDate = new Date();
                        int MILLI_TO_SECOND = 1000;
                        int difference=0;


                        difference =  (int)(currentDate.getTime() - dateReport.getTime())/MILLI_TO_SECOND;
                        if(difference<20)  /*Until 5 seconds difference between the dates will send notification to the user*/
                        {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return super.onStartCommand(intent, flags, startId);

    }


    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("SIN",
                "SIN_NOTIFICATIONS",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("NAVIGATION_NOTIFICATIONS");
        notificationManager.createNotificationChannel(channel);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}


