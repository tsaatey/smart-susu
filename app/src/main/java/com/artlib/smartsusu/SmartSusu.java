package com.artlib.smartsusu;

import android.content.res.Configuration;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.apps.norris.paywithslydepay.core.SlydepayPayment;

public class SmartSusu extends MultiDexApplication {
    @Override
    public void onCreate() {
        MultiDex.install(getApplicationContext());
        new SlydepayPayment(getApplicationContext()).initCredentials("teddyxray@gmail.com", "1490780531304");
        super.onCreate();

    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
