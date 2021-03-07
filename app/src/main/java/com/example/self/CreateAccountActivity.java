package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private Button createButton;
    private EditText nameEditText;
    private AutoCompleteTextView emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //FireStore Connection
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    private final CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth=FirebaseAuth.getInstance();

        createButton=findViewById(R.id.create_account);
        progressBar=findViewById(R.id.create_account_progress);
        emailEditText=findViewById(R.id.create_email);
        passwordEditText=findViewById(R.id.create_password);
        nameEditText=findViewById(R.id.enter_name);

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser=firebaseAuth.getCurrentUser();
                if(currentUser!=null)
                {
                    //user is already logged in
                }
                else
                {
                    //user not logged in
                }

            }
        };

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailEditText.getText().toString().trim();
                String password=passwordEditText.getText().toString().trim();
                String username=nameEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(email)
                        && !TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(username))
                {
                    Log.d("check1", "onClick: success1");
                    createUserEmailAccount(email,password,username);
                }
                else
                {
                    Log.d("check", "onClick: success");
                    Toast.makeText(CreateAccountActivity.this, "Empty fields not allowed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createUserEmailAccount(String email,String password,String username) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username))
         {
            progressBar.setVisibility(View.VISIBLE);

//             Log.d("check2", "onClick: success2");

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

//                    Log.d("check3", "onClick: success3");

                    if(task.isSuccessful())
                    {
//                        Log.d("check4", "onClick: success4");
                        Toast.makeText(CreateAccountActivity.this,
                                "Registered successfully.Please verify your email address.", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);

                        //we take our user to AddJournalActivity
                        currentUser=firebaseAuth.getCurrentUser();
                        assert currentUser != null;
                        String currentUserId= currentUser.getUid();
//                        Log.d("checkF", "onComplete: "+currentUserId);

                        //Creating a user map so we can create a user in the User collection
                        Map<String,String> userObj=new HashMap<>();
                        userObj.put("userId",currentUserId);
                        userObj.put("username",username);

                        collectionReference.add(userObj)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
//                                Log.d("check5", "onClick: success5");
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                        Log.d("check6", "onClick: success6");

                                        if(Objects.requireNonNull(task.getResult()).exists())
                                        {
//                                            Log.d("check7", "onClick: success7");
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String name=task.getResult().getString("username");

                                            JournalApi journalApi=JournalApi.getInstance(); //GLOBAL API
                                            journalApi.setUserId(currentUserId);
                                            journalApi.setUserName(name);

                                            Intent intent= new Intent(CreateAccountActivity.this,
                                                    PostJournalActivity.class);
//                                            intent.putExtra("username",name);
//                                            intent.putExtra("userId",currentUserId);
//                                            Log.d("checkM", "onComplete: successM");

                                            startActivity(intent);
                                            finish();
                                        }
                                        else
                                        {
                                            Toast.makeText(CreateAccountActivity.this,
                                                    Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e("checkN","From Database Inner "+task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("check8", "onClick: success8");
                            }
                        });
//                        Log.d("check9", "onClick: success9");
                        //save to fireBase Database
                    }
                    else
                    {
//                        Log.d("check10", "onClick: success10");
                        //something went wrong
                        Toast.makeText(CreateAccountActivity.this,
                                task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
//                    Log.d("check11", "onClick: success11");
                    Toast.makeText(CreateAccountActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
//            Log.d("check12", "onClick: success12");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}