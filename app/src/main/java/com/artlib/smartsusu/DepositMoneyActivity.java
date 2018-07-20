package com.artlib.smartsusu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.apps.norris.paywithslydepay.core.PayWithSlydepay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.hubtel.payments.Class.Environment;
import com.hubtel.payments.Exception.HubtelPaymentException;
import com.hubtel.payments.HubtelCheckout;
import com.hubtel.payments.Interfaces.OnPaymentResponse;
import com.hubtel.payments.SessionConfiguration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DepositMoneyActivity extends AppCompatActivity {

    private static int requestCode = 101;
    private EditText amountEditText;
    private Button depositMoneyButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private UserLocalDataStore userLocalDataStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_money);

        amountEditText = findViewById(R.id.amount_to_deposit);
        depositMoneyButton = findViewById(R.id.deposit_money_button);
        progressBar = findViewById(R.id.progress);

        progressBar.setVisibility(View.INVISIBLE);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        userLocalDataStore = new UserLocalDataStore(this);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);

        depositMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountt = amountEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(amountt)) {
                    sendMobileMoney();
                } else {

                }
            }
        });

    }

    private void sendMobileMoney() {
        String prefix = "233";
        String itemName = "Susu";
        double amount = Double.parseDouble(amountEditText.getText().toString().trim());
        String description = "Smart Susu Contribution";
        String customerName = userLocalDataStore.getCustomerFirstName() + " " + userLocalDataStore.getCustomerLastName();
        String customerEmail = userLocalDataStore.getCustomerEmailAddress();
        String orderCode = "SMSMOMO";
        String phoneNumber = prefix.concat(userLocalDataStore.getCustomerPhoneNumber().substring(1));

        //Performing transaction with provided ui
        PayWithSlydepay.Pay(DepositMoneyActivity.this, itemName, amount, description, customerName, customerEmail, orderCode, phoneNumber, requestCode);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == requestCode) {
            switch (resultCode) {
                case RESULT_OK:
                    //Payment was successful
                    depositMoney(amountEditText.getText().toString().trim());
                    amountEditText.setText("");
                    break;
                case RESULT_CANCELED:
                    //Payment failed
                    //Toast.makeText(DepositMoneyActivity.this, "Deposit failed", Toast.LENGTH_LONG).show();
                    break;
                case RESULT_FIRST_USER:
                    //Payment was cancelled by user
                    //Toast.makeText(DepositMoneyActivity.this, "Deposit cancelled", Toast.LENGTH_LONG).show();
                    break;
            }


        }

    }


    private void makeAPiCall(String contributionAmount) {
        final float amount = Float.parseFloat(contributionAmount);

        try {
            SessionConfiguration sessionConfiguration = new SessionConfiguration()
                    .Builder().setClientId("ygjlielo")
                    .setSecretKey("ttkttmps")
                    .setEnvironment(Environment.LIVE_MODE)
                    .build();
            HubtelCheckout hubtelPayments = new HubtelCheckout(sessionConfiguration);
            hubtelPayments.setPaymentDetails(amount, "Smart Susu Contribution");
            hubtelPayments.Pay(this);
            hubtelPayments.setOnPaymentCallback(new OnPaymentResponse() {
                @Override
                public void onFailed(String token, String reason) {
                    Toast.makeText(DepositMoneyActivity.this, reason, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled() {

                }

                @Override
                public void onSuccessful(String token) {
                    depositMoney(Float.toString(amount));
                }
            });
        } catch (HubtelPaymentException e) {
            e.printStackTrace();
        }
    }

    private void depositMoney(String amount) {
        Map<String, String> data = new HashMap<>();
        String customerId = firebaseAuth.getCurrentUser().getUid();
        data.put("customerId", customerId);
        data.put("amount", amount);
        data.put("date", getCurrentDate());
        data.put("dateTime", getCurrentDateAndTime());

        firebaseFirestore.collection("Customer_Contributions")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // data saved
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private String getCurrentDate() {
        final java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("dd-MM-yyy");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        final String date = simpleDateFormat.format(new Date()).toString();
        return date;
    }

    private String getCurrentDateAndTime() {
        final java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("dd-MM-yyy HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        final String date = simpleDateFormat.format(new Date()).toString();
        return date;
    }

    private String retrieveCarrierCode(String carrier) {
        Map<String, String> carrierMap = new HashMap<>();
        carrierMap.put("MTN", "mtn-gh");
        carrierMap.put("Vodafone", "vodafone-gh");
        carrierMap.put("Tigo", "tigo-gh");
        carrierMap.put("Airtel", "airtel-gh");
        carrierMap.put("AirtelTigo", "airtel-gh");
        return carrierMap.get(carrier);
    }

    private String getPhoneCarrier() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String carrier = telephonyManager.getNetworkOperatorName();
        return carrier;
    }
}
