package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by GOOD on 2018-02-26.
 */

public class VideoPlayGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    Context mContext;
    VideoPlayScreenActivity videoActivity;

    int mWidth, mHeight = 0;
    private Path volumePath;
    private RectF volumeRectf;
    private Path brightPath;
    private RectF brightRectf;

    private VerticalSeekBar brightSeekBar;
    private VerticalSeekBar volumeSeekBar;

//    protected MotionEvent motionEvent = null;
    public VideoPlayGestureDetectorListener mListener;
    public interface VideoPlayGestureDetectorListener{

    }

    public void setListener(VideoPlayGestureDetectorListener listener){
        mListener = listener;
    }

    public VideoPlayGestureDetector(Context context, VideoPlayScreenActivity activity, int width, int height){
        mContext = context;
        brightSeekBar = activity.findViewById(R.id.BrightSeekBar);
        volumeSeekBar = activity.findViewById(R.id.VolumeSeekBar);
        brightSeekBar.setEnabled(false);
        volumeSeekBar.setEnabled(false);
        videoActivity = activity;
        mWidth = width;
        mHeight = height;
        brightPath = new Path();
        volumePath = new Path();
        brightRectf = new RectF();
        volumeRectf = new RectF();
        brightPath.moveTo(0,0);
        brightPath.moveTo(0,height);
        brightPath.moveTo(width/4,height);
        brightPath.moveTo(width/4,0);
        brightPath.close();

        volumePath.moveTo(width/4*3,0);
        volumePath.moveTo(width/4*3,height);
        volumePath.moveTo(width,height);
        volumePath.moveTo(0,height);
        volumePath.close();

        brightPath.computeBounds(brightRectf,true);
        volumePath.computeBounds(volumeRectf,true);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("GestureTag", "y1: "+e1.getY()+", y2: "+e2.getY());
        if (brightRectf.contains(e1.getX(), e1.getY()) && brightRectf.contains(e2.getX(), e2.getY())) {
//            controlBright(e1.getY(), e2.getY());
        } else if (volumeRectf.contains(e1.getX(), e1.getY()) && volumeRectf.contains(e2.getX(), e2.getY())) {
            controlMediaVolume(e1.getY(), e2.getY());
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        return true;
    }



    private void controlMediaVolume(float y1, float y2){
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(y1>y2){
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }else{
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        }
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("TAGVOLUME", String.valueOf(currentVolume));
//        volumeSeekBar.setProgress(currentVolume);
    }
}
