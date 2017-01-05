package herosauce.app1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TriggerBR extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent launchMissiles = new Intent(context, Welcome.class);
        context.startActivity(launchMissiles);
    }
}
