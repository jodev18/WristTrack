package com.khsoftsolutions.wristtrack.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by myxroft2 on 8/12/17.
 */

public class TrackingService extends Service {

    public TrackingService(){

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //@Override
    //public
}
