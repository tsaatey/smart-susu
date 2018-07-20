package com.artlib.smartsusu;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView imageView;
    private CardView depositMoneyCardView;
    private CardView checkBalanceCardView;
    private CardView joinContributionGroupCardView;
    private CardView requestForWithdrawalCardView;
    private CardView accountSettingsCardView;
    private CircleImageView appLogoView;

    private UserLocalDataStore userLocalDataStore;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;

    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;

    private Utility utility;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        imageView = findViewById(R.id.user_image);
        appLogoView = findViewById(R.id.app_log);
        depositMoneyCardView = findViewById(R.id.depositMoney);
        checkBalanceCardView = findViewById(R.id.checkBalance);
        joinContributionGroupCardView = findViewById(R.id.joinContributionGroup);
        requestForWithdrawalCardView = findViewById(R.id.requestForWithdrawal);
        accountSettingsCardView = findViewById(R.id.accountSettings);

        userLocalDataStore = new UserLocalDataStore(this);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);

        // Set OnclickListener for each card view
        depositMoneyCardView.setOnClickListener(this);
        checkBalanceCardView.setOnClickListener(this);
        joinContributionGroupCardView.setOnClickListener(this);
        requestForWithdrawalCardView.setOnClickListener(this);
        accountSettingsCardView.setOnClickListener(this);

        utility = new Utility();

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (Math.abs(scrollRange + verticalOffset) < 10) {
                    collapsingToolbarLayout.setTitle(new Intent().getStringExtra("firstname"));
                    appLogoView.setImageResource(R.mipmap.smart_susu);
                    imageView.setImageDrawable(null);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    appLogoView.setImageDrawable(null);
                    String customerId = firebaseAuth.getCurrentUser().getUid();
                    getCustomerImage(customerId);
                    userLocalDataStore.storeUserKey(customerId);
                    isShow = false;
                }
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(DashboardActivity.this, MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (!userLocalDataStore.rememberMeIsChecked()) {
            logout();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        checkAndSendMoney();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.depositMoney:
                intent = new Intent(this, DepositMoneyActivity.class);
                startActivity(intent);
                break;
            case R.id.checkBalance:
                intent = new Intent(this, CheckAccountBalanceActivity.class);
                startActivity(intent);
                break;
            case R.id.joinContributionGroup:
                intent = new Intent(this, JoinContributionGroupActivity.class);
                startActivity(intent);
                break;
            case R.id.requestForWithdrawal:
                intent = new Intent(this, RequestForWithdrawalActivity.class);
                startActivity(intent);
                break;
            case R.id.accountSettings:
                intent = new Intent(this, AccountSettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void getCustomerImage(String customerId) {
        documentReference = firebaseFirestore.collection("Customers").document(customerId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String image = documentSnapshot.get("passport").toString();
                        userLocalDataStore.storeCustomerFirstName(documentSnapshot.get("firstname").toString());
                        userLocalDataStore.storeCustomerLastName(documentSnapshot.get("lastname").toString());
                        userLocalDataStore.storeCustomerPhoneNumber(documentSnapshot.get("phone").toString());
                        userLocalDataStore.storeCustomerEmailAddress(documentSnapshot.get("email").toString());
                        Picasso.get().load(image).into(imageView);
                    } else {
                        imageView.setImageResource(R.mipmap.ic_account_circle);
                    }
                }
            }
        });
    }

    private void logout() {
        firebaseAuth.signOut();
        userLocalDataStore.clearUserData();
    }

    private void checkAndSendMoney() {
        final String id = firebaseAuth.getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference reference = firestore.collection("Withdrawal_Request").document(id);
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String requestDateTime = documentSnapshot.getString("dateTime");
                        String requestedAmount = documentSnapshot.getString("amount");
                        String currentDateTime = utility.getCurrentDateAndTime();
                        long days = utility.getDays(requestDateTime, currentDateTime);
                        if (days >= 3) {
                            /*
                                TODO: Make API call to withdraw money @param: requestedAmount, from merchant account and send to customer's mobile money wallet
                             */

                            // TODO: Record withdrawal date when successful
                            recordMonthMoneyWasWithdrawn(utility.getCurrentDate(), id, requestedAmount);

                        }
                    }
                }
            }
        });
    }

    private void recordMonthMoneyWasWithdrawn(String date, final String id, final String amount) {
        FirebaseFirestore firebaseFirestore1 = FirebaseFirestore.getInstance();
        Map<String, String> data = new HashMap<>();
        data.put("date", date);
        firebaseFirestore1.collection("Current_Withdrawal").document(id)
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        recordWithdrawalRequest(amount, id);
                    }
                });
    }

    private void recordWithdrawalRequest(String amount, String id) {
        Map<String, String> data = new HashMap<>();
        String customerId = firebaseAuth.getCurrentUser().getUid();
        data.put("customerId", customerId);
        data.put("amount", amount);
        data.put("date", utility.getCurrentDate());
        data.put("dateTime", utility.getCurrentDateAndTime());
        data.put("status", "success");

        FirebaseFirestore firebaseFirestore1 = FirebaseFirestore.getInstance();

        firebaseFirestore1.collection("Withdrawal_Request").document(id)
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

}
