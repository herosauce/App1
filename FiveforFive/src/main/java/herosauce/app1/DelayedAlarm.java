package herosauce.app1;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class DelayedAlarm extends AppCompatActivity {

    //Designed to look and feel like Settings activity
    //Users will choose a group, a message, and a time interval (default 15 minutes)
    //Big confirmation button on the bottom launches service that waits specified interval before sending messages
    //On confirm, button becomes a cancel button; requires confirmation to cancel
    //Cancel halts service and resets page to default values. No groups selected by default, nor message.

    //TODO: set up service and BR to run counter in background, and get TextView updates from service
    //TODO: make the timer clickable, so users can specify a time
    //TODO: make the timer more attractive
    Button bStartTimer, bStopTimer;
    TextView tvTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delayed_alarm);

        bStartTimer = (Button) findViewById(R.id.button_timer);
        tvTimer = (TextView) findViewById(R.id.tvTimer);

        tvTimer.setText("00:15:00");

        final CounterClass timer = new CounterClass(900000, 1000);
        startButtonHandler(bStartTimer, timer, tvTimer);

    }

    public void startButtonHandler (final Button startButton, final CounterClass counter, final TextView tv){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.start();
                startButton.setText("disarm and reset alarm");
                startButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.delete_button_border));
                startButton.setTextColor(Color.parseColor("#FFFB6E9D"));
                disarmButtonHandler(startButton, counter, tv);

            }
        });
    }

    public void disarmButtonHandler(final Button disarmButton, final CounterClass counter, final TextView tv){
        disarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.cancel();
                disarmButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dark_green_button_border));
                disarmButton.setTextColor(Color.parseColor("#006b60"));
                tv.setText("00:15:00");
                disarmButton.setText("light the fuse");
                //TODO: stop the service that is set to send the SMS
                startButtonHandler(disarmButton, counter, tv);
            }
        });
    }

    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis )),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            tvTimer.setText(hms);

        }

        @Override
        public void onFinish() {
            tvTimer.setText("Message sent.");
            //TODO: this is where the SMS get sent
        }
    }

}
