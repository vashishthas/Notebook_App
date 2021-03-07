package com.example.self;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import util.JournalApi;
import model.Journal;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostJournalActivity";
    private static final int GALLERY_CODE = 1;

    private Button saveButton;
    private ProgressBar progressBar;
    private TextView currentUserTextView;
    private EditText titleEditText, thoughtsEditText;

    private ImageView addPhotoButton;
    private ImageView imageView;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection to FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Storage
    private StorageReference storageReference;


    private final CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

//        Log.d("checkP", "onCreate: ");

        //Instead of passing information in intent we made a GLOBAL CLASS
//        Bundle bundle=getIntent().getExtras();
//        if(bundle!=null)
//        {
//            String username=bundle.getString("username");
//            String userId=bundle.getString("userId");
//        }

        storageReference= FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar);
        titleEditText = findViewById(R.id.editTextTitle);
        thoughtsEditText = findViewById(R.id.editTextDescription);
        currentUserTextView = findViewById(R.id.post_title_edit);
        imageView = findViewById(R.id.imageView);

        progressBar.setVisibility(View.INVISIBLE);

        addPhotoButton = findViewById(R.id.postCameraButton);
        saveButton = findViewById(R.id.save_button);

        if (JournalApi.getInstance() != null)
        {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUserName();

            currentUserTextView.setText(currentUserName);
        }
        else
        {
            Toast.makeText(this, "Something Wrong with Journal API", Toast.LENGTH_SHORT).show();
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null)
                {

                } else {

                }
            }
        };

        addPhotoButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button:
                //save Journal
                saveJournal();
                break;
            case R.id.postCameraButton:
                //get image from gallery/phone
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);//Explicit intent
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                addPhotoButton.setVisibility(View.INVISIBLE);
                break;

        }
    }

    private void saveJournal() {
        String title = titleEditText.getText().toString().trim();
        String thoughts = thoughtsEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null) {
            //Saving image in STORAGE
            StorageReference filepath = storageReference
                    .child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds()); //my_image_22345
            //Important
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            progressBar.setVisibility(View.INVISIBLE);

                            //To get Image Url and save it in journal class
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageUrl = uri.toString();

                                    //TODO: create a Journal Object
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserName(currentUserName);
                                    journal.setUserId(currentUserId);

                                    //TODO: invoke our collection reference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                    finish();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PostJournalActivity.this, "File not Added", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "onFailure: " + e.getMessage());
                                        }
                                    });

                                    //TODO: add and save a Journal instance
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);

                }
            });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Empty Fields not allowed!!", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();// we have the actual path to image
                imageView.setImageURI(imageUri);// show image
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}