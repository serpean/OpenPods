package com.dosse.airpods;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class MainActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkBluetoothLE()) {
            showNoBTActivity();
        } else {
            requestPermissions();
            declareButtons();
        }
    }

    private void showNoBTActivity() {

        final Intent noBTActivity = new Intent(this, NoBTActivity.class);
        startActivity(noBTActivity);
        finish();
    }

    private boolean checkBluetoothLE() {
        final BluetoothAdapter btAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        return btAdapter == null || (btAdapter.isEnabled() && btAdapter.getBluetoothLeScanner() == null);
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
        } else {
            Starter.startPodsService(getApplicationContext());
        }
    }

    private void declareButtons() {

        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Starter.startPodsService(getApplicationContext());

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            }
        }

    }

}
