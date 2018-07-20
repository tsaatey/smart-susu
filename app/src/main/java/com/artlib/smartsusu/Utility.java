package com.artlib.smartsusu;

import android.annotation.SuppressLint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

public class Utility {

    public Utility() {
    }

    @SuppressLint("NewApi")
    public long getDays(String firstDate, String secondDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
        LocalDateTime fDate = LocalDateTime.parse(firstDate, formatter);
        LocalDateTime sDate = LocalDateTime.parse(secondDate, formatter);
        return ChronoUnit.DAYS.between(fDate, sDate);
    }

    public String getCurrentDate() {
        final java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("dd-MM-yyy");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        final String date = simpleDateFormat.format(new Date()).toString();
        return date;
    }

    public String getCurrentDateAndTime() {
        final java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("dd-MM-yyy HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        final String date = simpleDateFormat.format(new Date()).toString();
        return date;
    }
}
