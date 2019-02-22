package com.example.user.database;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SignInButton signIn;
    DatabaseReference myRef;
    FirebaseDatabase database;


    private int RC_SIGN_IN=1;
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG = "MainActivity";
    private FirebaseAuth mAuth;

    private Button registration;

    private EditText email;
    private EditText password;
    private Button login, forgot;
    private ProgressDialog progressDialog;


    private BottomNavigationView bottomNavigationItemView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        signIn = (SignInButton)findViewById(R.id.sign_in_button);
        forgot = (Button) findViewById(R.id.forgot);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading the app...");
        progressDialog.show();


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

                        if(myUser.isOnline())
                        {
                            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                            startActivity(intent);
                        }

                        break;
                    }
                }
                progressDialog.cancel();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });




        registration = (Button) findViewById(R.id.registrate);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.login);


        bottomNavigationItemView = (BottomNavigationView)findViewById(R.id.navB) ;
        bottomNavigationItemView.getMenu().getItem(0).setChecked(true);


        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.register_menu: {

                        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
                return true;
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email_text = email.getText().toString().trim();
                String password_text = password.getText().toString().trim();

                if(TextUtils.isEmpty(email_text) || TextUtils.isEmpty(password_text))
                {
                    Toast.makeText(MainActivity.this, "One or more fields are empty", Toast.LENGTH_LONG).show();
                }
                else
                {
                    mAuth.signInWithEmailAndPassword(email_text, password_text).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(!task.isSuccessful())
                            {
                                Toast.makeText(MainActivity.this, "Sign in problem", Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });


    }


    private void signIn() {   /*Sign in to the app with Google Account*/
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch(ApiException e){

                Log.w(TAG, "Google Sin in Failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithCredential: success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else
                        {
                            Log.w(TAG, "signInWithCredential: failure", task.getException());  /*In case of unsuccessful login*/
                            Toast.makeText(MainActivity.this, "You are not able to log in to Google", Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }
                    }
                });


    }

    private void updateUI(FirebaseUser user)   /*In case of successful registration*/
    {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            Toast.makeText(this, "Name of User : " + personName + "UserId is : " + personId, Toast.LENGTH_LONG);

            Intent intent = new Intent(this, AccountActivity.class);
            intent.putExtra("personPhoto", personPhoto.toString());
            startActivity(intent);
        }
    }



}

