package herosauce.app1;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import static herosauce.app1.Settings.ALERT_SETTINGS;
import static herosauce.app1.Settings.MY_GROUPS;
import static herosauce.app1.Settings.TRIGGER_COUNTER;

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

        //Setting up each icon to launch its respective management activity
        //First, the alert trigger (general settings)
        ImageView triggerIcon = (ImageView) findViewById(R.id.iv_trigger_settings);
        triggerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSettings = new Intent(getApplicationContext(), Settings.class);
                startActivity(startSettings);
            }
        });
        //Prepare textviews for updating
        TextView clicks = (TextView) findViewById(R.id.tv_click_count);
        TextView armedStatus = (TextView) findViewById(R.id.tv_armed_status);
        updateTriggerViews(clicks, armedStatus);
        //Now set ask icon to show explanatory toast
        ImageView askTrigger = (ImageView) findViewById(R.id.ask_trigger);
        askTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Tap the icon to configure alert settings", Toast.LENGTH_LONG).show();
            }
        });

        //Next, the group settings
        ImageView groupIcon = (ImageView) findViewById(R.id.iv_group_settings);
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermissionToReadUserContacts();
                Intent startManageContacts = new Intent(getApplicationContext(), ManageContacts.class);
                startActivity(startManageContacts);
            }
        });
        //Prepare textviews related to the groups
        TextView defaultGroupName = (TextView) findViewById(R.id.tv_group_name);
        TextView defGroupMembers = (TextView) findViewById(R.id.tv_group_folks);
        updateGroupViews(defaultGroupName, defGroupMembers);

        //Last one: message settings
        ImageView messageIcon = (ImageView) findViewById(R.id.iv_message_settings);
        messageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMessages = new Intent (getApplicationContext(),Messages.class);
                startActivity(startMessages);
            }
        });

        /*Button alarm = (Button) findViewById(R.id.alarm);
        alarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //need to get permission to send SMS
                getPermissionToSendSMS();
                //launch new dialog fragment
                FragmentManager manager = getFragmentManager();
                Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }
                BlastMessages blast = new BlastMessages();
                blast.show(manager, "fragment_edit_id");
            }
        });*/
    }

    private void updateGroupViews(TextView defaultGroupName, TextView defGroupMembers) {
        //Grabs information from Settings SP files to display group-related settings at a glance
        SharedPreferences groupSP = getSharedPreferences(MY_GROUPS, MODE_PRIVATE);
        Map<String, ?> allGroups = groupSP.getAll();
        //Initialize list to hold names of folks in def group
        ArrayList<String> list = new ArrayList<>();
        String defGroupName = "No default group defined!";
        int groupCounter = 0;
        for (final Map.Entry<String, ?> entry : allGroups.entrySet()){
            //Iterate over all groups to get the group name
            String currentGroupName = entry.getKey();
            //Use the group name to see if that group is a default group
            SharedPreferences alertSP = getSharedPreferences(ALERT_SETTINGS, MODE_PRIVATE);
            if (alertSP.getBoolean(currentGroupName, false)){
                //If so, call this group the default group (only one default group gets to be displayed on dash)
                defGroupName = currentGroupName;
                groupCounter++;
                //Then, get all users from that group and add them to the list
                SharedPreferences thisGroupSP = getSharedPreferences(currentGroupName, MODE_PRIVATE);
                Map<String, ?> groupContacts = thisGroupSP.getAll();
                for (final Map.Entry<String, ?> contact : groupContacts.entrySet()){
                    //Each key in this map is the name of a person in this group
                    list.add(contact.getKey());
                }
            }
        }
        //Set text for default group name
        if (groupCounter > 1){
            //In this instance, append the def group name string with the number of other groups
            defGroupName = defGroupName + " + " + String.valueOf(groupCounter - 1) + " more...";
        }
        defaultGroupName.setText(defGroupName);
        if (list.size() > 0){
            //Grab the first entry, plus length-1 others
            String members = list.get(0) + " + " + String.valueOf(list.size() - 1) + " more...";
            defGroupMembers.setText(members);
        } else {
            //If no default selected, set text to no default group selected
            defGroupMembers.setText("No default group members found!");
            //TODO: Set text to red and add an alert icon
        }
    }

    private void updateTriggerViews(TextView clicks, TextView armedStatus) {
        //Grabs information from Settings SP files to display settings at a glance
        SharedPreferences counterSP = getSharedPreferences(TRIGGER_COUNTER, MODE_PRIVATE);
        Integer previousCounter = counterSP.getInt("default", 3);
        clicks.setText(String.valueOf(previousCounter));
        //Now same process for armed status
        SharedPreferences armedSP = getSharedPreferences(IS_ARMED, MODE_PRIVATE);
        boolean isArmed = armedSP.getBoolean("armed", true);
        if (isArmed){
            armedStatus.setText("Armed");
            armedStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            armedStatus.setText("Disarmed");
            armedStatus.setTextColor(Color.parseColor("#C2185B"));
        }

    }

    private static final int SEND_SMS_PERMISSIONS_REQUEST = 0;
    public void getPermissionToSendSMS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.SEND_SMS)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
                requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                        SEND_SMS_PERMISSIONS_REQUEST);
            }
        }
    }

    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    public void getPermissionToReadUserContacts() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_CONTACTS)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSIONS_REQUEST);
            }

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
            armedSP.edit().putBoolean("armed", true).apply();
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
