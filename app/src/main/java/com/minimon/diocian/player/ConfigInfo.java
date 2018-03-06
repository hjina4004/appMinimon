package com.minimon.diocian.player;

/**
 * Created by GOOD on 2018-02-28.
 */

public class ConfigInfo {
    public static volatile ConfigInfo configInfo;
    public static final int bandwidth480        = 0;
    public static final int bandwidth720        = 1;
    public static final int bandwidth1080       = 2;
    public static final int banswidthAuto       = 3;
    private ConfigInfo(){
        if(configInfo != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static ConfigInfo getInstance(){
        if(configInfo == null){
            synchronized (ConfigInfo.class){
                if(configInfo == null) configInfo = new ConfigInfo();
            }
        }
        return configInfo;
    }

    private int bandwidth;
    private boolean isUseData;
    private boolean isAlertEvent;
    private boolean isAlertNotice;

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public boolean isUseData() {
        return isUseData;
    }

    public void setUseData(boolean useData) {
        isUseData = useData;
    }

    public boolean isAlertEvent() {
        return isAlertEvent;
    }

    public void setAlertEvent(boolean alertEvent) {
        isAlertEvent = alertEvent;
    }

    public boolean isAlertNotice() {
        return isAlertNotice;
    }

    public void setAlertNotice(boolean alertNotice) {
        isAlertNotice = alertNotice;
    }
}
