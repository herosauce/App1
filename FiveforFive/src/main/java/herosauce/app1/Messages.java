package herosauce.app1;

import android.annotation.TargetApi;
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
import android.os.Build;
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
import android.widget.ImageView;
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
            //firstUseMessage(); Removing for now - not sure this adds value
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void populateSavedMessages(){

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final SharedPreferences prefs = getSharedPreferences(MY_MESSAGES, MODE_PRIVATE);
        containerLayout = (LinearLayout)findViewById(R.id.msgs);
        //Clear existing TextViews
        containerLayout.removeAllViews();

        Map<String, ?> allMessages = prefs.getAll();
        for (final Map.Entry<String, ?> entry : allMessages.entrySet()){

            /*
            <LinearLayout
        style="@style/Dashboard_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/holder_alert_settings"
        android:layout_below="@+id/ll_circle">
        */
            final LinearLayout rowLayout = new LinearLayout(getApplicationContext());
            rowLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            //Dash Text Style
            rowLayout.setPadding(16,16,16,16);
            rowLayout.setElevation(4);
            rowLayout.setBackgroundColor(Color.WHITE);
        /*
        <ImageView
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:id="@+id/iv_trigger_settings"
            android:background="@drawable/ic_alert_settings"/>
            */
            final ImageView messageIcon = new ImageView(getApplicationContext());
            messageIcon.setLayoutParams(new ViewGroup.LayoutParams(124,124));
            messageIcon.setImageResource(R.drawable.ic_message_settings);
            messageIcon.setPadding(4,4,4,4);
            rowLayout.addView(messageIcon);
        /*

        <LinearLayout
            android:layout_width="250dp"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            style="@style/Dash_text_layout">
            */
            LinearLayout layoutMessageDetails = new LinearLayout(getApplicationContext());
            layoutMessageDetails.setLayoutParams(new ViewGroup.LayoutParams(950, ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutMessageDetails.setOrientation(LinearLayout.VERTICAL);
            //Dash Text Style
            layoutMessageDetails.setPadding(16,16,16,16);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                layoutMessageDetails.setElevation(4);
            }
            layoutMessageDetails.setBackgroundColor(Color.WHITE);
            layoutMessageDetails.setPadding(0,0,0,8);
        /*

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="16sp"
                android:text="Message Title"
                android:textColor="#C2185B"/>
                */
            //make textview with key = message title
            final TextView titleView = new TextView(getApplicationContext());
            titleView.setLayoutParams(layoutParams);
            titleView.setTextSize(16);
            titleView.setText(entry.getKey());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                titleView.setTextColor(getColor(R.color.colorPrimary));
            }
            layoutMessageDetails.addView(titleView);
        /*

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="13sp"
                android:id="@+id/tv_click_count"
                android:text="Message Text"/>
                */
            //adding textview with message contents
            final TextView messageBody = new TextView(getApplicationContext());
            messageBody.setLayoutParams(layoutParams);
            messageBody.setTextSize(13);
            messageBody.setTextColor(Color.BLACK);
            messageBody.setText(entry.getValue().toString());
            layoutMessageDetails.addView(messageBody);

            rowLayout.addView(layoutMessageDetails);
        /*

        </LinearLayout>

        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@drawable/ic_ask_pink"
            android:id="@+id/ask_trigger"
            android:layout_gravity="end|center_vertical"/>
            */
            //add edit and delete buttons
            final Button editButton = new Button(getApplicationContext());
            editButton.setLayoutParams(new ViewGroup.LayoutParams(96,96));
            editButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit));

            final Button deleteButton = new Button(getApplicationContext());
            deleteButton.setLayoutParams(new ViewGroup.LayoutParams(96,96));
            deleteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete));

            rowLayout.addView(editButton);
            rowLayout.addView(deleteButton);

            containerLayout.addView(rowLayout);
            View spacer = new View(getApplicationContext());
            spacer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,20));
            containerLayout.addView(spacer);

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
                    //remove row
                    containerLayout.removeView(rowLayout);
                }
            });
        }
    }

    public int getCounter(){
        return counter;
    }

    /*public void firstUseMessage(){

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            editButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
        }
        editButton.setTextColor(Color.parseColor("#FFA9CEF3"));
            editButton.setLayoutParams(layoutParams);
            editButton.setAllCaps(false);

            final Button deleteButton = new Button(getApplicationContext());
            deleteButton.setText("delete");
            deleteButton.setTextColor(Color.parseColor("#FFFB6E9D"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            deleteButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.delete_button_border));
        }
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
    }*/

}
