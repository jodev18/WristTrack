package com.khsoftsolutions.wristtrack.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.khsoftsolutions.wristtrack.R;
import com.khsoftsolutions.wristtrack.core.Globals;
import com.khsoftsolutions.wristtrack.database.KidManager;
import com.khsoftsolutions.wristtrack.database.ParentManager;
import com.khsoftsolutions.wristtrack.objects.ChildObject;
import com.khsoftsolutions.wristtrack.objects.ParentObject;

import es.dmoral.toasty.Toasty;


/**
 * TODO Make this a dialog activity.
 */
public class WristTrackMenu extends AppCompatActivity {

    private String LAT = "";
    private String LONG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrist_track_menu);

        setTitle("Action Menu");

        LAT = this.getIntent().getStringExtra(Globals.COORDINATES_LAT_KEY);
        LONG = this.getIntent().getStringExtra(Globals.COORDINATES_LONG_LEY);

        setupListView();
    }

    private void setupListView(){

        KidManager kid = new KidManager(WristTrackMenu.this);

        ChildObject cObj = kid.getChildInfo();


        ListView options = (ListView)findViewById(R.id.lvOptionsList);
        String[] choices = {"Help!","Where am I?","Who am I?"};

        ArrayAdapter<String> arrDapt
                = new ArrayAdapter<String>(WristTrackMenu.this,
                    android.R.layout.simple_list_item_1,choices);

        options.setAdapter(arrDapt);

        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i){
                    case 0:
                        showHelpDialog();
                        break;
                    case 1:
                        invokeLocationGet();
                        break;
                    case 2:
                        invokeIdentification();
                        break;
                    default:
                        Log.e("UNEXPECTED_ONITEMCLICK","Shouldn't have happened, but yeah it happened.");
                }
            }
        });

    }

    private void invokeHelpCall(){
        //Intent callIntent =
        ParentManager pMan = new ParentManager(WristTrackMenu.this);

        String phoneNumber = pMan.getParentInfo().PARENT_PHONE_NUMBER;

        pMan.cleanUp();

        Toasty.info(WristTrackMenu.this,"Calling...", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent); //done in the early parts

    }

    private void invokeHelpSMS(){

        ParentManager pMan = new ParentManager(WristTrackMenu.this);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(pMan.getParentInfo().PARENT_PHONE_NUMBER, null,
                "Latitude: " + LAT + "\n Longitude: " + LONG +"\n SOS MESSAGE", null, null);

        pMan.cleanUp();

        Toasty.info(WristTrackMenu.this,"Sent help sms.", Toast.LENGTH_LONG).show();
    }

    private void invokeLocationGet(){

    }

    private void invokeIdentification(){

        KidManager kid = new KidManager(WristTrackMenu.this);
        ParentManager parentManager = new ParentManager(WristTrackMenu.this);

        ChildObject childObject = kid.getChildInfo();
        ParentObject parentObject = parentManager.getParentInfo();

        //Cleanup
        parentManager.cleanUp();
        kid.cleanUp();

        AlertDialog.Builder wIdentify = new AlertDialog.Builder(WristTrackMenu.this);
        wIdentify.setTitle("Identity");
        wIdentify.setMessage("Child Name: " + childObject.CHILD_FIRST_NAME + " "
                + childObject.CHILD_LAST_NAME + "\n" + "Child's Parent: " + parentObject.PARENT_FIRST_NAME
                + " " + parentObject.PARENT_LAST_NAME + "\n" + "Address: " + childObject.CHILD_ADDRESS + "\n"
                + "Contact Number: " + parentObject.PARENT_PHONE_NUMBER);

        wIdentify.create().show();
    }

    private void showHelpDialog(){

        String[] choices = {"Call Parents!","Send them text!"};

        AlertDialog.Builder ab = new AlertDialog.Builder(WristTrackMenu.this);

        ab.setTitle("Call for help!");

        ab.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch(i){
                    case 0:
                        invokeHelpCall();
                        break;
                    case 1:
                        invokeHelpSMS();
                        break;
                    default:
                }

                dialogInterface.dismiss();
            }
        });

        ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        ab.setCancelable(false);

        ab.create().show();
    }

}
