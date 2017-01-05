package herosauce.app1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class PowerButtonReceiver extends BroadcastReceiver {

    private Boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i("Action", "Screen off");
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i("Action", "Screen on");
            screenOff = false;
        }

        Intent startTrigger = new Intent(context, AlarmTrigger.class);
        startTrigger.putExtra("screen_state", screenOff);
        context.startService(startTrigger);
    }
}
