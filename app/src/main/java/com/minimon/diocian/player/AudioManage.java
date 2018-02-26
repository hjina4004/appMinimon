package com.minimon.diocian.player;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by GOOD on 2018-02-23.
 */

public class AudioManage { //볼륨 조절만을 위한 클래스
    private Context mContext;
    private AudioManager audioManager;
    public AudioManage(Context context){
        this.mContext = context;
        audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void checkLocation(int y1, int y2){ //스크롤 이벤트가 일어날 때마다 y값 좌표를 받아서, 볼륨을 올릴지 내릴지 처리
        if(y1 > y2){
            reduceVolume();
        }else if(y1 < y2){
            raiseVolume();
        }
    }

    private void raiseVolume(){
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
    }

    private void reduceVolume(){
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
    }

}
