package com.dosse.airpods.state;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class ApplicationState {

    public static final int DEFAULT_STATUS = -1;

    private int deviceId;
    private int leftStatus;
    private int rightStatus;
    private int caseStatus;
    private long lastSeenConnected;
    private boolean maybeConnected;
    private List<ScanResult> recentBeacons;


    private static ApplicationState applicationState = null;

    private ApplicationState() {
        this.leftStatus = DEFAULT_STATUS;
        this.rightStatus = DEFAULT_STATUS;
        this.caseStatus = DEFAULT_STATUS;
        this.lastSeenConnected = 0;
        this.maybeConnected = false;
        recentBeacons = new ArrayList<>();
    }

    public static ApplicationState getInstance() {
        if (applicationState == null) {
            applicationState = new ApplicationState();
        }
        return applicationState;
    }

    public int getLeftStatus() {
        return leftStatus;
    }

    public void setLeftStatus(int leftStatus) {
        this.leftStatus = leftStatus;
    }

    public int getRightStatus() {
        return rightStatus;
    }

    public void setRightStatus(int rightStatus) {
        this.rightStatus = rightStatus;
    }

    public int getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(int caseStatus) {
        this.caseStatus = caseStatus;
    }

    public long getLastSeenConnected() {
        return lastSeenConnected;
    }

    public void setLastSeenConnected(long lastSeenConnected) {
        this.lastSeenConnected = lastSeenConnected;
    }

    public boolean isMaybeConnected() {
        return maybeConnected;
    }

    public void setMaybeConnected(boolean maybeConnected) {
        this.maybeConnected = maybeConnected;
    }

    public List<ScanResult> getRecentBeacons() {
        return recentBeacons;
    }

    public void setRecentBeacons(List<ScanResult> recentBeacons) {
        this.recentBeacons = recentBeacons;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
}
