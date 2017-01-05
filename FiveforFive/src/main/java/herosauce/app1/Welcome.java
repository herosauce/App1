package herosauce.app1;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Welcome extends AppCompatActivity {

    private static final String FIRST_START = "FirstStartSetting";
    public static final String IS_ARMED = "AppIsArmedFile";

    //TODO: Remove all logs and to-dos
    //TODO: create feature for time-delayed alert
    //TODO: create string resource files for everything
    //TODO: Make phone contacts activity look good
    //TODO: add icon/logo for users who don't have a photo
    //TODO: add toolbars
    //TODO: add a menu bar (DIY that shit!)
    //TODO: add on back-click listeners for each activity, including welcome
    //TODO: add icons next to buttons that launch activities. This'll totally make it look pro
    //TODO: make the "add contact", "edit", "delete," etc buttons just image buttons. Then I can control how they look, and I'll like them. And it won't actually take that long. Just man up, dude.

    //TODO: V.3 create feature for secondary message trigger based on different clicks (number, probably)
    //TODO: V.3 Integrate with Facebook somehow
    //TODO: V.3 implement checkSelfPermissions(permission) before trying to do anything that uses a "dangerous" permission (basically all of them, all the time)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //Check to see if this is the first time; if yes, display quickstart guide
        onFirstTimeStartup();
        //Starting alarm trigger service explicitly
        Intent startAlarmTriggerService = new Intent(getApplicationContext(), AlarmTrigger.class);
        startService(startAlarmTriggerService);

        Button contacts = (Button) findViewById(R.id.bContacts);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermissionToReadUserContacts();
                Intent startManageContacts = new Intent(getApplicationContext(), ManageContacts.class);
                startActivity(startManageContacts);
            }
        });

        Button messages =(Button) findViewById(R.id.bMessages);
        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMessages = new Intent (getApplicationContext(),Messages.class);
                startActivity(startMessages);
            }
        });

        Button settings =(Button) findViewById(R.id.bSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSettings = new Intent(getApplicationContext(), Settings.class);
                startActivity(startSettings);
            }
        });

        Button quickStart =(Button) findViewById(R.id.bQuickStart);
        quickStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startGuide = new Intent(getApplicationContext(), DelayedAlarm.class);
                startActivity(startGuide);
            }
        });

        Button alarm = (Button) findViewById(R.id.alarm);
        alarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //need to get permission to send SMS
                getPermissionToSendSMS();
                //need to launch new dialog fragment, which I need to create.
                FragmentManager manager = getFragmentManager();
                Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }
                BlastMessages blast = new BlastMessages();
                blast.show(manager, "fragment_edit_id");
            }
        });
    }
    private static final int SEND_SMS_PERMISSIONS_REQUEST = 0;
    public void getPermissionToSendSMS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.SEND_SMS)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }
            requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                    SEND_SMS_PERMISSIONS_REQUEST);
        }
    }

    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    public void getPermissionToReadUserContacts() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (shouldShowRequestPermissionRationale(
                    android.Manifest.permission.READ_CONTACTS)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == SEND_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onFirstTimeStartup(){
        //first, evaluate whether it's really the first time
        SharedPreferences sharedPreferences = getSharedPreferences(FIRST_START, MODE_PRIVATE);
        if (sharedPreferences.getBoolean("welcome_first_time", true)){
            //Set the alarm
            SharedPreferences armedSP = getSharedPreferences(IS_ARMED, MODE_PRIVATE);
            armedSP.edit().putBoolean("armed", true);
            //get all the permissions at once
            String[] permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            int permsRequestCode = 200;
            if (canMakeSmores()){requestPermissions(permissions, permsRequestCode);}


            Toast.makeText(getApplicationContext(), "Welcome to 5/5!", Toast.LENGTH_SHORT).show();
            sharedPreferences.edit().putBoolean("welcome_first_time", false).apply();

            Intent startGuide = new Intent(getApplicationContext(), QuickStartActivity.class);
            startActivity(startGuide);

        }
    }

    //TODO: implement google settings APIs for location, etc (V.2)
    //Below: new way of checking permissions. Trying it out.
    private boolean canMakeSmores(){

        return(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1);

    }

    private boolean hasPermission(String permission){

        if(canMakeSmores()){

            return(checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED);

        }

        return true;

    }

}
