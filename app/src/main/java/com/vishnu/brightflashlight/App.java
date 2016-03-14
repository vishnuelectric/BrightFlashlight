package com.vishnu.brightflashlight;

import android.app.Application;

import com.onesignal.OneSignal;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG);
        OneSignal.startInit(this).setAutoPromptLocation(true).init();
        OneSignal.promptLocation();
    }
}
