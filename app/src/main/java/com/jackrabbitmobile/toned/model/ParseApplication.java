package com.jackrabbitmobile.toned.model;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    //TODO add the authentication for Parse to an application class
    private final String PARSE_CLIENT_KEY = "ft0E8PiILonckmUwZYEwrptORe23S5akQZoKYZmC";
    private final String PARSE_APPLICATION_ID = "NgAKfAcsrDpSpbWPfsgw8i2mfT3godoqgMRSOcrn";

    @Override
    public void onCreate(){
        super.onCreate();

        ParseObject.registerSubclass(Workout.class);
        ParseObject.registerSubclass(Exercise.class);
        //Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(getApplicationContext(), PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

    }
}
