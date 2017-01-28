package herosauce.app1;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

import static herosauce.app1.Settings.ALERT_SETTINGS;
import static herosauce.app1.Settings.DEFAULT_MESSAGE;
import static herosauce.app1.Settings.FUSE_LENGTH;
import static herosauce.app1.Settings.MY_GROUPS;
import static herosauce.app1.Settings.MY_MESSAGES;
import static herosauce.app1.Settings.TRIGGER_COUNTER;

public class Welcome extends AppCompatActivity implements DialogInterface.OnDismissListener{

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
        populateFuse();

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
        //And now the icon for the explanatory toast
        ImageView askGroup = (ImageView) findViewById(R.id.ask_people);
        askGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Manage the people you want to alert in an emergency", Toast.LENGTH_LONG).show();
            }
        });

        //Last one: message settings
        ImageView messageIcon = (ImageView) findViewById(R.id.iv_message_settings);
        messageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMessages = new Intent (getApplicationContext(),Messages.class);
                startActivity(startMessages);
            }
        });
        //Prepare TextViews related to the default message
        TextView defaultMessageTitle = (TextView) findViewById(R.id.tv_message_title);
        TextView messageFirstLine = (TextView) findViewById(R.id.tv_message_line);
        updateMessageViews(defaultMessageTitle, messageFirstLine);
        //And finally, the explanatory toast
        ImageView askMessages = (ImageView) findViewById(R.id.ask_message);
        askMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Manage the message you send in an emergency", Toast.LENGTH_LONG).show();
            }
        });

        //Set up the delayed alert
        TextView timerDisplayMessage = (TextView) findViewById(R.id.tv_timer_display);
        TextView timer = (TextView) findViewById(R.id.tv_time);
        enableTimer(timerDisplayMessage, timer);

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

    private void enableTimer(final TextView display, final TextView timer) {
        //Access fuse length from a Shared Preferences file
        final SharedPreferences fuseSP = getSharedPreferences(FUSE_LENGTH, MODE_PRIVATE);
        final int fuseLength = fuseSP.getInt("fuse",15);
        //Controls the countdown timer and manages the on tick and on finish events
        timer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Launch a fragment to adjust how many minutes the timer should take
                FragmentManager manager = getFragmentManager();
                Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                TriggerDialog dialog = new TriggerDialog();
                dialog.show(manager, "fragment_edit_id");
                return false;
            }
        });
        //Create a Count Down Timer object using the fuse length, captured by the user
        final CountDownTimer myTimer = new CountDownTimer(fuseLength*60*1000,1000){

            @Override
            public void onTick(long millisUntilFinished) {
                int minutesUntilFinished = (int) millisUntilFinished/60000;
                int s = (int) millisUntilFinished / 1000;
                s = s % 60;
                String ms = String.valueOf(minutesUntilFinished) + ":" + String.valueOf(s);
                timer.setText(ms);
            }

            @Override
            public void onFinish() {
                //This is where the message gets sent
                fireZeMissiles();
            }
        };

        //Normal click on the numbers should start the countdown.
        // Click event handled in StartTimer method.
        startTimer(display, timer, myTimer);
    }

    private void startTimer(final TextView display, final TextView timer, final CountDownTimer myTimer) {
        //Adjust the start button - circle and textviews to reflect active status
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display.setText("Disarm");
                myTimer.start();
                //Adjust circle color and add a disarm handler
                LinearLayout circleLayout = (LinearLayout) findViewById(R.id.ll_circle);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    circleLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_round_red));
                }
                disarmTimerHandler(display, timer, myTimer);
            }
        });

    }

    private void disarmTimerHandler(final TextView display, final TextView timer, final CountDownTimer myTimer) {
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTimer.cancel();
                display.setText("Start Timer");
                //Adjust circle color and add a start handler
                LinearLayout circleLayout = (LinearLayout) findViewById(R.id.ll_circle);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    circleLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_round));
                }
                //Reset timer display to default fuse length
                final SharedPreferences fuseSP = getSharedPreferences(FUSE_LENGTH, MODE_PRIVATE);
                int fuseLength = fuseSP.getInt("fuse",15);
                String timerText = String.valueOf(fuseLength) + ":00";
                timer.setText(timerText);
                startTimer(display, timer, myTimer);
            }
        });
    }

    public void fireZeMissiles(){
        //Sends default SMS to groups selected in Settings file.
        //First: set up list of groups to send to
        ArrayList<String> groupArray = new ArrayList<>();

        SharedPreferences groupSP = getSharedPreferences(ALERT_SETTINGS, MODE_PRIVATE);
        Map<String, ?> groupMap = groupSP.getAll();
        for (final Map.Entry<String, ?> entry : groupMap.entrySet()){
            if (entry.getValue().equals(true)){
                groupArray.add(entry.getKey());
            }
        }
        //if there are no contacts in the group(s), cancel operation.
        if (groupArray.isEmpty()){onDestroy();}

        //Now, establish the default message to be sent
        final SharedPreferences defaultMessageSP = getSharedPreferences(DEFAULT_MESSAGE, MODE_PRIVATE);
        String defaultMessageTitle = "No default message detected";
        final Map<String, ?> defaultMessageMap = defaultMessageSP.getAll();
        for (final Map.Entry<String, ?> defaultEntry : defaultMessageMap.entrySet() ) {
            if (defaultEntry.getValue().equals(true)) {
                defaultMessageTitle = defaultEntry.getKey();
            }
        }
        //if there isn't a default message, cancel the operation here.
        if (defaultMessageTitle.equals("No default message detected")){onDestroy();}

        SharedPreferences messagesSP = getSharedPreferences(MY_MESSAGES, MODE_PRIVATE);
        String defaultMessage = messagesSP.getString(defaultMessageTitle, "No Message Found");
        //String sosMessage = addGPStoSMS(defaultMessage, getLastBestLocation());
        //Iterate over each group in groupArray; each member of the group has a number. Looks like BlastMessages.
        for (int i=0; i<groupArray.size(); i++){
            String currentGroupName = groupArray.get(i);
            final SharedPreferences thisGroupSP = getSharedPreferences(currentGroupName, MODE_PRIVATE);
            Map<String, ?> groupContacts = thisGroupSP.getAll();
            for (final Map.Entry<String, ?> contact : groupContacts.entrySet()){
                String number = contact.getValue().toString();
                //MultipleSMS(number, sosMessage);
                MultipleSMS(number, defaultMessage);
            }
        }
    }

    public void MultipleSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        // ---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(arg0, "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        Log.d("SMS STATUS", "SMS Sent.");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(arg0, "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(arg0, "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(arg0, "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(arg0, "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // ---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(arg0, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        Log.d("SMS Status", "SMS delivered.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(arg0, "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    private void updateMessageViews(TextView defaultMessageTitle, TextView messageFirstLine) {
        //Retrieves the stored Default Message, if it exists, and sets the dash display
        SharedPreferences messageSP = getSharedPreferences(MY_MESSAGES, MODE_PRIVATE);
        //Separate SP file for the default message - true means a message is set as default
        SharedPreferences defaultMessageSP = getSharedPreferences(DEFAULT_MESSAGE, MODE_PRIVATE);
        String messageTitle = "No default message detected";
        final Map<String, ?> defaultMessageMap = defaultMessageSP.getAll();
        for (final Map.Entry<String, ?> defaultEntry : defaultMessageMap.entrySet() ) {
            if (defaultEntry.getValue().equals(true)) {
                messageTitle = defaultEntry.getKey();
            }
        }
        //Need to see if there is a default message. If not, set TVs accordingly.
        if (messageTitle.equals("No default message detected")){
            defaultMessageTitle.setText(messageTitle);
            messageFirstLine.setText("");
        } else {
            //Otherwise, get the first line of text and set accordingly
            defaultMessageTitle.setText(messageTitle);
            String fullMessage = messageSP.getString(messageTitle, "No Message Defined");
            //Substring below creates a string starting at first character (index 0), up to either
            // the 20th character or the last character, whichever is smaller (in case user has a very
            // short message)
            String firstLine = fullMessage.substring(0, Math.min(fullMessage.length(), 20));
            messageFirstLine.setText(firstLine);
        }
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
        String numberOfClicks = "Clicks to trigger discreet alert: " + String.valueOf(previousCounter);
        clicks.setText(numberOfClicks);
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

    @Override
    public void onDismiss(final DialogInterface dialog){
        populateFuse();
    }

    private void populateFuse() {
        TextView tvFuse = (TextView) findViewById(R.id.tv_time);
        SharedPreferences fuseSP = getSharedPreferences(FUSE_LENGTH, MODE_PRIVATE);
        String fuse = fuseSP.getInt("fuse",15) + ":00";
        tvFuse.setText(fuse);
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
            if (canMakeSmores()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, permsRequestCode);
                }
            }


            Toast.makeText(getApplicationContext(), "Welcome to Ninja Buddy!", Toast.LENGTH_SHORT).show();
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return(checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED);
            }

        }

        return true;

    }

}
