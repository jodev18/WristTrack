package com.khsoftsolutions.wristtrack.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.location.LocationListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.khsoftsolutions.wristtrack.R;
import com.khsoftsolutions.wristtrack.core.Globals;
import com.khsoftsolutions.wristtrack.database.KidManager;
import com.khsoftsolutions.wristtrack.database.ParentManager;
import com.khsoftsolutions.wristtrack.objects.ChildObject;
import com.khsoftsolutions.wristtrack.objects.ParentObject;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import eu.chainfire.libsuperuser.Shell;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Splash extends AppCompatActivity {

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    /** USER-DEFINED VARIABLES */
    private int back_counter = 0;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    //private SmoothBluetooth mSmoothBluetooth;
    //private Bluetooth ble;

    private ProgressDialog prg;

    private Handler h;

    //private SimpleLocation simLoc;

    @BindView(R.id.tvLong) TextView longitude;
    @BindView(R.id.tvLat) TextView latitude;
    @BindView(R.id.tvCityName) TextView city;
    @BindView(R.id.tvAltitude) TextView altitude;
    @BindView(R.id.tvSpeed) TextView speed;
    @BindView(R.id.tvBearing) TextView bearing;

    AlertDialog npInfo;
    BluetoothAdapter bAdapt;

    Double currLat;
    Double currLong;

    private Integer updateIntervalMin = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        ButterKnife.bind(Splash.this);

        h = new Handler(this.getMainLooper());

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.sc_layout);

        bAdapt = BluetoothAdapter.getDefaultAdapter();
        initBlueListenerThread();

        prg = new ProgressDialog(Splash.this);

       if(infoCheck()){
           initialSetup();
           updateLocThread();
       }
    }

    private void uploadLocation(){

        KidManager kMan = new KidManager(Splash.this);
        ChildObject childObject = kMan.getChildInfo();

        ParentManager pMan = new ParentManager(Splash.this);
        final ParentObject pObj = pMan.getParentInfo();

        if(childObject != null){
            final ParseObject parseObject = new ParseObject(Globals.TrackInformation.OBJ_NAME);

            parseObject.put(Globals.TrackInformation.CHILD_OBJ_ID,childObject.OBJECT_ID);

            if(currLat != null & currLong != null){

                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
                String formattedDate = sdf.format(date);
                ParseGeoPoint geoPoint = new ParseGeoPoint(currLat,currLong);

                parseObject.put(Globals.TrackInformation.GEO_POINT,geoPoint);
                parseObject.put(Globals.TrackInformation.TRACK_TIMESTAMP,formattedDate);

                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toasty.success(Splash.this,"Uploaded current " +
                                "coordinates to server.",Toast.LENGTH_LONG).show();

                        if(e != null){

                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(pObj.PARENT_PHONE_NUMBER, null, "Latitude: " + currLat + "\n Longitude: " + currLong, null, null);

                            Toasty.info(Splash.this,"Current " +
                                    "coordinates will be later uploaded to server.",Toast.LENGTH_LONG).show();
                            parseObject.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Toasty.success(Splash.this,"Coordinates updated eventually.",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                Toasty.success(Splash.this,"Location updated",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initialSetup(){

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.findViewById(R.id.txtClock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        findViewById(R.id.btnMenu).setOnTouchListener(mDelayHideTouchListener);

        setButtonListener();

        boolean isRoot = checkRoot();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Splash.this);

        updateIntervalMin = sp.getInt(Globals.INTERVAL_PREFERENCE_KEY, 0);

        if(updateIntervalMin == 0){
            Toasty.warning(Splash.this,"There was no set minute interval " +
                    "so the default (5 min) update interval will be set.",Toast.LENGTH_LONG).show();
            updateIntervalMin = 5;
        }
        else{
            Toasty.info(Splash.this,"Interval set: "
                    + ((Integer)(updateIntervalMin / 60)).toString() + " mins", Toast.LENGTH_LONG).show();
        }


        if(!isRoot){
            notifyNoRoot();
            Toast.makeText(this, "The app has been " +
                    "denied root permissions.", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Root permissions granted.", Toast.LENGTH_SHORT).show();

            forceEnableGPS();
            //setBluetooth(true);
            setWifi(true);

            //If android version less or equal to version 19
            //Prevents status bar expansion for kitkat below
            if(Build.VERSION.SDK_INT <= 19){
                preventStatusBarExpansion(Splash.this);
            }
        }
    }

    private boolean infoCheck(){

        AlertDialog.Builder npInfoB = new AlertDialog.Builder(Splash.this);

        ParentManager pman = new ParentManager(Splash.this);
        KidManager kman = new KidManager(Splash.this);

        ChildObject childObject = kman.getChildInfo();

        ParentObject pObj = pman.getParentInfo();

        if(pObj == null && childObject == null){
            npInfoB.setTitle("Setup required");
            npInfoB.setMessage("Please connect to ParentTracker app to setup.");

            npInfoB.setCancelable(false);
            npInfo = npInfoB.create();
            npInfo.show();

            return false;
        }
        else{
            return true;
        }
    }

    private void initBlueListenerThread(){

        Toasty.info(Splash.this,"Waiting for ParentTracker to connect.",Toast.LENGTH_LONG).show();

        if(Build.VERSION.SDK_INT >= 17){
            Dexter.withActivity(Splash.this)
                    .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.CALL_PHONE,
                                        Manifest.permission.SEND_SMS)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {

                            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                            startActivityForResult(intent, 85);

                            if(!blueListener.isAlive()){
                                blueListener.start();
                            }

                            initLocationListener();
                            updateLocThread();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                        }
                    }).check();
        }
    }

    /**
     * Thread for listening from incoming connections.
     */
    private Thread blueListener = new Thread(new Runnable() {
        @Override
        public void run() {

            while (true) {

                try{
                    BluetoothServerSocket sock =
                            bAdapt.listenUsingRfcommWithServiceRecord("MY_APP",MY_UUID);

                    BluetoothSocket ss = sock.accept();

                    if(ss != null){
                        InputStream inp = ss.getInputStream();

                        final byte[] buff = new byte[1024];
                        int nnBytes = inp.read(buff);

                        final String parentParam = new String(buff);


                        if(parentParam.length() > 3){

                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    //Toasty.info(Splash.this,parentParam,Toast.LENGTH_LONG).show();
                                    Log.d("RECEIVED_BLUE",parentParam);
                                }
                            });

                            String[] data = parentParam.trim().split("\\|");

                            final String setInterval = data[2];

                            SharedPreferences spref = PreferenceManager
                                    .getDefaultSharedPreferences(Splash.this);

                            if(setInterval.length() > 0){
                                try{
                                    Integer minInterval = Integer.parseInt(setInterval);

                                    SharedPreferences.Editor e = spref.edit();

                                    e.putInt(Globals.INTERVAL_PREFERENCE_KEY,minInterval);

                                    if(e.commit()){
                                        h.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toasty.success(Splash.this,"Time interval set: " + setInterval,Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                    else{
                                        h.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toasty.error(Splash.this,"Interval time failed to set.",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                                catch (NumberFormatException numEx){

                                    Toasty.warning(Splash.this,"There was some problems encountered" +
                                            " while handling some data.",Toast.LENGTH_LONG).show();

                                    Log.e("INTERVAL_PARSING","Error encountered while handling" +
                                            " the interval duration value. Default interval " +
                                            "set.");
                                }
                            }

                            //Log.d("PARENT_DATA",data[0]);
                            //Log.d("CHILD_DATA",data[1]);

                            //Parse string to JSON
                            Gson gson = new GsonBuilder().setLenient().create();

                            final ParentObject parentObject = gson.fromJson(data[0],ParentObject.class);
                            final ChildObject childObject = gson.fromJson(data[1],ChildObject.class);

                            ParentManager pMan = new ParentManager(Splash.this);
                            long pstat = pMan.insertParent(parentObject);
                            pMan.cleanUp();

                            KidManager kMan = new KidManager(Splash.this);
                            long kstat = kMan.addChildInfo(childObject);

                            if(pstat > 0 && kstat > 0){
                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.success(Splash.this, "Parent information saved!", Toast.LENGTH_SHORT).show();
                                        updateLocThread();
                                        if(npInfo != null){
                                            npInfo.dismiss();
                                        }

                                        AlertDialog.Builder infoLoaded = new AlertDialog.Builder(Splash.this);
                                        infoLoaded.setTitle("Setup Finished");
                                        infoLoaded.setMessage("Parent Name: " + parentObject.PARENT_FIRST_NAME
                                                + " " + parentObject.PARENT_LAST_NAME + "\n"
                                                + "Contact Number: " + parentObject.PARENT_PHONE_NUMBER
                                                + "\n" + "Child Name: " + childObject.CHILD_FIRST_NAME + " " + childObject.CHILD_LAST_NAME);

                                        infoLoaded.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });

                                        infoLoaded.create().show();
                                    }
                                });
                            }
                            else{
                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(Splash.this, "Parent information failed to save. Please try again. ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            /*
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Splash.this, "String: " + parentParam, Toast.LENGTH_SHORT).show();
                                }
                            });*/
                        }
                        else{
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(Splash.this, "Incomplete response received.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                }
                catch(final IOException ioex){
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("ERROR_BLUETOOTH",ioex.getMessage());
                            //Toast.makeText(Splash
                               //     .this, "Error: "
                                 //   + ioex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    });

    private void initLocationListener(){
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                doSomethingWithLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                //Toast.makeText(Splash.this, "GPS Status Changed.", Toast.LENGTH_SHORT).show();
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(Splash.this, "GPS Enabled.", Toast.LENGTH_SHORT).show();
                initLocationListener();
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(Splash.this, "GPS Disabled.", Toast.LENGTH_SHORT).show();
                showActivateGPSDialog();
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if(Build.VERSION.SDK_INT > 19){
            if(checkLocationPermission()){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1.0f,
                        locationListener);
            }
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1.0f,
                    locationListener);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void setButtonListener(){
        Button menu =(Button)findViewById(R.id.btnMenu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currLat != null && currLong != null){
                    startActivity(new Intent().setClass(getApplicationContext(),WristTrackMenu.class)
                            .putExtra(Globals.COORDINATES_LAT_KEY,currLat.toString())
                            .putExtra(Globals.COORDINATES_LONG_LEY,currLong.toString()));
                }
                else{
                    Toasty.warning(Splash.this,"Please wait till current location is retrieved.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void showActivateGPSDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(Splash.this);
        dialog.setTitle("GPS Disabled");
        dialog.setMessage("GPS currently is disabled. " +
                "Please enable GPS in the settings.");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                //get gps
            }
        });
        dialog.show();
    }

    private boolean checkRoot(){
        return Shell.SU.available();
    }

    private void notifyNoRoot(){

        AlertDialog.Builder nr = new AlertDialog.Builder(Splash.this);

        nr.setTitle("Root Permissions");

        nr.setMessage("Root permissions are required for all " +
                "of the features of this app to work. Please allow root permission.");

        nr.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        nr.setCancelable(false);

        nr.create().show();
    }

    /**
     * Checks for internet connection.
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * As root, enables root programmatically.
     */
    private void forceEnableGPS(){
        try{
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("pm grant " + getApplicationContext()
                    .getPackageName()+" android.permission.WRITE_SECURE_SETTINGS \n");
            os.writeBytes("exit\n");
            os.flush();

            ContentResolver localContentResolver = Splash.this.getContentResolver();
            Settings.Secure.setLocationProviderEnabled(localContentResolver,
                    android.location.LocationManager.GPS_PROVIDER,true);
        }
        catch(IOException iex){
            Toast.makeText(this, "There was a problem activating GPS.", Toast.LENGTH_SHORT).show();
            Log.e("FORCE GPS","ERROR ACTIVATING GPS--IOEXCEPTION");
        }


    }

    /**
     * Toggles wifi activation.
     * @param status
     */
    private void setWifi(boolean status){
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(status);
    }

    public static void preventStatusBarExpansion(Context context) {
        
        WindowManager manager = ((WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        Activity activity = (Activity)context;
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|

                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");

        int result = 0;
        if (resId > 0) {
            result = activity.getResources().getDimensionPixelSize(resId);
        }

        localLayoutParams.height = result;

        localLayoutParams.format = PixelFormat.TRANSPARENT;

        customViewGroup view = new customViewGroup(context);

        manager.addView(view, localLayoutParams);
    }

    public static class customViewGroup extends ViewGroup {

        public customViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v("customViewGroup", "**********Intercepted");
            return true;
        }
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Dialogs
     */

    /**
     * Displays a dialog for asking help.
     * Sends a text message then calls that number.
     */
    private void showHelpConfirm(){

        AlertDialog.Builder ab = new AlertDialog.Builder(Splash.this);

        ab.setTitle("Help");
        ab.setMessage("Call parents for help?");

        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Dial phone number
                //TODO ROOT FUNCTION CALL

                final ParentManager pMan = new ParentManager(Splash.this);

                final String phoneNumber = pMan.getParentInfo().PARENT_PHONE_NUMBER;

                final Intent intent = new Intent(Intent.ACTION_CALL);

                KidManager kid = new KidManager(Splash.this);
                final ChildObject child = kid.getChildInfo();

                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent); //done in the early parts

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SmsManager smsManager = SmsManager.getDefault();

                        if(child != null){
                            smsManager.sendTextMessage(pMan.getParentInfo().PARENT_PHONE_NUMBER, null, "Ur " + child.CHILD_FIRST_NAME +
                                    " called for help. http://www.google.com/maps/place/" + (currLat != null ? currLat:"")
                                    + "," + (currLong != null ? currLong:"") +"\n SOS MESSAGE", null, null);
                        }
                        else{
                            smsManager.sendTextMessage(pMan.getParentInfo().PARENT_PHONE_NUMBER, null, "Ur " + child.CHILD_FIRST_NAME +
                                    " called for help. http://www.google.com/maps/place/" + (currLat != null ? currLat:"")
                                    + "," + (currLong != null ? currLong:"") +"\n SOS MESSAGE", null, null);
                        }
                    }
                },3000);


            }
        });

        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        ab.setCancelable(false);

        ab.create().show();
    }

    /**
     * Checks permission for location.
     * @return
     */
    public boolean checkLocationPermission() {
        
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("GPS Permission")
                        .setMessage("Please allow us to access your location.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Splash.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    
    private void getCity(double lat, double lng){
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        
        try{
            List<Address> addresses = gcd.getFromLocation(lat, lng, 1);

            if (addresses.size() > 0){
                //System.out.println(addresses.get(0).getLocality());

                String cityText = "City: \n" + addresses.get(0).getLocality();

                city.setText(cityText);
            }
            else{
                // do your staff
                Toast.makeText(this, "No address for specified location.", Toast.LENGTH_SHORT).show();
            }
        }
        catch(IOException ioex){
            Toast.makeText(this, "Failed to retrieve city name.", Toast.LENGTH_SHORT).show();
        }
        
    }

    private void updateLocThread(){

        if(h != null){
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    uploadLocation();
                    h.postDelayed(this,(1000) * updateIntervalMin);
                }
            },(1000) * updateIntervalMin);
        }
    }

    /**
     * Handle location updates
     * @param location
     */
    private void doSomethingWithLocation(Location location){

        Double lng = location.getLongitude();
        Double lat = location.getLatitude();

        //Toast.makeText(Splash.this, location.toString(), Toast.LENGTH_SHORT).show();

        String longData = "Longitude: "
                + lng.toString();
        String latData = "Latitude: "
                + lat.toString();

        currLat = lat;
        currLong = lng;

        Double alt =  location.getAltitude();
        //Float spd = location.getSpeed();

        Float kph = (location.getSpeed()*3600)/1000;
        Float f_bearing = location.getBearing();

        altitude.setText("Altitude: " + alt.toString());
        speed.setText("Speed: " + kph.toString() + " km/h");
        bearing.setText("Bearing: " + f_bearing.toString());
        longitude.setText(longData);
        latitude.setText(latData);

        getCity(lat,lng);
    }

    /**
     * Override all native controls.
     *
     * Override on back press. The user shall not exit the app.
     *
     * Else, pressing back five times will prompt a dialog to ask for help.
     */
    @Override
    public void onBackPressed(){

        if(back_counter == 4){
            //Return to zero
            back_counter = 0;
            showHelpConfirm();
        }
        else{
            back_counter++;
        }
    }

    /**
     * For handling runtime permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        initLocationListener();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Access to location was denied.", Toast.LENGTH_LONG).show();

                }
                return;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case 85:
                if (resultCode == Activity.RESULT_OK) {
                    Toasty.info(this, "Ready to accept connections.", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
