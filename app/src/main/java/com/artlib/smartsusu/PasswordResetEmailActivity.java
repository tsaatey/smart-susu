package com.artlib.smartsusu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetEmailActivity extends AppCompatActivity {

    private EditText passwordResetEmailEditText;
    private Button passwordResetEmailButton;
    private ProgressBar progressBar;
    private TextView notificationTextView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset_email);

        passwordResetEmailButton = findViewById(R.id.send_password_reset_email_button);
        passwordResetEmailEditText = findViewById(R.id.password_reset_email_edit_text);
        progressBar = findViewById(R.id.progress);
        notificationTextView = findViewById(R.id.notification);

        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();

        passwordResetEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = passwordResetEmailEditText.getText().toString();
                if (!TextUtils.isEmpty(email)) {
                    sendPasswordResetEmail(email);
                } else {
                    notificationTextView.setText(R.string.empty_field);
                    notificationTextView.setTextColor(Color.RED);
                }
            }
        });


    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        passwordResetEmailButton.setEnabled(false);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            notificationTextView.setText(R.string.email_sent);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                notificationTextView.setText(R.string.email_error);
                notificationTextView.setTextColor(Color.RED);
                progressBar.setVisibility(View.INVISIBLE);
                passwordResetEmailButton.setEnabled(true);
            }
        });
    }
}
