package com.artlib.smartsusu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth loginFirebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView forgotPasswordTextView;
    private CheckBox rememberMe;

    private UserLocalDataStore userLocalDataStore;


    private Handler mHandler = new Handler();
    private boolean doubleBackToExitPressedOnce = false;
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.sign_up_button);
        progressBar = findViewById(R.id.progress);
        errorTextView = findViewById(R.id.error_text_view);
        rememberMe = findViewById(R.id.remember_me);
        forgotPasswordTextView = findViewById(R.id.forgot_password);

        progressBar.setVisibility(View.INVISIBLE);
        forgotPasswordTextView.setText("");

        loginFirebaseAuth = FirebaseAuth.getInstance();

        userLocalDataStore = new UserLocalDataStore(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                // call login method here
                userLogin(username, password);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent loginIntent = new Intent(MainActivity.this, DashboardActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent passwordResetIntent = new Intent(MainActivity.this, PasswordResetEmailActivity.class);
                startActivity(passwordResetIntent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    @Override
    protected void onStart() {
        super.onStart();
        loginFirebaseAuth.addAuthStateListener(authStateListener);
    }

    private boolean isCredentialsSupplied(String username, String password) {
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void userLogin(String username, String password) {
        if (isCredentialsSupplied(username, password)) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setBackgroundColor(Color.parseColor("#AA00FF"));
            loginButton.setEnabled(false);
            if (isEmailValid(username)) {
                // check if login is successful before redirect to dashboard
                loginFirebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Log in successful. Goto dashboard
                            Intent dashboardIntent = new Intent(MainActivity.this, DashboardActivity.class);
                            dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            progressBar.setVisibility(View.INVISIBLE);
                            loginButton.setEnabled(true);
                            if (rememberMe.isChecked()) {
                                userLocalDataStore.storeRememberMeState(true);
                            }
                            startActivity(dashboardIntent);
                            finish();
                        } else {
                            // Log in failed, display error messages
                            progressBar.setVisibility(View.INVISIBLE);
                            errorTextView.setText(R.string.login_failed);
                            forgotPasswordTextView.setText(R.string.forgot_password);
                            loginButton.setEnabled(true);
                        }
                    }
                });

            } else {
                // Display invalid email error message
                progressBar.setVisibility(View.INVISIBLE);
                errorTextView.setText(R.string.invalid_email);
                loginButton.setEnabled(true);
            }
        } else {
            // Display field required messages
            progressBar.setVisibility(View.INVISIBLE);
            errorTextView.setText(R.string.empty_fields);
            loginButton.setEnabled(true);
        }
    }
}
