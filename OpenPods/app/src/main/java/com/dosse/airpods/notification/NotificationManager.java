package com.dosse.airpods.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.dosse.airpods.R;
import com.dosse.airpods.state.ApplicationState;

public class NotificationManager {

    private static final String TAG = NotificationManager.class.getSimpleName();

    private android.app.NotificationManager mNotifyManager;
    private Context context;

    @NonNull
    public static NotificationManager from(@NonNull Context context) {
        return new NotificationManager(context);
    }

    private NotificationManager(final Context context) {
        this.context = context;
    }

    public void createNotificationManager() {

        if (mNotifyManager == null) {
            mNotifyManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(TAG, TAG, android.app.NotificationManager.IMPORTANCE_LOW);
                channel.enableVibration(false);
                channel.enableLights(false);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mNotifyManager.createNotificationChannel(channel);
            }
        }
    }

    public Notification createOpenPodsNotification() {


        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, TAG)
                .setShowWhen(false)
                .setOngoing(false)
                .setSmallIcon(R.mipmap.notification_icon)
                .setTimeoutAfter(10000L);

        if (!isLocationEnabled(context)) {
            final RemoteViews locationDisabledBig = new RemoteViews(context.getPackageName(), R.layout.location_disabled_big);
            final RemoteViews locationDisabledSmall = new RemoteViews(context.getPackageName(), R.layout.location_disabled_small);
            mBuilder.setCustomContentView(locationDisabledSmall)
                    .setCustomBigContentView(locationDisabledBig);
        } else {
            final ApplicationState state = ApplicationState.getInstance();
            final RemoteViews notificationBig = createStatusView(context.getPackageName(), R.layout.status_big, state);
            final RemoteViews notificationSmall = createStatusView(context.getPackageName(), R.layout.status_small, state);

            mBuilder.setCustomContentView(notificationSmall)
                    .setCustomBigContentView(notificationBig);
        }
        return mBuilder.build();
    }

    private RemoteViews createStatusView(final String packageName, final int layoutId, final ApplicationState state) {
        final RemoteViews notification = new RemoteViews(packageName, layoutId);
        notification.setViewVisibility(R.id.leftPodText, View.VISIBLE);
        notification.setViewVisibility(R.id.rightPodText, View.VISIBLE);
        notification.setViewVisibility(R.id.podCaseText, getVisibility(state.getCaseStatus()));
        notification.setTextViewText(R.id.leftPodText, getStatusText(state.getLeftStatus()));
        notification.setTextViewText(R.id.rightPodText, getStatusText(state.getRightStatus()));
        notification.setTextViewText(R.id.podCaseText, getStatusText(state.getCaseStatus()));
        return notification;
    }

    private boolean isLocationEnabled(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final LocationManager service = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return service != null && service.isLocationEnabled();
        } else {
            try {
                return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF;
            } catch (final Settings.SettingNotFoundException e) {
                return true;
            }
        }
    }

    private int getVisibility(final int status) {
        return status > 0 ? View.VISIBLE : View.INVISIBLE;
    }

    private String getStatusText(final int batteryStatus) {
        return batteryStatus > 0 ? String.valueOf(batteryStatus) : "";
    }

    public void deleteNotification(final Context context) {
        NotificationManagerCompat.from(context).cancel(1);
    }
}

