package com.dosse.airpods;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.dosse.airpods.pods.PodsService;

import java.util.Arrays;
import java.util.List;

/**
 * A simple starter class that starts the service when the device is booted, or after an update
 */
public class Starter extends BroadcastReceiver {

    private static final String TAG = Starter.class.getSimpleName();
    private static final List<String> starterActions = Arrays.asList(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_PACKAGE_ADDED);

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (action != null && starterActions.contains(action)) {
            startPodsService(context);
        }
    }

    public static void startPodsService(final Context context) {
        context.startService(new Intent(context, PodsService.class));
    }

    public static void restartPodsService(final Context context) {
        context.stopService(new Intent(context, PodsService.class));
        try {
            Thread.sleep(500);
        } catch (final InterruptedException t) {
            Log.e(TAG, "Can't wait on restart");
            Thread.currentThread().interrupt();
        }
        context.startService(new Intent(context, PodsService.class));
    }
}
