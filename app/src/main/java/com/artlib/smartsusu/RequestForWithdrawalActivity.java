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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RequestForWithdrawalActivity extends AppCompatActivity {

    private static double minAmount = 20;
    private Button submitRequestButton;
    private EditText editText;
    private TextView textView;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DocumentReference documentReference;
    private double totalAmount;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_for_withdrawal);

        submitRequestButton = findViewById(R.id.withdrawal_request_button);
        editText = findViewById(R.id.amount_to_withdraw);
        textView = findViewById(R.id.withdrawal_error);

        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);

        utility = new Utility();

        // Database
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);

        //
        submitRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = editText.getText().toString().trim();
                requestForWithdrawal(amount);
            }
        });

    }

    private void requestForWithdrawal(final String amount) {
        if (!TextUtils.isEmpty(amount)) {
            progressBar.setVisibility(View.VISIBLE);
            final String id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Customer_Contributions")
                    .whereEqualTo("customerId", id)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                totalAmount += Double.parseDouble(documentSnapshot.get("amount").toString());
                            }
                            if (totalAmount > minAmount) {
                                double balance = totalAmount - minAmount;
                                double amountLiableForWithdrawal = balance - (0.1 * balance);
                                if (Double.parseDouble(amount) <= amountLiableForWithdrawal) {
                                    documentReference = firebaseFirestore.collection("Withdrawal_Request").document(id);
                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                if (documentSnapshot.exists()) {
                                                    String status = documentSnapshot.getString("status");
                                                    if (!status.equalsIgnoreCase("pending")) {

                                                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                                        DocumentReference doc = firestore.collection("Current_Withdrawal").document(id);
                                                        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot snapshot = task.getResult();
                                                                    String dd = snapshot.getString("date");
                                                                    String[] dds = dd.split("-");
                                                                    String[] current = utility.getCurrentDate().split("-");
                                                                    if (!dds[1].equalsIgnoreCase(current[1])) {
                                                                        recordWithdrawalRequest(amount, id);
                                                                    } else {
                                                                        displayMessage("You have already withdrawn money this month. Please wait for next month", "#FF0000");
                                                                    }
                                                                } else {
                                                                    displayMessage("An error occurred, please try again!", "#FF0000");
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        displayMessage("You have already made a withdrawal request for this month", "#FF0000");
                                                    }
                                                } else {
                                                    // Place fresh withdrawal request
                                                    recordWithdrawalRequest(amount, id);
                                                }
                                            } else {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                displayMessage("An error occurred, please try again", "#FF0000");
                                            }
                                        }
                                    });
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    displayMessage("Cannot place request! Amount requested is higher than can be withdrawn", "#FF0000");
                                }
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                displayMessage("Account balance does not meet the minimum requirements", "#FF0000");
                            }

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            displayMessage("Please you do not have money in your account", "#FF0000");
                        }

                    }

                }
            });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            displayMessage("Please enter an amount to withdraw", "#FF0000");
        }
    }

    private void displayMessage(String message, String color) {
        textView.setText(message);
        textView.setTextColor(Color.parseColor(color));
        textView.setVisibility(View.VISIBLE);
    }

    private void recordWithdrawalRequest(String amount, String id) {
        Map<String, String> data = new HashMap<>();
        String customerId = firebaseAuth.getCurrentUser().getUid();
        data.put("customerId", customerId);
        data.put("amount", amount);
        data.put("date", utility.getCurrentDate());
        data.put("dateTime", utility.getCurrentDateAndTime());
        data.put("status", "pending");

        FirebaseFirestore firebaseFirestore1 = FirebaseFirestore.getInstance();

        firebaseFirestore1.collection("Withdrawal_Request").document(id)
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        displayMessage("Withdrawal request successfully submitted!", "#00FF00");

                    }
                });
    }


}
