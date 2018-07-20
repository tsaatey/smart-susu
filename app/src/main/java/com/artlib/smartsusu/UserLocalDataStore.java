package com.artlib.smartsusu;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ARTLIB on 06/07/2017.
 */

public class UserLocalDataStore {

    public static final String SP_NAME = "SmartSusu";
    SharedPreferences userLocalDataStore;

    public UserLocalDataStore(Context context) {
        userLocalDataStore = context.getSharedPreferences(SP_NAME, 0);
    }

    // Method to clear user data from the system when logged out
    public void clearUserData() {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.clear();
        editor.commit();
    }

    public void storeCustomerFirstName(String firstname) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("firstname", firstname);
        editor.commit();
    }

    public String getCustomerFirstName() {
        return userLocalDataStore.getString("firstname", "");
    }

    public void storeCustomerLastName(String lastname) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("lastname", lastname);
        editor.commit();
    }

    public String getCustomerLastName() {
        return userLocalDataStore.getString("lastname", "");
    }

    public void storeCustomerDateOfBirth(String dateofbirth) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("dateofbirth", dateofbirth);
        editor.commit();
    }

    public String getCustomerDateOfBirth() {
        return userLocalDataStore.getString("dateofbirth", "");
    }

    public void storeCustomerHouseNumber(String housenumber) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("housenumber", housenumber);
        editor.commit();
    }

    public String getCustomerHouseNumber() {
        return userLocalDataStore.getString("housenumber", "");
    }

    public void storeCustomerVoterId(String voterid) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("voterid", voterid);
        editor.commit();
    }

    public String getCustomerVoterId() {
        return userLocalDataStore.getString("voterid", "");
    }

    public void storeCustomerEmailAddress(String email) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("email", email);
        editor.commit();
    }

    public String getCustomerEmailAddress() {
        return userLocalDataStore.getString("email", "");
    }

    public void storeCustomerPhoneNumber(String phone) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("phone", phone);
        editor.commit();
    }

    public String getCustomerPhoneNumber() {
        return userLocalDataStore.getString("phone", "");
    }

    public void storeRememberMeState(boolean checked) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putBoolean("rememberMe", checked);
        editor.commit();
    }

    public boolean rememberMeIsChecked() {
        return userLocalDataStore.getBoolean("rememberMe", false);
    }

    public void storeStartDate(String date) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("start_date", date);
        editor.commit();
    }

    public String getStartDate() {
        return userLocalDataStore.getString("start_date", "");
    }

    public void storeEndDate(String endDate) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("end_date", endDate);
        editor.commit();
    }

    public String getEndDate() {
        return userLocalDataStore.getString("end_date", "");
    }

    public void storeUserKey(String key) {
        SharedPreferences.Editor editor = userLocalDataStore.edit();
        editor.putString("key", key);
        editor.commit();
    }

    public String getUserKey() {
        return userLocalDataStore.getString("key", "");
    }

}
