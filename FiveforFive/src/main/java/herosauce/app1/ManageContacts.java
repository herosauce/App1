package herosauce.app1;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Map;

public class ManageContacts extends AppCompatActivity implements DialogInterface.OnDismissListener{

    public static final String MY_GROUPS = "MyGroupsFile";
    public static final String GROUP_COUNTER = "CounterFile";
    public static final String FIRST_START = "FirstStartSetting";
    LinearLayout allGroupHolder, groupContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_contacts);
        onFirstTimeStartup();
        //populate screen with groups
        populateSavedGroups();
        //check and see if we just need to add a new group
        tryAddContactToGroup();
        //Manage button click for new group
        //This will start a dialogue asking for the group title, and when the
        //title dialogue is over (one EditText) the name will be saved to the Group Names SP file
        Button new_group = (Button) findViewById(R.id.bNewGroup);
        new_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //group name dialog fragment
                FragmentManager manager = getFragmentManager();
                Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                EditGroupName newDialog = new EditGroupName();
                Bundle nullBundle = new Bundle();
                nullBundle.putString("GroupName", "NULL");
                newDialog.setArguments(nullBundle);

                newDialog.show(manager, "fragment_edit_id");
            }
        });


    }

    public void deleteGroupButtonHandler(Button button, final String groupName, final LinearLayout groupLayout, final LinearLayout groupHolder){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences groupSP = getSharedPreferences(MY_GROUPS, MODE_PRIVATE);
                //TODO: add a confirmation dialog for good measure
                Toast.makeText(getApplicationContext(), "Deleted group " + groupName + ".", Toast.LENGTH_SHORT).show();
                groupSP.edit().remove(groupName).apply();
                groupLayout.removeAllViews();
                groupHolder.removeView(groupLayout);
            }
        });
    }

    public void editGroupButtonHandler(Button button, final String groupName){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //group name dialog fragment
                FragmentManager manager = getFragmentManager();
                Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }
                EditGroupName newDialog = new EditGroupName();
                Bundle nameBundle = new Bundle();
                nameBundle.putString("GroupName", groupName);
                newDialog.setArguments(nameBundle);
                newDialog.show(manager, "fragment_edit_id");
            }
        });
    }

    @Override
    public void onDismiss(final DialogInterface dialog){
        populateSavedGroups();
    }

    public void addContactButtonHandler(Button button, final String groupName){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startPhoneBook = new Intent(getApplicationContext(), PhoneBook.class);
                startPhoneBook.putExtra("GroupName", groupName);
                startActivity(startPhoneBook);
            }
        });
    }

    public void populateSavedGroups(){
        //Iterate over MY_GROUPS shared preferences
        //For each group, populate rows for each contact saved to that group in that group's SP file
        //Finally, create an "Add Contact" button
        final SharedPreferences groupSP = getSharedPreferences(MY_GROUPS, MODE_PRIVATE);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        groupParams.setMargins(0,0,0,40);
        allGroupHolder = (LinearLayout) findViewById(R.id.additional_group_holder);
        allGroupHolder.removeAllViews();

        //Remove default group buttons/fields
        LinearLayout activityContainer = (LinearLayout) findViewById(R.id.activity_holder);
        LinearLayout defaultHolder = (LinearLayout) findViewById(R.id.default_group);
        activityContainer.removeView(defaultHolder);

        Map<String, ?> allGroups = groupSP.getAll();
        for (final Map.Entry<String, ?> entry : allGroups.entrySet()) {
            //for each group, have to set a container ID that corresponds to the group
            //this lives in SharedPreferences: Group_Counter (holds unique group IDs)
            final String currentGroupName = entry.getKey();
            SharedPreferences getGroupID = getSharedPreferences(GROUP_COUNTER, MODE_PRIVATE);
            Integer groupID = getGroupID.getInt(currentGroupName, 2);

            //Create titleview with group name
            groupContainer = new LinearLayout(getApplicationContext());
            groupContainer.setLayoutParams(groupParams);
            groupContainer.setOrientation(LinearLayout.VERTICAL);
            groupContainer.setId(groupID);

            TableRow.LayoutParams textParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2);
            TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3); //height was 110, caused issues on samsung
            TableRow.LayoutParams editButtonParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 3);
            editButtonParams.setMargins(0, 0, 20, 0);

            final LinearLayout groupNameHolder = new LinearLayout(getApplicationContext());
            groupNameHolder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            groupNameHolder.setOrientation(LinearLayout.HORIZONTAL);
            allGroupHolder.addView(groupContainer);
            groupContainer.addView(groupNameHolder);

            //Add textview and buttons for each group
            final TextView groupTitle = new TextView(getApplicationContext());
            groupTitle.setLayoutParams(textParams);
            groupTitle.setTextColor(Color.parseColor("#FFA9CEF3"));
            groupTitle.setText(currentGroupName);
            groupNameHolder.addView(groupTitle);

            final Button editGroupName = new Button(getApplicationContext());
            editGroupName.setText("edit");
            editGroupName.setAllCaps(false);
            editGroupName.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
            editGroupName.setTextColor(Color.parseColor("#FFA9CEF3"));
            editGroupName.setLayoutParams(editButtonParams);
            groupNameHolder.addView(editGroupName);
            editGroupButtonHandler(editGroupName, currentGroupName);

            final Button deleteGroup = new Button(getApplicationContext());
            deleteGroup.setText("delete");
            deleteGroup.setAllCaps(false);
            deleteGroup.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.delete_button_border));
            deleteGroup.setTextColor(Color.parseColor("#FFFB6E9D"));
            deleteGroup.setLayoutParams(buttonParams);
            groupNameHolder.addView(deleteGroup);
            deleteGroupButtonHandler(deleteGroup, currentGroupName, groupContainer, allGroupHolder);

            //Read SP file for this group name, and iterate over that
            //I honestly can't believe this worked! :)
            final SharedPreferences thisGroupSP = getSharedPreferences(currentGroupName, MODE_PRIVATE);
            Map<String, ?> groupContacts = thisGroupSP.getAll();
            for (final Map.Entry<String, ?> contact : groupContacts.entrySet()){
                //create layout for contact row
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.setMargins(0,10,0,0);
                final LinearLayout rowHolder = new LinearLayout(getApplicationContext());
                rowHolder.setLayoutParams(rowParams);
                rowHolder.setOrientation(LinearLayout.HORIZONTAL);
                rowHolder.setPadding(8, 8, 8, 8);
                groupContainer.addView(rowHolder);

                final TextView contactName = new TextView(getApplicationContext());
                contactName.setTextColor(Color.WHITE);
                contactName.setTypeface(Typeface.DEFAULT_BOLD);
                contactName.setText(contact.getKey());
                contactName.setLayoutParams(textParams);
                contactName.setId(0);

                final TextView contactNumber = new TextView(getApplicationContext());
                contactNumber.setTextColor(Color.WHITE);
                contactNumber.setText(contact.getValue().toString());
                contactNumber.setLayoutParams(textParams);

                final Button deleteContact = new Button(getApplicationContext());
                deleteContact.setText("X");
                deleteContact.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.delete_button_border));
                deleteContact.setTextColor(Color.parseColor("#FFFB6E9D"));
                deleteContact.setLayoutParams(buttonParams);

                deleteContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        thisGroupSP.edit().remove(contact.getKey()).apply();

                        Toast.makeText(getApplicationContext(), "Deleted from group " + currentGroupName, Toast.LENGTH_SHORT).show();
                        rowHolder.removeAllViews();
                        groupContainer.removeView(rowHolder);
                    }

                });

                rowHolder.addView(contactName);
                rowHolder.addView(contactNumber);
                rowHolder.addView(deleteContact);
            }
            //After adding all rows, but before moving onto next group, need to add
            // "add contact" button and handler

            //So, button first:
            final Button addContactButton = new Button(getApplicationContext());
            addContactButton.setText("add contact");
            addContactButton.setAllCaps(false);
            addContactButton.setBackgroundResource(R.drawable.add_button_border);
            addContactButton.setTextColor(Color.parseColor("#FFA5FFA3"));
            LinearLayout.LayoutParams addButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addButtonParams.setMargins(0,20,0,0);
            addContactButton.setLayoutParams(addButtonParams);
            groupContainer.addView(addContactButton);
            //And now call the handler:
            addContactButtonHandler(addContactButton, currentGroupName);
        }
    }

    public void tryAddContactToGroup(){
        Intent phoneBookIntent = getIntent();

        if (phoneBookIntent.hasExtra("holdover_name")){
            final String parent_group = phoneBookIntent.getStringExtra("holdover_name");
            final String contact_name = phoneBookIntent.getStringExtra("contact_name");
            String contact_number = phoneBookIntent.getStringExtra("contact_number");

            final SharedPreferences thisGroupSP = getSharedPreferences(parent_group, MODE_PRIVATE);
            thisGroupSP.edit().putString(contact_name, contact_number).apply();
            Toast.makeText(getApplicationContext(), "added "+contact_name+" to group "+ parent_group, Toast.LENGTH_SHORT).show();

            allGroupHolder = (LinearLayout) findViewById(R.id.additional_group_holder);
            allGroupHolder.removeAllViews();
            populateSavedGroups();
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
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), Welcome.class));
        finish();
    }

    public void onFirstTimeStartup(){
        //first, evaluate whether it's really the first time
        SharedPreferences sharedPreferences = getSharedPreferences(FIRST_START, MODE_PRIVATE);
        Boolean firstTime = sharedPreferences.getBoolean("contacts_first_time", true);
        if (firstTime){
            //Next, create the default group and add it to screen
            Button edit_default_group = (Button) findViewById(R.id.editDefault);
            Button delete_default_group = (Button) findViewById(R.id.deleteDefault);
            TextView default_group_name = (TextView) findViewById(R.id.default_group_name);
            final LinearLayout default_holder = (LinearLayout) findViewById(R.id.default_group);
            final LinearLayout activity_holder = (LinearLayout) findViewById(R.id.activity_holder);
            final String sDefaultGroupName = default_group_name.getText().toString();

            //Give default group a counter
            final SharedPreferences groupSP = getSharedPreferences(MY_GROUPS, MODE_PRIVATE);
            groupSP.edit().putString(sDefaultGroupName, sDefaultGroupName).apply();

            final SharedPreferences counterSP = getSharedPreferences(GROUP_COUNTER, MODE_PRIVATE);
            counterSP.edit().putInt(sDefaultGroupName, 1).apply();

            Button default_add_contact = (Button) findViewById(R.id.add_contact);
            addContactButtonHandler(default_add_contact, default_group_name.getText().toString());

            //lastly, set first_time to false
            sharedPreferences.edit().putBoolean("contacts_first_time", false).apply();

            edit_default_group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //group name dialog fragment
                    FragmentManager manager = getFragmentManager();
                    Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }
                    EditGroupName newDialog = new EditGroupName();
                    Bundle nullBundle = new Bundle();
                    nullBundle.putString("GroupName", sDefaultGroupName);
                    newDialog.setArguments(nullBundle);
                    newDialog.show(manager, "fragment_edit_id");
                }
            });

            delete_default_group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Deleted group: " + sDefaultGroupName + ".", Toast.LENGTH_SHORT).show();
                    groupSP.edit().remove(sDefaultGroupName).apply();
                    counterSP.edit().remove(sDefaultGroupName).apply();
                    default_holder.removeAllViews();
                    activity_holder.removeView(default_holder);
                }
            });
        }
    }
}


