package herosauce.app1;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

public class Settings extends AppCompatActivity {

    //Pulling from all the Shared Preferences in this activity
    public static final String MY_MESSAGES = "MyMessagesFile";
    public static final String ALERT_SETTINGS = "MyAlertSettings";
    public static final String MY_GROUPS = "MyGroupsFile";
    public static final String GROUP_COUNTER = "CounterFile";
    public static final String DEFAULT_MESSAGE = "DefaultMessageFile";
    public static final String IS_ARMED = "AppIsArmedFile";
    public static final String TRIGGER_COUNTER = "TriggerCounterFile";
    public static final String FUSE_LENGTH = "FuseLengthFile";

    //TODO: get programatically added checkboxes to look good
    public static final String ALARM_TRIGGERS = "MyTriggersFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Establish click count and whether or not alarm is triggered
        final EditText triggerCounter = (EditText) findViewById(R.id.etTriggerCount);
        final SharedPreferences counterSP = getSharedPreferences(TRIGGER_COUNTER, MODE_PRIVATE);
        //Get counter and set EditText with value
        Integer previousCounter = counterSP.getInt("default", 3);
        triggerCounter.setText(String.valueOf(previousCounter));
        triggerCounter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (triggerCounter.getText().toString().equals("")){
                    counterSP.edit().putInt("default", 3).apply();
                } else {
                    Integer newCounter = Integer.parseInt(triggerCounter.getText().toString());
                    counterSP.edit().putInt("default", newCounter).apply();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final CheckBox isDisarmed = (CheckBox) findViewById(R.id.cbDisarmTrigger);
        final SharedPreferences armedSP = getSharedPreferences(IS_ARMED, MODE_PRIVATE);
        //set checked status based on armedSP
        boolean wasArmed = armedSP.getBoolean("armed", true);
        if (wasArmed) {
            isDisarmed.setChecked(false);
        }else {
            isDisarmed.setChecked(true);
        }
        isDisarmed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                               @Override
                                               public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                                                   if (isChecked){
                                                       armedSP.edit().putBoolean("armed", false).apply();
                                                   } else {
                                                       armedSP.edit().putBoolean("armed", true).apply();
                                                   }
                                               }
                                           }
        );

        LinearLayout groupHolder = (LinearLayout) findViewById(R.id.settings_group_holder);
        groupHolder.removeAllViews();
        SharedPreferences groupSP = getSharedPreferences(MY_GROUPS, MODE_PRIVATE);
        Map<String, ?> allGroups = groupSP.getAll();
        for (final Map.Entry<String, ?> entry : allGroups.entrySet()) {
            final String currentGroupName = entry.getKey();
            //Need to set a textview for each group, within a rowholder, and a checkbox
            //Going to try subbing a linearlayout for the tablerow below
            /*TableRow.LayoutParams rowHolderParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowHolderParams.setMargins(20,4,0,0);
            rowHolderParams.setLayoutDirection(LinearLayout.HORIZONTAL);
            final LinearLayout rowHolder = new LinearLayout(getApplicationContext());
            rowHolder.setLayoutParams(rowHolderParams);*/

            final LinearLayout rowHolder = new LinearLayout(getApplicationContext());
            rowHolder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowHolder.setOrientation(LinearLayout.HORIZONTAL);

            TableRow.LayoutParams textParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2);
            textParams.setMargins(60,0,0,0);
            TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3);

            //Group title textview setup
            final TextView tvGroup = new TextView(getApplicationContext());
            tvGroup.setText(currentGroupName);
            tvGroup.setLayoutParams(textParams);
            tvGroup.setTypeface(Typeface.DEFAULT_BOLD);
            tvGroup.setTextColor(Color.GRAY);
            rowHolder.addView(tvGroup);

            //Setting up the checkbox
            final CheckBox cbGroup = new CheckBox(getApplicationContext());
            cbGroup.setLayoutParams(buttonParams);
            rowHolder.addView(cbGroup);

            //When true, save group in new SharedPreferences file as true - indicating it is a default group.
            final SharedPreferences alertSP = getSharedPreferences(ALERT_SETTINGS, MODE_PRIVATE);
            cbGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                   @Override
                                                   public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                                                       if (isChecked){
                                                           alertSP.edit().putBoolean(currentGroupName, true).apply();
                                                       } else {
                                                           alertSP.edit().putBoolean(currentGroupName, false).apply();
                                                       }
                                                   }
                                               }
            );

            //If this group was previously marked as default, check the box
            if (alertSP.getBoolean(currentGroupName, false)){
                cbGroup.setChecked(true);
            }

            //Finally, add these views to the Settings screen.
            groupHolder.addView(rowHolder);
        }
        //Now that all the groups are in place, need to deal with choosing the right message to send.

        final SharedPreferences messageSP = getSharedPreferences(MY_MESSAGES, MODE_PRIVATE);
        //need an array to feed the spinner
        final ArrayList<String> messageArray = new ArrayList<>();
        Map<String, ?> allMessages = messageSP.getAll();
        for (final Map.Entry<String, ?> messageEntry : allMessages.entrySet() ){
            messageArray.add(messageEntry.getKey());
        }
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, messageArray);
        spinner.setAdapter(spinnerAdapter);

        //Now to handle spinner changes to save the default message.
        //This section iterates through default messages SP file, which stores a true value when a message is selected as default.
        final SharedPreferences defaultMessageSP = getSharedPreferences(DEFAULT_MESSAGE, MODE_PRIVATE);
        String defaultMessage = "No default message detected";
        final Map<String, ?> defaultMessageMap = defaultMessageSP.getAll();
        for (final Map.Entry<String, ?> defaultEntry : defaultMessageMap.entrySet() ) {
            if (defaultEntry.getValue().equals(true)) {
                defaultMessage = defaultEntry.getKey();
            }
        }

        //Need to see if there is a default message. If not, need to add the default to the list of choices in the spinner.
        if (defaultMessage.equals("No default message detected")) {
            messageArray.add(defaultMessage);
        }
        spinner.setSelection(spinnerAdapter.getPosition(defaultMessage));
        //Now, I need to update what happens when a new item is selected.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newDefault = messageArray.get(position);
                defaultMessageSP.edit().putBoolean(newDefault, true).apply();
                //need to make sure all other messages are not set true
                for (final Map.Entry<String, ?> defaultEntry : defaultMessageMap.entrySet()) {
                    if (!defaultEntry.getKey().equals(newDefault)) {
                        defaultMessageSP.edit().putBoolean(defaultEntry.getKey(), false).apply();
                    }
                }
                TextView messageTitle = (TextView) findViewById(R.id.tvSampleMessageName);
                messageTitle.setText(newDefault);
                TextView messageBody = (TextView) findViewById(R.id.tvMessageBody);
                messageBody.setText(messageSP.getString(newDefault, "NULL"));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Not sure I really need anything here.
            }
        });


    }
}
