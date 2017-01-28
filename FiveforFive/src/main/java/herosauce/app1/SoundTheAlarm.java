package herosauce.app1;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class SoundTheAlarm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_the_alarm);

        /*Button contacts = (Button) findViewById(R.id.bContacts);
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
        });*/

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), Welcome.class));
        finish();
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

}
