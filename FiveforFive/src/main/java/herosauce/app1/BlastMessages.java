package herosauce.app1;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public class BlastMessages extends DialogFragment {


    public static final String MY_GROUPS = "MyGroupsFile";
    public static final String GROUP_COUNTER = "CounterFile";
    public static final String MY_MESSAGES = "MyMessagesFile";
    private TextView mInstructions, mBlastGroup, mBlastMessage;
    private LinearLayout allGroupHolder;
    private Button groupButton;

    public BlastMessages() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_blast_messages, container);

        mInstructions = (TextView) view.findViewById(R.id.instructions);
        mBlastGroup = (TextView) view.findViewById(R.id.tv_blast_group);
        mBlastMessage = (TextView) view.findViewById(R.id.tv_blast_message);

        //want to set up all the groups; when one is clicked, the others disappear, the textview changes, and the messages appear.
        //TODO When you click the message you want, the counter starts (and cancel button appears) and after a few seconds, the SMS is(are) sent.
        mInstructions.setText("choose a group");
        mBlastGroup.setText("Step 1: choose a group to alert");
        mBlastMessage.setText("Step 2: choose a message to send the group");
        populateGroups(view);

        return view;
    }

    public void populateGroups(final View fragment) {
        //Copied and modified from ManageContacts
        final SharedPreferences groupSP = this.getActivity().getSharedPreferences(MY_GROUPS, Context.MODE_PRIVATE);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        allGroupHolder = (LinearLayout) fragment.findViewById(R.id.launch_container);
        groupParams.setMargins(0, 10, 0, 0);

        Map<String, ?> allGroups = groupSP.getAll();
        for (final Map.Entry<String, ?> entry : allGroups.entrySet()) {
            //for each group, have to set a container ID that corresponds to the group
            //this lives in SharedPreferences: Group_Counter (holds unique group IDs)
            final String currentGroupName = entry.getKey();
            SharedPreferences getGroupID = this.getActivity().getSharedPreferences(GROUP_COUNTER, Context.MODE_PRIVATE);
            final Integer groupID = getGroupID.getInt(currentGroupName, 2);

            groupButton = new Button(getActivity());
            groupButton.setLayoutParams(groupParams);
            groupButton.setId(groupID);
            groupButton.setText(currentGroupName);
            groupButton.setTextColor(Color.WHITE);
            groupButton.setAllCaps(false);
            groupButton.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.button_border));
            groupButton.setPadding(12, 12, 12, 12);

            allGroupHolder.addView(groupButton);

            groupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allGroupHolder.removeAllViews();
                    //allGroupHolder.addView(groupButton);
                    populateMessages(currentGroupName, fragment);
                }
            });

        }
    }

    public void populateMessages(final String name, final View fragment){
        //adding groupID for now, thinking this is how this method can iterate over folks in the group to get the number. Mm?
        final SharedPreferences messageSP = this.getActivity().getSharedPreferences(MY_MESSAGES, Context.MODE_PRIVATE);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        groupParams.setMargins(0,10,0,0);
        allGroupHolder = (LinearLayout) fragment.findViewById(R.id.launch_container);

        mInstructions = (TextView) fragment.findViewById(R.id.instructions);
        mBlastGroup = (TextView) fragment.findViewById(R.id.tv_blast_group);
        mBlastMessage = (TextView) fragment.findViewById(R.id.tv_blast_message);

        mInstructions.setText("select message");
        mBlastGroup.setText("Group: " + name);
        mBlastMessage.setText("Step 2: choose a message to send the group");

        Map<String, ?> allMessages = messageSP.getAll();
        for (final Map.Entry<String, ?> entry : allMessages.entrySet()) {
            final String currentMessageTitle = entry.getKey();
            final String currentMessageText = entry.getValue().toString();

            groupButton = new Button(getActivity());
            groupButton.setLayoutParams(groupParams);
            groupButton.setText(currentMessageTitle);
            groupButton.setTextColor(Color.WHITE);
            groupButton.setAllCaps(false);
            groupButton.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.button_border));
            groupButton.setPadding(12, 12, 12, 12);

            allGroupHolder.addView(groupButton);

            groupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allGroupHolder.removeAllViews();
                    mInstructions.setText("confirm");
                    mBlastMessage.setText("Message: " + currentMessageTitle);
                    //Adding a confirmation dialog here and a yes/no button option.
                    //TODO: Add a third step where user can just set up a time delay before the messages are sent. Needs more research and should look different.
                    allGroupHolder.setOrientation(LinearLayout.HORIZONTAL);
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 110, 1);
                    layoutParams.setMargins(8, 8, 8, 8);

                    final Button yesButton = new Button(fragment.getContext());
                    yesButton.setText("confirm");
                    yesButton.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.add_button_border));
                    yesButton.setTextColor(Color.parseColor("#FFA5FFA3"));
                    yesButton.setLayoutParams(layoutParams);
                    yesButton.setAllCaps(false);

                    final Button noButton = new Button(fragment.getContext());
                    noButton.setText("cancel");
                    noButton.setTextColor(Color.parseColor("#FFFB6E9D"));
                    noButton.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.delete_button_border));
                    noButton.setLayoutParams(layoutParams);
                    noButton.setAllCaps(false);

                    allGroupHolder.addView(noButton);
                    allGroupHolder.addView(yesButton);

                    yesButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final SharedPreferences thisGroupSP = getActivity().getSharedPreferences(name, Context.MODE_PRIVATE);
                            Map<String, ?> groupContacts = thisGroupSP.getAll();
                            for (final Map.Entry<String, ?> contact : groupContacts.entrySet()) {
                                String number = contact.getValue().toString();
                                MultipleSMS(number, currentMessageText);
                            }
                            getDialog().dismiss();
                        }
                    });

                    noButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            getDialog().dismiss();
                        }
                    });
                }
            });
        }
    }

    //following copied from StackOverflow
    private void MultipleSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this.getActivity(), 0, new Intent(
                SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this.getActivity(), 0,
                new Intent(DELIVERED), 0);

        // ---when the SMS has been sent---
        getActivity().registerReceiver(new BroadcastReceiver() {
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
        getActivity().registerReceiver(new BroadcastReceiver() {
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

    @Override
    public void onDismiss(final DialogInterface dialog){
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener){
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
