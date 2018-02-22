package com.minimon.diocian.player;

import android.graphics.Bitmap;

/**
 * Created by GOOD on 2018-02-21.
 */

public class Drama {
    private String idx;
    private String c_idx;
    private String ep;
    private String thumbnailUrl;
    private String contentTitle;
    private String c_title;
    private String channelName;
    private String playCount;
    private String heartCount;
    private String point;
    private String playTime;


    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getPlayCount() {
        return playCount;
    }

    public void setPlayCount(String playCount) {
        this.playCount = playCount;
    }

    public String getHeartCount() {
        return heartCount;
    }

    public void setHeartCount(String heartCount) {
        this.heartCount = heartCount;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getEp() {
        return ep;
    }

    public void setEp(String ep) {
        this.ep = ep;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getC_idx() {
        return c_idx;
    }

    public void setC_idx(String c_idx) {
        this.c_idx = c_idx;
    }

    public String getC_title() {
        return c_title;
    }

    public void setC_title(String c_title) {
        this.c_title = c_title;
    }
}
