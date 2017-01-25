package herosauce.app1;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;


public class Messages extends AppCompatActivity implements DialogInterface.OnDismissListener{

    private int counter = 0;
    LinearLayout containerLayout;
    private static final String MY_MESSAGES = "MyMessagesFile";
    private static final String FIRST_START = "FirstStartSetting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Log.i("Messages", "running");

        //See if this is the first time opening this activity
        SharedPreferences sharedPreferences = getSharedPreferences(FIRST_START, MODE_PRIVATE);
        Boolean first_use = sharedPreferences.getBoolean("messages_first_time", true);
        if (first_use) {
            firstUseMessage();
            //Change the value so this method doesn't run twice
            sharedPreferences.edit().putBoolean("messages_first_time", false).apply();
        } else {
            //Otherwise, populate the saved messages
            populateSavedMessages();
        }


        Button newMessage = (Button) findViewById(R.id.bNewMessage);
        newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                counter++;
                if (counter > 15)
                    return;

                FragmentManager manager = getFragmentManager();
                Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                EditMessages newDialog = new EditMessages();
                //pass null title so new message creation won't crash
                Bundle nullBundle = new Bundle();
                nullBundle.putString("Title", "NULL");
                newDialog.setArguments(nullBundle);

                newDialog.show(manager, "fragment_edit_id");
            }
        });
    }


    @Override
    public void onDismiss(final DialogInterface dialog){
        populateSavedMessages();
    }

    public void populateSavedMessages(){

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 110, 1);
        layoutParams.setMargins(8,8,8,8);
        //layoutParams.weight=1;

        final SharedPreferences prefs = getSharedPreferences(MY_MESSAGES, MODE_PRIVATE);
        containerLayout = (LinearLayout)findViewById(R.id.msgs);
        //Clear existing TextViews
        containerLayout.removeAllViews();

        Map<String, ?> allMessages = prefs.getAll();
        for (final Map.Entry<String, ?> entry : allMessages.entrySet()){

            //make textview with key = message title
            final TextView titleView = new TextView(getApplicationContext());
            titleView.setLayoutParams(layoutParams);
            titleView.setTextColor(Color.WHITE);
            titleView.setTextSize(16);
            titleView.setPadding(8, 8, 8, 8);
            titleView.setText(entry.getKey());
            containerLayout.addView(titleView);

            //adding textview with message contents
            final TextView messageBody = new TextView(getApplicationContext());
            messageBody.setLayoutParams(layoutParams);
            messageBody.setTextColor(Color.WHITE);
            messageBody.setTextSize(12);
            messageBody.setPadding(8, 8, 8, 8);
            messageBody.setText(entry.getValue().toString());
            containerLayout.addView(messageBody);

            //add edit and delete buttons
            final Button editButton = new Button(getApplicationContext());
            editButton.setText("edit");
            editButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
            editButton.setTextColor(Color.parseColor("#FFA9CEF3"));
            editButton.setLayoutParams(layoutParams);
            editButton.setAllCaps(false);

            final Button deleteButton = new Button(getApplicationContext());
            deleteButton.setText("delete");
            deleteButton.setTextColor(Color.parseColor("#FFFB6E9D"));
            deleteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.delete_button_border));
            deleteButton.setLayoutParams(layoutParams);
            deleteButton.setAllCaps(false);

            final LinearLayout buttonHolder = new LinearLayout(getApplicationContext());
            buttonHolder.setOrientation(LinearLayout.HORIZONTAL);

            containerLayout.addView(buttonHolder);
            buttonHolder.addView(editButton);
            buttonHolder.addView(deleteButton);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //bundle up the title and message
                    Bundle bundle = new Bundle();
                    bundle.putString("Title", entry.getKey());
                    bundle.putString("Message", entry.getValue().toString());

                    //launch dialog
                    FragmentManager manager = getFragmentManager();
                    Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }

                    EditMessages editDialog = new EditMessages();
                    editDialog.setArguments(bundle);

                    editDialog.show(manager, "fragment_edit_id");
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get shared preferences, delete title/key pair
                    prefs.edit().remove(entry.getKey()).apply();
                    //make toast
                    Toast.makeText(getApplicationContext(), "deleted.", Toast.LENGTH_SHORT).show();
                    //remove buttons and textview

                    buttonHolder.removeAllViews();
                    containerLayout.removeView(buttonHolder);
                    containerLayout.removeView(titleView);
                    containerLayout.removeView(messageBody);
                }
            });
        }
    }

    public int getCounter(){
        return counter;
    }

    public void firstUseMessage(){

            //Now create a default message - the code below copied and modified from populateMessages() - should really combine these methods
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 110, 1);
            layoutParams.setMargins(8, 8, 8, 8);

            final SharedPreferences prefs = getSharedPreferences(MY_MESSAGES, MODE_PRIVATE);
            containerLayout = (LinearLayout)findViewById(R.id.msgs);
            final String defaultMessageTitle = "Default SOS Message";
            final String defaultMessageBody = "If I don't call/text you within 5 minutes, please call or come find me. (-powered by 5/5)";
            prefs.edit().putString(defaultMessageTitle, defaultMessageBody).apply();

            final TextView titleView = new TextView(getApplicationContext());
            titleView.setLayoutParams(layoutParams);
            titleView.setTextColor(Color.WHITE);
            titleView.setTextSize(16);
            titleView.setPadding(8, 8, 8, 8);
            titleView.setText(defaultMessageTitle);
            containerLayout.addView(titleView);

            //adding textview with message contents
            final TextView messageBody = new TextView(getApplicationContext());
            messageBody.setLayoutParams(layoutParams);
            messageBody.setTextColor(Color.WHITE);
            messageBody.setTextSize(12);
            messageBody.setPadding(8, 8, 8, 8);
            messageBody.setText(defaultMessageBody);
            containerLayout.addView(messageBody);

            //add edit and delete buttons
            final Button editButton = new Button(getApplicationContext());
            editButton.setText("edit");
            editButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
            editButton.setTextColor(Color.parseColor("#FFA9CEF3"));
            editButton.setLayoutParams(layoutParams);
            editButton.setAllCaps(false);

            final Button deleteButton = new Button(getApplicationContext());
            deleteButton.setText("delete");
            deleteButton.setTextColor(Color.parseColor("#FFFB6E9D"));
            deleteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.delete_button_border));
            deleteButton.setLayoutParams(layoutParams);
            deleteButton.setAllCaps(false);

            final LinearLayout buttonHolder = new LinearLayout(getApplicationContext());
            buttonHolder.setOrientation(LinearLayout.HORIZONTAL);

            containerLayout.addView(buttonHolder);
            buttonHolder.addView(editButton);
            buttonHolder.addView(deleteButton);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //bundle up the title and message
                    Bundle bundle = new Bundle();
                    bundle.putString("Title", defaultMessageTitle);
                    bundle.putString("Message", defaultMessageBody);

                    //launch dialog
                    FragmentManager manager = getFragmentManager();
                    Fragment frag = manager.findFragmentByTag("fragment_edit_id");
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }

                    EditMessages editDialog = new EditMessages();
                    editDialog.setArguments(bundle);
                    editDialog.show(manager, "fragment_edit_id");
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get shared preferences, delete title/key pair
                    prefs.edit().remove(defaultMessageTitle).apply();
                    //make toast
                    Toast.makeText(getApplicationContext(), "deleted forever. no take-backsies.", Toast.LENGTH_SHORT).show();
                    //remove buttons and textview

                    buttonHolder.removeAllViews();
                    containerLayout.removeView(buttonHolder);
                    containerLayout.removeView(titleView);
                }
            });



    }

}
