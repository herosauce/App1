package herosauce.app1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startService = new Intent(context, AlarmTrigger.class);
        context.startService(startService);
    }
}
