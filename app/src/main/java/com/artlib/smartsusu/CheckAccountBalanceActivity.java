package com.artlib.smartsusu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;

public class CheckAccountBalanceActivity extends AppCompatActivity {

    private EditText startDateEditText;
    private EditText endDateEditText;
    private Button viewHistoryButton;
    private TextView amountTextView;


    private LinearLayout checkBalanceLayout;
    private LinearLayout viewHistoryLayout;
    private LinearLayout balanceLayout;
    private LinearLayout historyLayout;
    private LinearLayout controlLayout;

    private DatePickerDialog datePickerDialog;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private double totalContributions;

    private UserLocalDataStore userLocalDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_account_balance);

        startDateEditText = findViewById(R.id.start_date_editText);
        endDateEditText = findViewById(R.id.end_date_editText);
        checkBalanceLayout = findViewById(R.id.total_balance);
        viewHistoryLayout = findViewById(R.id.contribution_history);
        balanceLayout = findViewById(R.id.balance_layout);
        historyLayout = findViewById(R.id.history_layout);
        controlLayout = findViewById(R.id.control_layout);
        viewHistoryButton = findViewById(R.id.view_history_button);
        amountTextView = findViewById(R.id.amountTextView);

        startDateEditText.setFocusable(false);
        startDateEditText.setClickable(true);
        startDateEditText.setLongClickable(false);

        endDateEditText.setFocusable(false);
        endDateEditText.setClickable(true);
        endDateEditText.setLongClickable(false);

        // Initialize Firebase database and authentication
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //
        userLocalDataStore = new UserLocalDataStore(this);

        // Set offline capabilities
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);

        // Set onclickListener for check balance layout
        checkBalanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balanceLayout.setVisibility(View.VISIBLE);
                historyLayout.setVisibility(View.GONE);
                displayTotalContributions();
            }
        });

        // Set onclickListener for view contribution layout
        viewHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balanceLayout.setVisibility(View.GONE);
                historyLayout.setVisibility(View.VISIBLE);
            }
        });

        viewHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyLayout.setVisibility(View.GONE);
                balanceLayout.setVisibility(View.GONE);
                getSpecifiedDates();
            }
        });

        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(CheckAccountBalanceActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        startDateEditText.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);

                datePickerDialog.show();
            }
        });

        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(CheckAccountBalanceActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        endDateEditText.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);

                datePickerDialog.show();
            }

        });
    }

    private String getFormattedDate(String date) {
        String[] dateArray = date.split("-");
        String a = dateArray[0];
        String b = dateArray[1];
        String c = dateArray[2];

        int d = Integer.parseInt(a);
        if (d < 10) {
            a = "0".concat(a);
        }

        int e = Integer.parseInt(b);
        if (e < 10) {
            b = "0".concat(b);
        }
        String formattedDate = a.concat("-").concat(b).concat("-").concat(c);
        return formattedDate;

    }


    private void getSpecifiedDates() {
        String start_date = startDateEditText.getText().toString().trim();
        String end_date = endDateEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(start_date) && !TextUtils.isEmpty(end_date)) {
            start_date = getFormattedDate(start_date);
            end_date = getFormattedDate(end_date);
            userLocalDataStore.storeStartDate(start_date);
            userLocalDataStore.storeEndDate(end_date);

            Intent intent = new Intent(CheckAccountBalanceActivity.this, ContributionViewActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(CheckAccountBalanceActivity.this, "Please select start and end dates!", Toast.LENGTH_LONG).show();
        }
    }

    private void displayTotalContributions() {
        String id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Customer_Contributions")
                .whereEqualTo("customerId", id)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            totalContributions += Double.parseDouble(documentSnapshot.get("amount").toString());
                        }
                        amountTextView.setText("GHS " + totalContributions);
                    }

                }

            }
        });
    }


}
