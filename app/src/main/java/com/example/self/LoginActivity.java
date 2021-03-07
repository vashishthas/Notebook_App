package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private Button createAccountButton;
    private ProgressBar progressBar;

    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //FireStore Connection
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    private final CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth=FirebaseAuth.getInstance();

        loginButton=findViewById(R.id.login_button);
        createAccountButton=findViewById(R.id.create_account_login);
        progressBar=findViewById(R.id.login_progress);

        emailAddress=findViewById(R.id.email_login);
        password=findViewById(R.id.password_login);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                        password.getText().toString().trim());

            }

        });
    }

    private void loginEmailPasswordUser(String email, String pwd) {

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd))
        {
            firebaseAuth.signInWithEmailAndPassword(email,pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user= firebaseAuth.getCurrentUser();

                            assert user != null;
                            String currentUserId=user.getUid();

                            //Searching amongst the users
                            collectionReference.whereEqualTo("userId",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                            if(e!=null)
                                            {

                                            }
                                            if(!queryDocumentSnapshots.isEmpty())
                                            {
                                                for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots)
                                                {
                                                    JournalApi journalApi= JournalApi.getInstance();
                                                    journalApi.setUserName(snapshot.getString("username"));
                                                    journalApi.setUserId(currentUserId);
                                                    //OR
//                                                    journalApi.setUserId(snapshot.getString("userId"));
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    //Go to list Activity
                                                    startActivity(new Intent(LoginActivity.this,JournalListActivity.class));
                                                    finish();
                                                }
                                            }
                                            else
                                            {

                                            }
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
        else
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please Enter email and password", Toast.LENGTH_SHORT).show();
        }
    }
}