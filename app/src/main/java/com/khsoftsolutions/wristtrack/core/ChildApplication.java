package com.khsoftsolutions.wristtrack.core;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.parse.Parse;

/**
 * Created by myxroft2 on 9/10/17.
 */

public class ChildApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("cIVPmNYYcqxCkohWKrrNh6PVJ5vnNjCVljtVPseC")
                .server("https://parseapi.back4app.com/")
                .clientKey("fVnKyvPWjtfxK8qJnbX9N6ioz7oWpcy0mlNAlZof")
                .build()
        );
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
