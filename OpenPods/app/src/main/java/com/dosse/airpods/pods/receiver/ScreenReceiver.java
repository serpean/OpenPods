package com.dosse.airpods.pods.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.dosse.airpods.pods.PodsService;

import static com.dosse.airpods.Constants.DEBUG_MODE;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = ScreenReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d(TAG, "SCREEN OFF");
                context.stopService(new Intent(context, PodsService.class));
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                Log.d(TAG, "SCREEN ON");
                BluetoothAdapter btAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                if (btAdapter.isEnabled()) context.startService(new Intent(context, PodsService.class));
            }
        }
    }
}
