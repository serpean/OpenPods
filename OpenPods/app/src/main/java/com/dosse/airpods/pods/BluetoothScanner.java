package com.dosse.airpods.pods;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.Context;
import android.util.Log;
import com.dosse.airpods.pods.scancallback.*;
import com.dosse.airpods.state.ApplicationState;

import java.util.Collections;
import java.util.List;

import static com.dosse.airpods.Constants.DEBUG_MODE;

public class BluetoothScanner {
    private static final String TAG = BluetoothScanner.class.getSimpleName();

    private BluetoothLeScanner btScanner;

    public void startAirPodsScanner(final Context context) {

        if (DEBUG_MODE) Log.d(TAG, "START SCANNER");
        createBTScanner(context);
        btScanner.startScan(getScanFilters(), getSettings(), new BluetoothScanCallback(context));
    }

    private void createBTScanner(final Context context) {
        final BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) Log.w(TAG, "BT Off");
            btScanner = btAdapter.getBluetoothLeScanner();
        } else Log.w(TAG, "No BT");
    }

    private List<ScanFilter> getScanFilters() {
        final byte[] manufacturerData = new byte[27];
        final byte[] manufacturerDataMask = new byte[27];

        manufacturerData[0] = 7;
        manufacturerData[1] = 25;

        manufacturerDataMask[0] = -1;
        manufacturerDataMask[1] = -1;

        final ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setManufacturerData(76, manufacturerData, manufacturerDataMask);
        return Collections.singletonList(builder.build());
    }

    private ScanSettings getSettings() {
        return new ScanSettings.Builder().setScanMode(0).setReportDelay(0).build();
    }

    public void stopAirPodsScanner() {
        if (btScanner != null) {
            if (DEBUG_MODE) Log.d(TAG, "STOP SCANNER");
            btScanner.stopScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                }
            });
        }
        final ApplicationState applicationState = ApplicationState.getInstance();
        applicationState.setLeftStatus(ApplicationState.DEFAULT_STATUS);
        applicationState.setRightStatus(ApplicationState.DEFAULT_STATUS);
        applicationState.setCaseStatus(ApplicationState.DEFAULT_STATUS);
    }

}
