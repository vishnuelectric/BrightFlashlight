package com.vishnu.brightflashlight;

import android.app.Application;

import com.onesignal.OneSignal;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG);
        OneSignal.startInit(this).init();
    }
}
