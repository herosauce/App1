package herosauce.app1;

import android.Manifest;
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
import android.os.Build;
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
import android.widget.ImageView;
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

        //populate screen with groups
        populateSavedGroups();

        //check and see if we just need to add a new group
        tryAddContactToGroup();

        //Manage button click for new group
        //This will start a dialogue asking for the group title, and when the
        //title dialogue is over (one EditText) the name will be saved to the Group Names SP file
        Button newGroup = (Button) findViewById(R.id.bNewGroup);

        newGroup.setOnClickListener(new View.OnClickListener() {
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

    public void addContactButtonHandler(ImageView button, final String groupName){
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
        //Include an "Add Contact" icon
        final SharedPreferences groupSP = getSharedPreferences(MY_GROUPS, MODE_PRIVATE);
        allGroupHolder = (LinearLayout) findViewById(R.id.additional_group_holder);
        allGroupHolder.removeAllViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Map<String, ?> allGroups = groupSP.getAll();
        for (final Map.Entry<String, ?> entry : allGroups.entrySet()) {
            //for each group, have to set a container ID that corresponds to the group
            //this lives in SharedPreferences: Group_Counter (holds unique group IDs)
            final String currentGroupName = entry.getKey();
            SharedPreferences getGroupID = getSharedPreferences(GROUP_COUNTER, MODE_PRIVATE);
            Integer groupID = getGroupID.getInt(currentGroupName, 2);

            //Create layout to hold group details
            groupContainer = new LinearLayout(getApplicationContext());
            groupContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            groupContainer.setOrientation(LinearLayout.HORIZONTAL);
            groupContainer.setPadding(16,16,16,16);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                groupContainer.setElevation(4);
            }
            groupContainer.setBackgroundColor(Color.WHITE);
            groupContainer.setId(groupID);

            //Icon for adding a person - click it to add someone to this group (launches phonebook)
            final ImageView addPersonIcon = new ImageView(getApplicationContext());
            addPersonIcon.setLayoutParams(new ViewGroup.LayoutParams(124,124));
            addPersonIcon.setImageResource(R.drawable.ic_message_settings);
            addPersonIcon.setPadding(4,4,4,4);
            groupContainer.addView(addPersonIcon);
            addContactButtonHandler(addPersonIcon,currentGroupName);

            //Layout to hold details about the group: name and members
            LinearLayout layoutMiddleContainer = new LinearLayout(getApplicationContext());
            layoutMiddleContainer.setLayoutParams(new ViewGroup.LayoutParams(950, ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutMiddleContainer.setOrientation(LinearLayout.VERTICAL);
            //Dash Text Style
            layoutMiddleContainer.setPadding(16,16,16,16);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                layoutMiddleContainer.setElevation(4);
            }
            layoutMiddleContainer.setBackgroundColor(Color.WHITE);

            //Row layout to hold group name, edit and delete icon buttons
            final LinearLayout rowGroupName = new LinearLayout(getApplicationContext());
            rowGroupName.setLayoutParams(new ViewGroup.LayoutParams(650, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowGroupName.setOrientation(LinearLayout.HORIZONTAL);
            //Dash Text Style
            rowGroupName.setPadding(8,8,8,8);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rowGroupName.setElevation(4);
            }
            rowGroupName.setBackgroundColor(Color.WHITE);

            //Textview: group name
            final TextView groupName = new TextView(getApplicationContext());
            groupName.setLayoutParams(layoutParams);
            groupName.setTextSize(16);
            groupName.setText(currentGroupName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                groupName.setTextColor(getColor(R.color.colorPrimary));
            }
            rowGroupName.addView(groupName);

            final Button editGroupName = new Button(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                editGroupName.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit));
            }
            editGroupName.setLayoutParams(new ViewGroup.LayoutParams(96,96));
            rowGroupName.addView(editGroupName);
            editGroupButtonHandler(editGroupName, currentGroupName);

            final Button deleteGroup = new Button(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                deleteGroup.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete));
            }
            deleteGroup.setLayoutParams(new ViewGroup.LayoutParams(96,96));
            rowGroupName.addView(deleteGroup);
            deleteGroupButtonHandler(deleteGroup, currentGroupName, groupContainer, allGroupHolder);

            //Add the row to the middle container
            layoutMiddleContainer.addView(rowGroupName);

            //Read SP file for this group name, and iterate over that
            //I honestly can't believe this worked!
            final SharedPreferences thisGroupSP = getSharedPreferences(currentGroupName, MODE_PRIVATE);
            Map<String, ?> groupContacts = thisGroupSP.getAll();
            for (final Map.Entry<String, ?> contact : groupContacts.entrySet()){
                //Layout to hold row for each member: name, number, delete icon
                final LinearLayout rowMemberInfo = new LinearLayout(getApplicationContext());
                rowMemberInfo.setLayoutParams(new ViewGroup.LayoutParams(650, ViewGroup.LayoutParams.WRAP_CONTENT));
                rowMemberInfo.setOrientation(LinearLayout.HORIZONTAL);
                //Dash Text Style
                rowMemberInfo.setPadding(8,8,8,8);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rowMemberInfo.setElevation(4);
                }
                rowMemberInfo.setBackgroundColor(Color.WHITE);

                final TextView contactName = new TextView(getApplicationContext());
                contactName.setTextColor(Color.BLACK);
                contactName.setTypeface(Typeface.DEFAULT_BOLD);
                contactName.setText(contact.getKey());
                contactName.setTextSize(13);
                contactName.setLayoutParams(layoutParams);
                contactName.setId(0);
                rowMemberInfo.addView(contactName);

                final TextView contactNumber = new TextView(getApplicationContext());
                contactNumber.setTextColor(Color.BLACK);
                contactNumber.setText(contact.getValue().toString());
                contactNumber.setLayoutParams(layoutParams);
                contactNumber.setTextSize(13);
                rowMemberInfo.addView(contactNumber);

                final Button deleteContact = new Button(getApplicationContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    deleteContact.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete));
                }
                deleteContact.setLayoutParams(new ViewGroup.LayoutParams(64,64));

                deleteContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        thisGroupSP.edit().remove(contact.getKey()).apply();

                        Toast.makeText(getApplicationContext(), "Deleted from group " + currentGroupName, Toast.LENGTH_SHORT).show();
                        rowMemberInfo.removeAllViews();
                        groupContainer.removeView(rowMemberInfo);
                    }

                });
                rowMemberInfo.addView(deleteContact);
                //End of cycle through a single member - add row to container, plus a spacer view
                layoutMiddleContainer.addView(rowMemberInfo);
                View spacer = new View(getApplicationContext());
                spacer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,20));
                layoutMiddleContainer.addView(spacer);
            }
            groupContainer.addView(layoutMiddleContainer);
            allGroupHolder.addView(groupContainer);
            //After adding all rows, but before moving onto next group, need to add
            // "add contact" button and handler

            /*//So, button first:
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
            addContactButtonHandler(addContactButton, currentGroupName);*/
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
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    /*//Not sure this adds any value
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
    }*/
}


