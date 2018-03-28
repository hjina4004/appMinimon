package com.minimon.diocian.player;

import android.content.ContentValues;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by GOOD on 2018-02-27.
 */

public class EpisodeInfo {
    private static volatile EpisodeInfo insatnace;

    public static final int bandwidth480        = 0;
    public static final int bandwidth720        = 1;
    public static final int bandwidth1080       = 2;
    public static final int banswidthAuto       = 3;

    private String idx;
    private String c_idx;
    private String videoUrl;
    private String title;
    private long resumePosition;
    private String thumbnailUrl;
    private String isAdult;
    private String point;
    private String playTime;
    private String grade;
    private String ep;
    private String remainingTime;
    private boolean isUseLte;
    private int bandwidth;
    private String introVideoUrl;
    private String currentVideoUrl;
    private boolean prepareVideoFlag;
    private boolean isReplayVideoFlag;
    private boolean isIntro;
    private ArrayList<EpisodeHistory> episodeHistory = new ArrayList<>();

    public EpisodeHistory historyPop(){
        EpisodeHistory last = episodeHistory.get(episodeHistory.size()-1);
        episodeHistory.remove(episodeHistory.size()-1);
        return last;
    }

    public void historyPush(String url, ContentValues values){
        EpisodeHistory history = new EpisodeHistory();
        history.setUrl(url);
        history.setContent(values);
        episodeHistory.add(history);
    }

    public void historyClear(){
        episodeHistory.clear();
    }

    public int getHistorySize(){
        return episodeHistory.size();
    }

    public EpisodeHistory historyLast(){
        if(getHistorySize()==0)
            return null;
        EpisodeHistory last = episodeHistory.get(episodeHistory.size()-1);
        return last;
    }


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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getIsAdult() {
        return isAdult;
    }

    public void setIsAdult(String isAdult) {
        this.isAdult = isAdult;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getEp() {
        return ep;
    }

    public void setEp(String ep) {
        this.ep = ep;
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        //String clean1 = string1.replaceAll("[^0-9]", "");
        this.remainingTime = remainingTime.replaceAll("[^0-9]", "");
    }

    public boolean isUseLte() {
        return isUseLte;
    }

    public void setUseLte(boolean useLte) {
        isUseLte = useLte;
    }

    public String getIntroVideoUrl() {
        return introVideoUrl;
    }

    public void setIntroVideoUrl(String introVideoUrl) {
        this.introVideoUrl = introVideoUrl;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getCurrentVideoUrl() {
        return currentVideoUrl;
    }

    public void setCurrentVideoUrl(String currentVideoUrl) {
        this.currentVideoUrl = currentVideoUrl;
    }

    public boolean isPrepareVideoFlag() {
        return prepareVideoFlag;
    }

    public void setPrepareVideoFlag(boolean prepareVideoFlag) {
        this.prepareVideoFlag = prepareVideoFlag;
    }

    public boolean isReplayVideoFlag() {
        return isReplayVideoFlag;
    }

    public void setReplayVideoFlag(boolean replayVideoFlag) {
        isReplayVideoFlag = replayVideoFlag;
    }

    public boolean isIntro() {
        return isIntro;
    }

    public void setIntro(boolean intro) {
        isIntro = intro;
    }
}
