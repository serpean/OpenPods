package com.dosse.airpods.pods.scancallback;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.dosse.airpods.notification.NotificationManager;
import com.dosse.airpods.pods.util.*;
import com.dosse.airpods.state.ApplicationState;

import java.util.List;

import static com.dosse.airpods.Constants.APPLE_INC;
import static com.dosse.airpods.Constants.DEBUG_MODE;

public class BluetoothScanCallback extends ScanCallback {

    private static final String TAG = BluetoothScanCallback.class.getSimpleName();
    private static final long RECENT_BEACONS_MAX_T_NS = 10000000000L;
    private Context context;

    public BluetoothScanCallback(final Context context) {
        this.context = context;
    }

    @Override
    public void onBatchScanResults(final List<ScanResult> scanResults) {

        for (ScanResult result : scanResults) onScanResult(-1, result);
        super.onBatchScanResults(scanResults);
    }

    @Override
    public void onScanResult(final int callbackType, final ScanResult result) {

        if (isIBeacon(result)) {
            final List<ScanResult> recentBeacons = ApplicationState.getInstance().getRecentBeacons();
            recentBeacons.add(result);
            ScanResult strongestBeacon = result;
            for (ScanResult res : recentBeacons) {
                if (isOldBeacon(result)) {
                    recentBeacons.remove(res);
                } else if (!sameDevice(strongestBeacon, result) && strongestBeacon.getRssi() < res.getRssi()) {
                    strongestBeacon = res;
                }
            }
            analyzeBeacon(strongestBeacon);
        }

    }

    private boolean isIBeacon(final ScanResult scanResult) {

        boolean isIBeacon = false;
        if (scanResult != null && scanResult.getScanRecord() != null) {
            final byte[] data = scanResult.getScanRecord().getManufacturerSpecificData(APPLE_INC);
            isIBeacon = data != null && data.length == 27;
        }
        return isIBeacon;
    }

    private boolean isOldBeacon(final ScanResult result) {

        if (DEBUG_MODE) Log.d(TAG, String.format("%d db", result.getRssi()));
        return SystemClock.elapsedRealtimeNanos() - result.getTimestampNanos() > RECENT_BEACONS_MAX_T_NS;
    }

    private void analyzeBeacon(final ScanResult beacon) {

        if (beacon != null && beacon.getScanRecord() != null && beacon.getRssi() > -60) {
            final byte[] data = beacon.getScanRecord().getManufacturerSpecificData(APPLE_INC);
            if (data != null) {
                final ApplicationState state = ApplicationState.getInstance();
                state.setLeftStatus(getChargeByPosition(data, 13));
                state.setRightStatus(getChargeByPosition(data, 12));
                final int actualCaseCharge = getChargeByPosition(data, 14);
                final int caseCharge = actualCaseCharge > 0 ? actualCaseCharge : state.getCaseStatus();
                state.setCaseStatus(caseCharge);
                Log.d(TAG, "CHANGED Left: " + state.getLeftStatus() + " Right: " + state.getRightStatus() + " Case: " + state.getCaseStatus());
                state.setLastSeenConnected(System.currentTimeMillis());

                final NotificationManager opnm = NotificationManager.from(context);
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                opnm.createNotificationManager();
                notificationManager.notify(1, opnm.createOpenPodsNotification());
            }

        }
    }

    private int getChargeByPosition(final byte[] data, final int pos) {
        int result = -1;
        if (data != null && data.length == 27) {
            final int aux = FreePodsHexUtil.hexByteToInt(data[pos]);
            result = aux <= 100 ? aux : result;
        }
        return result;
    }

    private boolean sameDevice(final ScanResult firstBeacon, final ScanResult secondBeacon) {
        return firstBeacon.getDevice().getAddress().equals(secondBeacon.getDevice().getAddress());
    }

    private boolean isFlipped(String str) {
        return (Integer.toString(Integer.parseInt("" + str.charAt(10), 16) + 0x10, 2)).charAt(3) == '0';
    }
}
