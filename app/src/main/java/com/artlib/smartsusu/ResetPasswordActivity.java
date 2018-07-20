package com.artlib.smartsusu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ResetPasswordActivity extends AppCompatActivity {

    PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationStateChangedCallbacks;
    private LinearLayout phoneLayout;
    private LinearLayout codeLayout;
    private LinearLayout passwordLayout;
    private Button sendCodeButton;
    private Button verifyCodeButton;
    private Button changePasswordButton;
    private EditText phoneEditText;
    private EditText codeEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        passwordLayout = findViewById(R.id.phone_area);
        codeLayout = findViewById(R.id.code_area);
        passwordLayout = findViewById(R.id.new_password_layout);
        sendCodeButton = findViewById(R.id.send_verification_code_button);
        verifyCodeButton = findViewById(R.id.send_verify_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        phoneEditText = findViewById(R.id.customer_phone);
        codeEditText = findViewById(R.id.verification_pin);
        passwordEditText = findViewById(R.id.new_password);
        confirmPasswordEditText = findViewById(R.id.new_password_confirmed);
        progressBar = findViewById(R.id.progress);

        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneEditText.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    sendVerificationCode(phone);
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Please provide phone number", Toast.LENGTH_LONG).show();
                }
            }
        });

        verificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                passwordLayout.setVisibility(View.GONE);
                codeLayout.setVisibility(View.VISIBLE);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressBar.setVisibility(View.INVISIBLE);
                sendCodeButton.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "An error occurred, please try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
            }
        };

    }

    private void sendVerificationCode(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        sendCodeButton.setEnabled(false);
        phoneNumber = phoneNumber.replace(phoneNumber.substring(0, 1), "233");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                ResetPasswordActivity.this,
                verificationStateChangedCallbacks);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Some", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("Some", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }


}
