package com.dosse.airpods.pods.util;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

public class FreePodsPodsUtil {

    public static boolean checkUUID(BluetoothDevice bluetoothDevice) {
        ParcelUuid[] AIRPODS_UUIDS = {
                ParcelUuid.fromString("74ec2172-0bad-4d01-8f77-997b2be0722a"),
                ParcelUuid.fromString("2a72e02b-7b99-778f-014d-ad0b7221ec74"),
                ParcelUuid.fromString("0000111e-0000-1000-8000-00805f9b34fb"),
                ParcelUuid.fromString("0000110b-0000-1000-8000-00805f9b34fb"),
                ParcelUuid.fromString("0000110e-0000-1000-8000-00805f9b34fb"),
                ParcelUuid.fromString("00000000-0000-0000-0099-aabbccddeeff"),
        };
        ParcelUuid[] uuids = bluetoothDevice.getUuids();
        if (uuids == null) return false;
        for (ParcelUuid u : uuids) {
            for (ParcelUuid v : AIRPODS_UUIDS) {
                if (u.equals(v)) return true;
            }
        }
        return false;
    }
}
