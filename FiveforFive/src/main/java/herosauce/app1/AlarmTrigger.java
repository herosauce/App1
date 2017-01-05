package herosauce.app1;


import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class AlarmTrigger extends Service {

    public static final String DEFAULT_MESSAGE = "DefaultMessageFile";
    private static final String ALERT_SETTINGS = "MyAlertSettings";
    public static final String MY_MESSAGES = "MyMessagesFile";
    public static final String IS_ARMED = "AppIsArmedFile";
    public static final String TRIGGER_COUNTER = "TriggerCounterFile";

    //TODO: make the service stop, or halt, or something, with destroying and restarting over and over again.

    private Integer counter;
    private long timer;
    private boolean isArmed;

    public AlarmTrigger() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new PowerButtonReceiver();
        registerReceiver(mReceiver, filter);
        Log.i("Alarm trigger", "power receiver registered");
        counter = 0;
        timer = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Alarm trigger", "Running, waiting for signal");
        boolean screenOff = intent.getBooleanExtra("screen_state", true);
        SharedPreferences armedSP = getSharedPreferences(IS_ARMED, MODE_PRIVATE);
        isArmed = armedSP.getBoolean("armed", true);

        SharedPreferences counterSP = getSharedPreferences(TRIGGER_COUNTER, MODE_PRIVATE);
        Integer userCounter = counterSP.getInt("default", 3);
        if (isArmed) {
            if (!screenOff) {
                //Check time - if it's been more than 3 seconds since the last click, reset click count
                if (System.currentTimeMillis() - timer > 2500) {
                    counter = 0;
                }
                counter += 1;
                Log.i("Alarm trigger", "counting: " + counter.toString());
                //Reset timer - you get two more seconds to make a move.
                timer = System.currentTimeMillis();
                String s = String.valueOf(timer);
                Log.i("Timer", s);
                if (counter >= userCounter) {
                    Log.i("Alarm trigger", "Triggered - fire ze missiles!");
                    counter = 0;

                    fireZeMissiles();
                }
            } else if (screenOff && counter != 0) {
                if (System.currentTimeMillis() - timer > 3500) {
                    String c = String.valueOf(System.currentTimeMillis() - timer);
                    Log.i("Timer - calculated time", c);
                    counter = 0;
                }
                counter += 1;
                Log.i("Alarm trigger", "still counting: " + counter.toString());
            }
        } else {
            Log.i("Alarm trigger", "not counting, not destroyed");
            //onDestroy();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Not sure I need anything here.
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Alarm trigger", "Destroyed");
        counter = 0;

        //check if the system is armed. If not, don't restart the service.
        SharedPreferences armedSP = getSharedPreferences(IS_ARMED, MODE_PRIVATE);
        isArmed = armedSP.getBoolean("default", true);
        if (isArmed) {
            Intent restartService = new Intent(getApplicationContext(), AlarmTrigger.class);
            startService(restartService);
        } else {
            Log.i("Alarm Trigger", "Disarmed; shutting down service");
        }

    }

    public String addGPStoSMS (String originalMessage, Location currentLocation){
    //waiting for v.2 to implement this. Going to be great, though.
        StringBuffer smsBody = new StringBuffer();
        smsBody.append(originalMessage);
        smsBody.append(" http://maps.google.com?q=");
        smsBody.append(currentLocation.getLatitude());
        smsBody.append(",");
        smsBody.append(currentLocation.getLongitude());

        return smsBody.toString();
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



    private void MultipleSMS(String phoneNumber, String message) {
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
}