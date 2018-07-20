package com.artlib.smartsusu;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class AccountSettingsActivity extends AppCompatActivity {

    private CardView logOutCardView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private UserLocalDataStore userLocalDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        logOutCardView = findViewById(R.id.logout_card_view);

        firebaseAuth = FirebaseAuth.getInstance();
        userLocalDataStore = new UserLocalDataStore(this);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(AccountSettingsActivity.this, MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        logOutCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void logout() {
        firebaseAuth.signOut();
        userLocalDataStore.clearUserData();
    }
}


