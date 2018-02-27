package com.minimon.diocian.player;

import org.json.JSONObject;

/**
 * Created by GOOD on 2018-02-27.
 */

public class EpisodeInfo {
    private static volatile EpisodeInfo insatnace;

    private JSONObject data;
    private String idx;
    private String c_idx;
    private String videoUrl;
    private long resumePosition;

    private EpisodeInfo(){
        if(insatnace != null)
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
    }

    public  static EpisodeInfo getInsatnace(){
        if(insatnace == null){
            synchronized (EpisodeInfo.class){
                if(insatnace == null) insatnace = new EpisodeInfo();
            }
        }
        return insatnace;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getC_idx() {
        return c_idx;
    }

    public void setC_idx(String c_idx) {
        this.c_idx = c_idx;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public long getResumePosition() {
        return resumePosition;
    }

    public void setResumePosition(long resumePosition) {
        this.resumePosition = resumePosition;
    }
}
