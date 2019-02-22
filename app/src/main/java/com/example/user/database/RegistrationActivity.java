package com.example.user.database;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;

public class RegistrationActivity extends AppCompatActivity {

    EditText email_text;
    EditText pass1;
    EditText pass2;
    EditText first;
    EditText last;
    Button registrate;

    FirebaseAuth mAuth;
    private ProgressDialog progressDialog;



    private BottomNavigationView bottomNavigationItemView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Intent i = getIntent();
        boolean isLogout = i.getBooleanExtra("logout", false);   /*In case of logout refer to home page*/
        if(isLogout)
        {
            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(intent);
        }

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        email_text = (EditText)findViewById(R.id.email);
        pass1 = (EditText)findViewById(R.id.password1);
        pass2 = (EditText)findViewById(R.id.password2);
        first = (EditText)findViewById(R.id.first_name);
        last = (EditText)findViewById(R.id.last_name);
        registrate = (Button)findViewById(R.id.registrate);

        final boolean[] check = new boolean[1];




        bottomNavigationItemView = (BottomNavigationView)findViewById(R.id.navB) ;

        bottomNavigationItemView.getMenu().getItem(1).setChecked(true);



        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.Signin_menu: {

                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
                return true;
            }
        });


        registrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {







                final String email = email_text.getText().toString().trim();
                String password1 = pass1.getText().toString().trim();
                String password2 = pass2.getText().toString().trim();
                final String first_name = first.getText().toString().trim();
                final String last_name = last.getText().toString().trim();
                boolean correctFlag=true;







                    /*All tests for input integrity*/
                    if (!password1.equals(password2)) {
                        Toast.makeText(RegistrationActivity.this, "The password does not match", Toast.LENGTH_LONG).show();
                        correctFlag = false;
                    }
                    if (password1.equals("") && password2.equals("")) {
                        Toast.makeText(RegistrationActivity.this, "No password entered", Toast.LENGTH_LONG).show();
                        correctFlag = false;
                    }
                    if (email.equals("") || first_name.equals("") || last_name.equals("")) {
                        Toast.makeText(RegistrationActivity.this, "One or more of the parameters are incorrect", Toast.LENGTH_LONG).show();
                        correctFlag = false;
                    }

                    if (correctFlag)  /*There is no problem filling the fields*/ {
                        progressDialog.setMessage("Registrating user...");
                        progressDialog.show();

                        mAuth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(RegistrationActivity.this, "Registered Succesfully", Toast.LENGTH_SHORT).show();
                                    progressDialog.cancel();
                                    Intent intent = new Intent(RegistrationActivity.this, AccountActivity.class);
                                    intent.putExtra("firstName", first_name);
                                    intent.putExtra("lastName", last_name);
                                    startActivity(intent);
                                } else {


                                    mAuth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                                            check[0] = !task.getResult().getProviders().isEmpty();

                                            if(check[0])
                                            {
                                                Toast.makeText(RegistrationActivity.this, "The email address you entered is already registered to the app", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(RegistrationActivity.this, "Could not register. please try again", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    progressDialog.cancel();
                                }
                            }
                        });
                    }
                }

        });
    }
}
