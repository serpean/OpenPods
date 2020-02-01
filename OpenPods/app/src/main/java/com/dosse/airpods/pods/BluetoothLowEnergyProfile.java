package com.dosse.airpods.pods;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import com.dosse.airpods.state.ApplicationState;
import com.dosse.airpods.pods.util.*;

import static com.dosse.airpods.Constants.DEBUG_MODE;

/*
This BT Profile Proxy allows us to know if airpods are already connected
 when the app is started. It also fires an event when BT is turned off,
 in case the BroadcastReceiver doesn't do its job
*/
public class BluetoothLowEnergyProfile implements BluetoothProfile.ServiceListener {
    private static final String TAG = BluetoothLowEnergyProfile.class.getSimpleName();

    @Override
    public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
        if (i == BluetoothProfile.HEADSET) {
            if (DEBUG_MODE) Log.d(TAG, "BT PROXY SERVICE CONNECTED");
            BluetoothHeadset h = (BluetoothHeadset) bluetoothProfile;
            for (BluetoothDevice d : h.getConnectedDevices()) {
                if (FreePodsPodsUtil.checkUUID(d)) {
                    if (DEBUG_MODE) Log.d(TAG, "BT PROXY: AIRPODS ALREADY CONNECTED");
                    final ApplicationState state = ApplicationState.getInstance();
                    state.setMaybeConnected(true);
                    break;
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(final int i) {
        if (i == BluetoothProfile.HEADSET) {
            if (DEBUG_MODE) Log.d(TAG, "BT PROXY SERVICE DISCONNECTED ");
            final ApplicationState state = ApplicationState.getInstance();
            state.setMaybeConnected(false);
        }

    }
}
