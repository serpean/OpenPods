package com.dosse.airpods.pods;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import com.dosse.airpods.pods.receiver.*;

/**
 * This is the class that does most of the work. It has 3 functions:
 * - Detect when AirPods are detected
 * - Receive beacons from AirPods and decode them (easier said than done thanks to google's autism)
 * - Display the notification with the status
 */

/**
 * The following method (startAirPodsScanner) creates a bluetoth LE scanner.
 * This scanner receives all beacons from nearby BLE devices (not just your devices!) so we need to do 3 things:
 * - Check that the beacon comes from something that looks like a pair of AirPods
 * - Make sure that it is YOUR pair of AirPods
 * - Decode the beacon to get the status
 * <p>
 * On a normal OS, we would use the bluetooth address of the device to filter out beacons from other devices.
 * UNFORTUNATELY, someone at google was so concerned about privacy (yea, as if they give a shit) that he decided it was a good idea to not allow access to the bluetooth address of incoming BLE beacons. As a result, we have no reliable way to make sure that the beacon comes from YOUR airpods and not the guy sitting next to you on the bus.
 * What we did to workaround this issue is this:
 * - When a beacon arrives that looks like a pair of AirPods, look at the other beacons received in the last 10 seconds and get the strongest one
 * - If the strongest beacon's fake address is the same as this, use this beacon; otherwise use the strongest beacon
 * - Filter for signals stronger than -60db
 * - Decode...
 * <p>
 * Decoding the beacon:
 * This was done through reverse engineering. Hopefully it's correct.
 * - The beacon coming from a pair of AirPods contains a manufacturer specific data field n°76 of 27 bytes
 * - We convert this data to a hexadecimal string
 * - The 12th and 13th characters in the string represent the charge of the left and right pods. Under unknown circumstances, they are right and left instead (see isFlipped). Values between 0 and 10 are battery 0-100%; Value 15 means it's disconnected
 * - The 15th character in the string represents the charge of the case. Values between 0 and 10 are battery 0-100%; Value 15 means it's disconnected
 * - The 14th character in the string represents the "in charge" status. Bit 0 (LSB) is the left pod; Bit 1 is the right pod; Bit 2 is the case. Bit 3 might be case open/closed but I'm not sure and it's not used
 * <p>
 * After decoding a beacon, the status is written to leftStatus, rightStatus, caseStatus, chargeL, chargeR, chargeCase so that the NotificationThread can use the information
 */
public class PodsService extends Service {
    private static final String TAG = PodsService.class.getSimpleName();

    private BroadcastReceiver btReceiver = null;
    private BroadcastReceiver screenReceiver = null;

    /**
     * When the service is created, we register to get as many bluetooth and airpods related events as possible.
     * ACL_CONNECTED and ACL_DISCONNECTED should have been enough, but you never know with android these days.
     */
    @Override
    public void onCreate() {

        super.onCreate();
        btReceiver = new BTReceiver();
        registerReceiver(btReceiver, btIntentFilter());
        final BluetoothAdapter bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bluetoothAdapter.getProfileProxy(getApplicationContext(), new BluetoothLowEnergyProfile(), BluetoothProfile.HEADSET);
        if (bluetoothAdapter.isEnabled()) {
            //if BT is already on when the app is started, start the scanner without waiting for an event to happen
            (new BluetoothScanner()).startAirPodsScanner(this);
        }
        screenReceiver = new ScreenReceiver();
        registerReceiver(screenReceiver, screenIntentFilter());
    }

    private IntentFilter btIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.device.action.NAME_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT");
        intentFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED");
        intentFilter.addCategory("android.bluetooth.headset.intent.category.companyid.76");

        return intentFilter;
    }

    private IntentFilter screenIntentFilter() {

        final IntentFilter screenIntentFilter = new IntentFilter();
        screenIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        return screenIntentFilter;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (btReceiver != null) unregisterReceiver(btReceiver);
        if (screenReceiver != null) unregisterReceiver(screenReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
