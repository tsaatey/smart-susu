package com.artlib.smartsusu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ContributionViewActivity extends AppCompatActivity {

    private ListView listView;

    private ArrayList<String> contributionDateTimeArrayList;
    private ArrayList<String> amountContributedArrayList;
    private ArrayList<Contribution> contributionArrayList;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private UserLocalDataStore userLocalDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribution_view);

        listView = findViewById(R.id.contribution_history_list_view);

        // Initialize arrayLists
        contributionDateTimeArrayList = new ArrayList<>();
        amountContributedArrayList = new ArrayList<>();
        contributionArrayList = new ArrayList<>();

        // Initialize Firebase database and authentication
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //
        userLocalDataStore = new UserLocalDataStore(this);

        // Set offline capabilities
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        firebaseFirestore.setFirestoreSettings(settings);

        // Pass start and end dates to method
        displayContributionsForSpecifiedPeriod(userLocalDataStore.getStartDate(), userLocalDataStore.getEndDate());

    }

    // Method to display susu contributions for a certain period
    private void displayContributionsForSpecifiedPeriod(String start_date, String end_date) {
        String customerId = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Customer_Contributions")
                .whereEqualTo("customerId", userLocalDataStore.getUserKey())
                .whereGreaterThanOrEqualTo("date", start_date)
                .whereLessThanOrEqualTo("date", end_date)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    amountContributedArrayList.add(documentSnapshot.get("amount").toString());
                                    contributionDateTimeArrayList.add(documentSnapshot.get("dateTime").toString());
                                }

                                for (int i = 0, n = amountContributedArrayList.size(); i < n; i++) {
                                    contributionArrayList.add(new Contribution(contributionDateTimeArrayList.get(i), amountContributedArrayList.get(i)));
                                }

                                ViewContributionAdapter viewContributionAdapter = new ViewContributionAdapter(ContributionViewActivity.this, android.R.layout.simple_list_item_1, contributionArrayList);
                                listView.setAdapter(viewContributionAdapter);

                            } else {
                                Toast.makeText(ContributionViewActivity.this, "No data available", Toast.LENGTH_LONG).show();
                            }


                        } else {
                            Log.d("Contributions error", "onComplete: " + task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Contribution errors", "onFailure: " + e.getMessage());
            }
        });


    }
}
