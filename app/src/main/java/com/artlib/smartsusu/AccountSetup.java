package com.artlib.smartsusu;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AccountSetup extends AppCompatActivity {

    private static int GALLERY_CODE = 120;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText usernameEditText;
    private Button createAccountButton;
    private ImageView passportImageView;
    private ProgressBar progressBar;
    private Uri passportUri = null;
    private FirebaseFirestore customerFirestore;
    private FirebaseAuth firebaseAuth;
    private StorageReference customerPassportStorageReference;
    private UserLocalDataStore userLocalDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.password_confirm);
        createAccountButton = findViewById(R.id.create_account_button);
        passportImageView = findViewById(R.id.passport_image_button);
        progressBar = findViewById(R.id.progress);

        progressBar.setVisibility(View.INVISIBLE);

        customerFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        customerPassportStorageReference = FirebaseStorage.getInstance().getReference();
        userLocalDataStore = new UserLocalDataStore(this);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        customerFirestore.setFirestoreSettings(settings);

        usernameEditText.setKeyListener(null);
        usernameEditText.setText(userLocalDataStore.getCustomerEmailAddress());

        passportImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCustomerAccount();
            }
        });

    }

    private void createCustomerAccount() {
        if (passportUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setBackgroundColor(Color.parseColor("#AA00FF"));
            createAccountButton.setEnabled(false);
            final String password = passwordEditText.getText().toString();
            final String confirmPassword = confirmPasswordEditText.getText().toString();

            if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
                if (password.equals(confirmPassword)) {
                    firebaseAuth.createUserWithEmailAndPassword(userLocalDataStore.getCustomerEmailAddress(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final String customerId = firebaseAuth.getCurrentUser().getUid();
                                StorageReference filePath = customerPassportStorageReference.child("CustomerPassport").child(passportUri.getLastPathSegment());
                                filePath.putFile(passportUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String downloadedUri = taskSnapshot.getUploadSessionUri().toString();
                                        Date currentTime = Calendar.getInstance().getTime();
                                        Map<String, String> customerData = new HashMap<>();
                                        customerData.put("firstname", userLocalDataStore.getCustomerFirstName());
                                        customerData.put("lastname", userLocalDataStore.getCustomerLastName());
                                        customerData.put("dateofbirth", userLocalDataStore.getCustomerDateOfBirth());
                                        customerData.put("housenumber", userLocalDataStore.getCustomerHouseNumber());
                                        customerData.put("voterid", userLocalDataStore.getCustomerVoterId());
                                        customerData.put("email", userLocalDataStore.getCustomerEmailAddress());
                                        customerData.put("phone", userLocalDataStore.getCustomerPhoneNumber());
                                        customerData.put("passport", downloadedUri + currentTime);

                                        customerFirestore.collection("Customers")
                                                .document(customerId)
                                                .set(customerData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        createAccountButton.setEnabled(true);
                                                        Intent dashboardIntent = new Intent(AccountSetup.this, DashboardActivity.class);
                                                        dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(dashboardIntent);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                createAccountButton.setEnabled(true);
                                                Toast.makeText(AccountSetup.this, "Account could not be created. Try again", Toast.LENGTH_LONG).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                createAccountButton.setEnabled(true);
                                                Log.d("Image Storage", "onFailure: " + e.getStackTrace());
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            createAccountButton.setEnabled(true);
                            Log.d("Account Error", "onFailure: " + e.getMessage());
                        }
                    });
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(AccountSetup.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                    createAccountButton.setEnabled(true);
                }
            } else {
                progressBar.setVisibility(View.INVISIBLE);
                createAccountButton.setEnabled(true);
                Toast.makeText(AccountSetup.this, "Please supply password", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(AccountSetup.this, "Please select a passport picture", Toast.LENGTH_LONG).show();
            createAccountButton.setEnabled(true);
        }
    }

    private void openGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openGalleryIntent.setType("image/*");
        startActivityForResult(openGalleryIntent, GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            passportUri = data.getData();
            CropImage.activity(passportUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                passportUri = result.getUri();
                passportImageView.setImageURI(passportUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
