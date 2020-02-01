package com.dosse.airpods.pods.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.dosse.airpods.pods.PodsService;
import com.dosse.airpods.pods.util.*;
import com.dosse.airpods.state.ApplicationState;

public class BTReceiver extends BroadcastReceiver {
    private static final String TAG = BTReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();

        if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                Log.d(TAG, "BT OFF");
                context.stopService(new Intent(context, PodsService.class));
                final ApplicationState appState = ApplicationState.getInstance();
                appState.getRecentBeacons().clear();
            } else if (state == BluetoothAdapter.STATE_ON) {
                Log.d(TAG, "BT ON");
                context.startService(new Intent(context, PodsService.class));
            }
        }

        final BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        if (FreePodsPodsUtil.checkUUID(bluetoothDevice) && action != null && (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) || action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED))) { //airpods disconnected, remove notification but leave the scanner going
            Log.d(TAG, "ACL DISCONNECTED");
            final ApplicationState appState = ApplicationState.getInstance();
            appState.getRecentBeacons().clear();
        }

    }
}
