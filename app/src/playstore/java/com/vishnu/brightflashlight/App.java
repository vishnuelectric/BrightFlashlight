package com.vishnu.brightflashlight;

import android.app.Application;
import android.widget.Toast;

import com.onesignal.OneSignal;
import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG);
        OneSignal.startInit(this).setNotificationOpenedHandler(new OneSignal.NotificationOpenedHandler() {
            @Override
            public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {

                if(additionalData.optString("rate").equalsIgnoreCase("true"))
                {
                    Toast.makeText(getApplicationContext(),additionalData.optString("message"),Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(),additionalData.optString("message"),Toast.LENGTH_LONG).show();
                }

                if(additionalData.optString("update").equalsIgnoreCase("true"))
                {
                    Toast.makeText(getApplicationContext(),additionalData.optString("message"),Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(),additionalData.optString("message"),Toast.LENGTH_LONG).show();
                }
            }
        }).init();
        OneSignal.enableInAppAlertNotification(true);
        OneSignal.enableNotificationsWhenActive(true);
        OneSignal.setSubscription(true);

    }
}
