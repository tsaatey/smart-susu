package com.artlib.smartsusu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private DatePickerDialog datePickerDialog;
    private EditText dobEditText;
    private Calendar calendar;
    private int year;
    private int month;
    private int day;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText dateOfBirthEditText;
    private EditText houseNumberEditText;
    private EditText voterIdEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button registerButton;

    private UserLocalDataStore userLocalDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dobEditText = findViewById(R.id.dateofbirth);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // variable definitions
        firstNameEditText = findViewById(R.id.firstname);
        lastNameEditText = findViewById(R.id.lastname);
        dateOfBirthEditText = findViewById(R.id.dateofbirth);
        houseNumberEditText = findViewById(R.id.housenumber);
        voterIdEditText = findViewById(R.id.voterid);
        emailEditText = findViewById(R.id.email);
        phoneEditText = findViewById(R.id.phone);
        registerButton = findViewById(R.id.register_button);

        dobEditText.setKeyListener(null);

        userLocalDataStore = new UserLocalDataStore(this);

        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = DatePickerDialog.newInstance(RegisterActivity.this, year, month, day);
                datePickerDialog.setThemeDark(false);
                datePickerDialog.showYearPickerFirst(false);
                datePickerDialog.setAccentColor(Color.parseColor("#009688"));
                datePickerDialog.setTitle("Please Pick Your Date of Birth");
                datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllCustomerDetailsAndStore();
            }
        });
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth + "-" + monthOfYear + "-" + year;
        dobEditText.setText(date);

    }

    public boolean validateEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void getAllCustomerDetailsAndStore() {
        String firstname = firstNameEditText.getText().toString().trim();
        String lastname = lastNameEditText.getText().toString().trim();
        String dateofbirth = dateOfBirthEditText.getText().toString().trim();
        String housenumber = houseNumberEditText.getText().toString().trim();
        String voterid = voterIdEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // check if variables are not null
        if (!TextUtils.isEmpty(firstname)
                && !TextUtils.isEmpty(lastname)
                && !TextUtils.isEmpty(dateofbirth)
                && !TextUtils.isEmpty(housenumber)
                && !TextUtils.isEmpty(voterid)
                && !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(phone)) {

            userLocalDataStore.storeCustomerFirstName(firstname);
            userLocalDataStore.storeCustomerLastName(lastname);
            userLocalDataStore.storeCustomerDateOfBirth(dateofbirth);
            userLocalDataStore.storeCustomerHouseNumber(housenumber);
            userLocalDataStore.storeCustomerVoterId(voterid);
            userLocalDataStore.storeCustomerEmailAddress(email);
            userLocalDataStore.storeCustomerPhoneNumber(phone);

            Intent accountIntent = new Intent(RegisterActivity.this, AccountSetup.class);
            startActivity(accountIntent);
        } else {
            // raise field validation error
            Toast.makeText(RegisterActivity.this, "Please all fields are required!", Toast.LENGTH_LONG).show();
        }
    }

}
